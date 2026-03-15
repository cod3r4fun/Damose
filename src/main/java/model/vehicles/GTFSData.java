package model.vehicles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import utility.TemporaryDataHolder;
import utility.Triple;


/**
 * Container and accessor for GTFS-formatted static transit data.
 *
 * <p>This class wraps all relevant GTFS datasets parsed from a {@link TemporaryDataHolder}, 
 * including routes, trips, stops, stop times, and calendar data.
 *
 * <p>Internal mappings are constructed during instantiation and used for efficient access 
 * to frequently queried structures such as trips per route, stops per trip, and routes per stop.
 *
 * <p>All returned collections are immutable views to preserve data integrity.
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class GTFSData {
	
	/** List of all known stops in the dataset. */
	private final List<Stop> allStops;

	/** List of all known routes in the dataset. */
	private final List<Route> allRoutes;

	/** Map of route IDs to their route type (e.g., bus, tram, etc.). */
	private final Map<String, Integer> routeTypeById;

	/** Map of stop IDs to their corresponding {@link Stop} object. */
	private final Map<String, Stop> stopByStopId;

	/** Map of stop IDs to the set of routes that pass through each stop. */
	private final Map<String, Set<Route>> routesPerStop;

	/** Map of route IDs to the list of trips that operate under each route. */
	private final Map<String, List<Trip>> tripsPerRoute;

	/** Map of trip IDs to the ordered list of stops (with arrival/departure times). */
	private final Map<String, List<Triple<Stop, LocalTime, LocalTime>>> stopsPerTrip;

	/** Map of stop IDs to all trips stopping there, including time data. */
	private final Map<String, Set<Triple<Trip, LocalTime, LocalTime>>> tripsPerStop;

	/** Map of service IDs to their active service date. */
	private final Map<String, LocalDate> serviceIdToDate;

	
	
	/**
	 * Constructs a GTFS data object from parsed GTFS structures.
	 *
	 * @param tdh the temporary data holder containing parsed GTFS entities
	 */
	public GTFSData(TemporaryDataHolder tdh) {
		allStops = tdh.getAllStops();
		allRoutes = tdh.getAllRoutes();
		
		routeTypeById = allRoutes.stream()
			    .collect(Collectors.toMap(Route::getRouteId, route -> Integer.parseInt(route.getType())));

		stopByStopId = allStops.stream()
				.collect(Collectors.toMap(Stop::getStopId, stop -> stop));

		routesPerStop = new HashMap<>();
		mapRoutesPerStop(tdh, routesPerStop);

		tripsPerRoute = new HashMap<>();
		mapTripsPerRoute(tdh, tripsPerRoute);

		stopsPerTrip = new HashMap<>();
		mapStopsPerTrip(tdh, stopsPerTrip);

		tripsPerStop = new HashMap<>();
		mapTripsPerStop(tdh, tripsPerStop);

		serviceIdToDate = new HashMap<>();
		mapServiceIdToDate(tdh, serviceIdToDate);
		//System.out.println(serviceIdToDate.keySet().stream().filter(key -> serviceIdToDate.get(key).isBefore(LocalDate.now())).toList());	// to remove
		//System.out.println(serviceIdToDate.keySet().stream().filter(key -> serviceIdToDate.get(key).isBefore(LocalDate.now())).toList().size());  // to remove

	}
	
	/**
	 * Retrieves the type of transit service for the given route ID.
	 *
	 * @param routeId the route identifier
	 * @return integer representing route type; -1 if unknown
	 */
	public int getRouteType(String routeId) {
	    return routeTypeById.getOrDefault(routeId, -1);
	}
	
	/**
	 * Looks up a {@link Stop} by its stop ID.
	 *
	 * @param stopId the unique identifier of the stop
	 * @return stop instance or null if not found
	 */
	public Stop getStopByStopId(String stopId) {
		return stopByStopId.get(stopId);
	}
	
	/**
	 * Returns all routes that serve the specified stop.
	 *
	 * @param stopId the stop ID
	 * @return set of routes passing through the stop
	 */
	public Set<Route> getRoutesPerStop(String stopId){
		return routesPerStop.get(stopId);
	}
	
	/**
	 * Returns all trips operating under a specified route.
	 *
	 * @param routeId the route identifier
	 * @return list of trips associated with the route
	 */
	public List<Trip> getTripsPerRoute(String routeId){
		return tripsPerRoute.get(routeId);
	}
	
	/**
	 * Retrieves all stops served by a trip, including time data.
	 *
	 * @param tripId the unique identifier of the trip
	 * @return ordered list of (stop, arrival time, departure time) entries
	 */
	public List<Triple<Stop, LocalTime, LocalTime>> getStopsPerTrip(String tripId){
		return stopsPerTrip.get(tripId);
	}
	
	/**
	 * Returns all trips stopping at a given stop, along with time information.
	 *
	 * @param stopId the unique identifier of the stop
	 * @return set of (trip, arrival time, departure time) entries
	 */
	public Set<Triple<Trip, LocalTime, LocalTime>> getTripsPerStop(String stopId){
		return tripsPerStop.get(stopId);
	}
	
	/**
	 * Gets the service date associated with a given service ID.
	 *
	 * @param serviceId the GTFS service identifier
	 * @return date the service is valid for, or null if missing
	 */
	public LocalDate getServiceIdDate(String serviceId) {
		return serviceIdToDate.get(serviceId);
	}
	
	
	/**
	 * Resolves the operating date for a given trip based on its service ID.
	 *
	 * @param trip the trip to resolve
	 * @return date the trip operates on
	 */
	public LocalDate getTripDate(Trip trip) {
		return getServiceIdDate(trip.getService_id());
	}
	
	/**
	 * Returns all known stops in the dataset.
	 *
	 * @return immutable list of all stops
	 */
	public List<Stop> getAllStops(){
		return List.copyOf(allStops);
	}
	
	/**
	 * Returns all known routes in the dataset.
	 *
	 * @return immutable list of all routes
	 */
	public List<Route> getAllRoutes(){
		return List.copyOf(allRoutes);
	}

	/**
	 * Searches for and returns a route by its ID.
	 *
	 * @param routeId the route identifier
	 * @return route instance if found; otherwise null
	 */
	public Route searchRoute(String routeId) {
		return allRoutes.stream().filter((t) -> t.getRouteId().equals(routeId)).findFirst().orElse(null);
	}
	
	/**
	 * Populates a map from service IDs to service dates based on valid calendar entries.
	 *
	 * @param tdh parsed GTFS data
	 * @param serviceIdToDate the map to populate
	 */
	private static void mapServiceIdToDate(TemporaryDataHolder tdh, Map<String, LocalDate> serviceIdToDate) {
		//System.out.println(tdh.getAllDates().stream().filter(calD -> calD.getDate().isBefore(LocalDate.now())).map(calD -> calD.getService_id()).toList());
		for (CalendarDates calDate: tdh.getAllDates()) {
			if (calDate.getException_type().equals("2")) continue;
			try {
			if (serviceIdToDate.get(calDate.getService_id()).equals(LocalDate.now())) continue;
			} catch (NullPointerException e) {
				
			}
			serviceIdToDate.put(calDate.getService_id(), calDate.getDate());
		}
	}
	
	/**
	 * Builds the mapping from route IDs to the list of trips under each route.
	 *
	 * @param tdh parsed GTFS data
	 * @param tripsPerRoute the map to populate
	 */
	private static void mapTripsPerRoute(TemporaryDataHolder tdh, Map<String, List<Trip>> tripsPerRoute) {
		for (Trip trip : tdh.getAllTrips()) {
			tripsPerRoute.computeIfAbsent(trip.getRoute_id(), k -> new ArrayList<>()).add(trip);
		}
	}
	
	
	// code written by chatGPT based on the author's logic flow
	
	
	/**
	 * Constructs the mapping from stop IDs to all associated routes.
	 *
	 * @param tdh parsed GTFS data
	 * @param routesPerStop the map to populate
	 */
	private static void mapRoutesPerStop(TemporaryDataHolder tdh, Map<String, Set<Route>> routesPerStop) {
		Map<String, String> tripToRoute = new HashMap<>();
		for (Trip trip : tdh.getAllTrips()) {
		    tripToRoute.put(trip.getTrip_id(), trip.getRoute_id());
		}

		Map<String, Route> routeMap = new HashMap<>();
		for (Route route : tdh.getAllRoutes()) {
		    routeMap.put(route.getRouteId(), route);
		}

		for (StopTime st : tdh.getAllStopTimes()) {
		    String stopId = st.getStop_id();
		    String tripId = st.getTrip_id();
		    String routeId = tripToRoute.get(tripId);
		    Route route = routeMap.get(routeId);

		    routesPerStop.computeIfAbsent(stopId, k -> new HashSet<>()).add(route);
		}

	}
	

	
	/**
	 * Creates a mapping of trip IDs to their corresponding sequence of stops with time information.
	 *
	 * @param tdh parsed GTFS data
	 * @param stopsPerTrip the map to populate
	 */
	private static void mapStopsPerTrip(
	        TemporaryDataHolder tdh,
	        Map<String, List<Triple<Stop, LocalTime, LocalTime>>> stopsPerTrip) {

	    // Build a lookup map for StopId -> Stop
	    Map<String, Stop> stopById = new HashMap<>();
	    for (Stop stop : tdh.getAllStops()) {
	        stopById.put(stop.getStopId(), stop);
	    }

	    // Group StopTimes by TripId
	    Map<String, List<StopTime>> groupedByTrip = new HashMap<>();
	    for (StopTime st : tdh.getAllStopTimes()) {
	        groupedByTrip
	            .computeIfAbsent(st.getTrip_id(), k -> new ArrayList<>())
	            .add(st);
	    }

	    // Build the final map
	    for (Map.Entry<String, List<StopTime>> entry : groupedByTrip.entrySet()) {
	        String tripId = entry.getKey();
	        List<StopTime> stopTimes = entry.getValue();

	        stopTimes.sort(Comparator.comparingInt(StopTime::getStop_sequence));

	        List<Triple<Stop, LocalTime, LocalTime>> tripleList = new ArrayList<>();
	        for (StopTime st : stopTimes) {
	            Stop stop = stopById.get(st.getStop_id());
	            if (stop != null) {
	                tripleList.add(new Triple<>(
	                    stop,
	                    st.getArrival_time(),
	                    st.getDeparture_time()
	                ));
	            }
	        }

	        stopsPerTrip.put(tripId, tripleList);
	    }
	}

	
	/**
	 * Maps each stop ID to the set of trips (with timing) that stop there.
	 *
	 * @param tdh parsed GTFS data
	 * @param tripsPerStop the map to populate
	 */
	private static void mapTripsPerStop(TemporaryDataHolder tdh, Map<String, Set<Triple<Trip, LocalTime, LocalTime>>> tripsPerStop) {

		Map<String, Trip> tripById = new HashMap<>();
		for (Trip trip : tdh.getAllTrips()) {
		    tripById.put(trip.getTrip_id(), trip);
		}

		for (StopTime st : tdh.getAllStopTimes()) {
		    String stopId = st.getStop_id();
		    Trip trip = tripById.get(st.getTrip_id());

		    if (trip == null) continue; // safety check

		    Triple<Trip, LocalTime, LocalTime> triple = new Triple<>(
		        trip,
		        st.getArrival_time(),
		        st.getDeparture_time()
		    );

		    tripsPerStop.computeIfAbsent(stopId, k -> new HashSet<>()).add(triple);
		}
		

	}
	

	
}
