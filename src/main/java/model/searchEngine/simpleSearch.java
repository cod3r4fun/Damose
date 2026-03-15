package model.searchEngine;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import controller.DateController;
import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import model.transitEngine.StandardVehicleAnalyser;
import model.transitEngine.VehicleTransitEngine;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import model.vehicles.Trip;
import model.vehicles.Vehicle;



/**
 * A simple/standard implementation of the {@link Searcher} interface that provides
 * search functionality for transit stop times based on routes and stops.
 * <p>
 * This class supports both real-time and static (scheduled) data sources,
 * automatically switching between them depending on the active connection
 * status checked via {@link MasterConnectionStatusChecker}.
 * <p>
 * The search methods return:
 * <ul>
 *   <li>Stop times per route</li>
 *   <li>Stop times per stop</li>
 *   <li>Stop times for a specific route and stop combination</li>
 * </ul>
 * <p>
 * Internally, it uses a {@link VehicleTransitEngine} implementation, 
 * specifically {@link StandardVehicleAnalyser}, to calculate expected vehicle
 * arrival times in the real-time search.
 * <p>
 * The class maintains a unique ID per instance and relies on
 * {@link Manager.CityManager} to query city-specific transit data.
 * 
 * @author 
 */
public class simpleSearch implements Searcher {
	/** tracker for number of instances created, used in the assignment of a unique id. */
	private static int createdObjects;
	
    /** The CityManager instance for accessing transit data of a particular city. */
	private final Manager.CityManager cityManager;
	
    /** Unique identifier for this simpleSearch instance. */
	private final int id;
	
    /** Vehicle transit engine used to calculate expected arrival times. */
	private final VehicleTransitEngine vte;
	
    /**
     * Constructs a simpleSearch for the specified city.
     * 
     * @param city The city name to associate this search with; must not be null.
     * @throws IllegalArgumentException if city is null.
     */
	public simpleSearch(String city) {
		if (city == null) throw new IllegalArgumentException("simpleSearch must have a non-null city");
		this.cityManager = Manager.searchCityManager(city);
		vte = new StandardVehicleAnalyser();
		createdObjects ++;
		id = createdObjects;
	}
	
    /**
     * Returns the unique identifier of this search instance.
     * 
     * @return the unique ID assigned to this simpleSearch instance.
     */
	public int getId() {
		return id;
	}
	
	
    /**
     * Searches for all stop times of vehicles on a given route.
     * This method prefers real-time data if the connection is active,
     * otherwise falls back to static scheduled data.
     * 
     * @param routeId the ID of the route to search.
     * @return a map of stops to lists of expected arrival times.
     * @throws IllegalArgumentException if routeId is null.
     */
	@Override
	public synchronized Map<Stop, List<LocalTime>> searchStopTimesPerRoute(String routeId) {
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return searchStaticStopTimesPerRoute(routeId);
		var toRet = searchRTStopTimesPerRoute(routeId);
		// if (toRet.isEmpty()) return searchStaticStopTimesPerRoute(routeId);
		
		return toRet;
	}
	
	
    /**
     * Searches for all stop times of vehicles arriving at a given stop.
     * Prefers real-time data if available, otherwise returns static data.
     * 
     * @param stopId the ID of the stop to search.
     * @return a map of routes to lists of expected arrival times.
     * @throws IllegalArgumentException if stopId is null.
     */
	@Override
	public synchronized Map<Route, List<LocalTime>> searchStopTimesPerStop(String stopId) {
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return searchStaticStopTimesPerStop(stopId);
		var toRet = searchRTStopTimesPerStop(stopId);
		// if (toRet.isEmpty()) return searchStaticStopTimesPerStop(stopId);
		return toRet;
	}
	
	
	
    /**
     * Searches for expected arrival times of vehicles at a specific stop on a specific route.
     * Uses real-time data if available, otherwise uses static scheduled data.
     * 
     * @param routeId the ID of the route.
     * @param stopId the ID of the stop.
     * @return a list of expected arrival times.
     * @throws IllegalArgumentException if routeId or stopId is null.
     */
	@Override
	public List<LocalTime> searchStopTimesPerRouteAndStop(String routeId, String stopId) {
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return searchStaticStopTimesPerRouteAndStop(routeId, stopId);
		var toRet = searchRTStopTimesPerRouteAndStop(routeId, stopId);
		// if (toRet == null || toRet.isEmpty()) return searchStaticStopTimesPerRouteAndStop(routeId, stopId);
		return toRet;
	}
	
	
	
	
	
    /**
     * Searches real-time expected arrival times for all stops on a given route.
     * 
     * @param routeId the route identifier; must not be null.
     * @return map from each stop on the route to a list of expected arrival times.
     * @throws IllegalArgumentException if routeId is null.
     */
	private Map<Stop, List<LocalTime>> searchRTStopTimesPerRoute(String routeId){
		if (routeId == null) throw new IllegalArgumentException("routeId cannot be null");
		
		Map<Stop, List<LocalTime>> stopTimesPerRoute = new HashMap<>();
		List<Vehicle> interestedVehicles = cityManager.getAllVehiclesRT().stream()
				.filter((t) -> t.getRouteId().equals(routeId)).collect(Collectors.toList());
		List<Stop> stops = cityManager.getStopsPerRoute(routeId);
		
		for (var vehicle: interestedVehicles) {
			for (var stop: stops) {
				stopTimesPerRoute.computeIfAbsent(stop, k -> new ArrayList<>()).
				add(vte.expectedArrivalTimeToStop(vehicle, stop, cityManager));
			}
		}
		return stopTimesPerRoute;
	}
	
	
	
    /**
     * Searches real-time expected arrival times for all routes that serve a given stop.
     * 
     * @param stopId the stop identifier; must not be null.
     * @return map from each route serving the stop to a list of expected arrival times.
     * @throws IllegalArgumentException if stopId is null.
     */
	private Map<Route, List<LocalTime>> searchRTStopTimesPerStop(String stopId){
		if (stopId == null) throw new IllegalArgumentException("stopId cannot be null");
		
		Map<Route, List<LocalTime>> stopTimesPerStop = new HashMap<>();
		List<Vehicle> interestedVehicles = cityManager.getAllVehiclesRT().stream()
				.filter((t) -> cityManager.getStopsPerRoute(t.getRouteId()).
						stream().filter((c) -> c.getStopId().equals(stopId)).count() == 1).toList();
		for (var vehicle: interestedVehicles) {
			stopTimesPerStop.computeIfAbsent(cityManager.searchRoute(vehicle.getRouteId()),
					k -> new ArrayList<>()).add(vte.expectedArrivalTimeToStop(vehicle, stopId, cityManager));
		}
		return stopTimesPerStop;
	}
	
	
	
	
    /**
     * Searches real-time expected arrival times at a specific stop on a specific route.
     * 
     * @param routeId the route identifier; must not be null.
     * @param stopId the stop identifier; must not be null.
     * @return list of expected arrival times for the stop on the route.
     * @throws IllegalArgumentException if routeId or stopId is null.
     */
	private List<LocalTime> searchRTStopTimesPerRouteAndStop(String routeId, String stopId){
		if (routeId == null) throw new IllegalArgumentException("routeId cannot be null");
		if (stopId == null) throw new IllegalArgumentException("stopId cannot be null");
		
		return this.searchRTStopTimesPerRoute(routeId).get(cityManager.getStopByStopId(stopId));
	}
	

	
    /**
     * Searches static (scheduled) stop times for all stops on a given route.
     * This is used as a fallback when real-time data is unavailable.
     * 
     * @param routeId the route identifier; must not be null.
     * @return map from each stop on the route to a list of scheduled arrival times.
     * @throws IllegalArgumentException if routeId is null.
     */
	private Map<Stop, List<LocalTime>> searchStaticStopTimesPerRoute(String routeId) {
		if (routeId == null) throw new IllegalArgumentException("routeId cannot be null");
		
		Map<Stop, List<LocalTime>> stopTimesPerRoute = new HashMap<>();
		List<Trip> allTrips = cityManager.getTripsPerRoute(routeId);
		for (var trip: allTrips) {
			if (cityManager.getTripDate(trip).isAfter(DateController.getReferenceDate())) continue;
			for (var stopData: cityManager.getStopsPerTrip(trip)) {
				var t = stopData.getY();
				if (t.isAfter(LocalTime.now().minusMinutes(2)) 
						|| (DateController.getReferenceDate().isAfter(cityManager.getTripDate(trip)) 
						&& LocalDate.now().isBefore(cityManager.getTripDate(trip))))
				stopTimesPerRoute.computeIfAbsent(stopData.getX(), k-> new ArrayList<>()).add(t);
				//System.out.println(t); System.out.println(cityManager.getTripDate(trip));
			}
		}
		
		return stopTimesPerRoute;
	}


	
    /**
     * Searches static (scheduled) stop times for all routes that serve a given stop.
     * Used when real-time data is unavailable.
     * 
     * @param stopId the stop identifier; must not be null.
     * @return map from each route serving the stop to a list of scheduled arrival times.
     * @throws IllegalArgumentException if stopId is null.
     */
	private Map<Route, List<LocalTime>> searchStaticStopTimesPerStop(String stopId) {
		if (stopId == null) throw new IllegalArgumentException("stopId cannot be null");
		
		Map<Route, List<LocalTime>> stopTimesPerStop = new HashMap<>();
		Set<Route> allRoutes = cityManager.getRoutesPerStop(stopId);
		for (var route: allRoutes) {
			for (var trip: cityManager.getTripsPerRoute(route)) {
				if (cityManager.getTripDate(trip).isAfter(DateController.getReferenceDate())) continue;
				var timesPerTrip = cityManager.getStopsPerTrip(trip).stream().filter((t) -> t.getX().getStopId().equals(stopId))
				.collect(Collectors.toList());
				for (var time: timesPerTrip) {
					var t = time.getY();
					if (t.isAfter(LocalTime.now().minusMinutes(2)) 
							|| (DateController.getReferenceDate().isAfter(cityManager.getTripDate(trip)) 
							&& LocalDate.now().isBefore(cityManager.getTripDate(trip))))
					stopTimesPerStop.computeIfAbsent(route, k-> new ArrayList<>()).add(t);
				}
			}
		}
		return stopTimesPerStop;
	}

	
	
    /**
     * Searches static scheduled stop times for a given route and stop.
     * This method is used as fallback when real-time data is unavailable.
     * 
     * @param routeId the route ID.
     * @param stopId the stop ID.
     * @return a list of scheduled stop times for the route-stop pair.
     * @throws IllegalArgumentException if either routeId or stopId is null.
     */
	private List<LocalTime> searchStaticStopTimesPerRouteAndStop(String routeId, String stopId) {
		if (routeId == null) throw new IllegalArgumentException("routeId cannot be null");
		if (stopId == null) throw new IllegalArgumentException("stopId cannot be null");
		
		return this.searchStopTimesPerRoute(routeId).get(cityManager.getStopByStopId(stopId));
	}
	

}
