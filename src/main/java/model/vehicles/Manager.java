package model.vehicles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import utility.TemporaryDataHolder;
import utility.Triple;
import utility.exceptionUtils.DuplicationNotAccepted;

//documentation written with the help of chatGPT


/**
 * Manager class responsible for managing multiple city-specific transit data managers.
 * 
 * <p>This class holds a static registry of all created cities and their respective {@link CityManager} instances,
 * preventing duplication of city data managers.
 * 
 * <p>The {@code CityManager} inner class encapsulates all transit-related data for a particular city,
 * including stops, routes, trips, calendar exceptions, and stop times and realt-time vehicle tracking;
 * 
 * <p>Data registration methods in {@code CityManager} are synchronized to ensure thread safety
 * during concurrent modifications.
 * 
 * <p><strong>Usage Overview:</strong>
 * <ul>
 *   <li>Create a {@code CityManager} for a city (throws {@link DuplicationNotAccepted} if city already exists)</li>
 *   <li>Use the city manager to register transit data elements</li>
 *   <li>Query registered data via immutable lists</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> {@code CityManager} instances are tracked in a static list accessible via {@link #getAllCityManagers()}.
 * 
 * @author	Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Manager {

    /**
     * Static list of city names for which managers have been created.
     * Used to prevent duplication of city managers.
     */
    private static final ArrayList<String> cities = new ArrayList<>();

    /**
     * Static list of all {@link CityManager} instances created.
     */
    private static final ArrayList<CityManager> allCityManagers = new ArrayList<>();

    /**
     * Singleton exception instance for duplication error when attempting to create
     * a CityManager for a city that already exists.
     */
    private static final DuplicationNotAccepted DuplError = new DuplicationNotAccepted();

    /**
     * Returns an unmodifiable list of all created {@link CityManager} instances.
     *
     * @return immutable list of all registered city managers
     */
    public static List<CityManager> getAllCityManagers() {
        return List.copyOf(allCityManagers);
    }
    
    
    /**
     * Retrieves the {@link CityManager} associated with the specified city.
     *
     * @param city the city name to search for
     * @return the corresponding CityManager, or {@code null} if not found
     */
    
    public static CityManager searchCityManager(String city) {
    	for (var cityMan: allCityManagers) {
    		if (cityMan.getCity().equals(city)) {
    			return cityMan;
    		}
    	}
    	return null;
    }

   
    
    
    /**
     * Represents a data container and manager for GTFS and real-time transit data of a specific city.
     *
     * <p>Each {@code CityManager} instance is responsible for accessing (reading, registering and updating)
     * transit data for a particular city, including routes, stops, trips, calendars, stop times,
     * and real-time vehicles.
     *
     * <p><strong>Thread Safety:</strong> Registration methods for real-time vehicles are synchronized.
     *
     * <p><strong>Typical Usage:</strong>
     * <ul>
     *   <li>Set GTFS data using {@link #setGTFSData(TemporaryDataHolder)}</li>
     *   <li>Query GTFS structures like routes, trips, and stops</li>
     *   <li>Track real-time vehicles and their updates</li>
     * </ul>
     */
    public class CityManager {

    	/**
    	 * Flag indicating whether this city manager is currently being updated.
    	 */
        private boolean inUpdate;
        
        /**
         * Name of the city this {@code CityManager} is associated with.
         */
        private final String city;
        
        /**
         * GTFS dataset containing static transit information for this city.
         */
        private GTFSData gtfsData;
        
        /**
         * List storing real-time vehicle instances for this city.
         */
        private final ArrayList<Vehicle> allVehiclesRT;
        

        /**
         * Constructs a {@code CityManager} for a given city.
         *
         * @param city the name of the city to manage
         * @throws DuplicationNotAccepted if a manager for this city already exists
         */
        public CityManager(String city) throws DuplicationNotAccepted {
            if (cities.contains(city)) {
                throw DuplError;
            }
            this.allVehiclesRT = new ArrayList<>();
            inUpdate = false;
            this.city = city;
            cities.add(city);
            allCityManagers.add(this);
        }
        
        
        /**
         * Searches for a route by its unique ID.
         *
         * @param routeId the route ID to search
         * @return the corresponding {@link Route}, or {@code null} if not found
         */
        public Route searchRoute(String routeId) {
        	return gtfsData.searchRoute(routeId);
        }
        
        /**
         * Returns the type of a route (e.g., bus, tram) based on its ID.
         *
         * @param routeId the ID of the route
         * @return integer representing the GTFS route type
         */
        public int getRouteTypeById(String routeId) {
        	return gtfsData.getRouteType(routeId);
        }
        
        /**
         * Retrieves a stop by its stop ID.
         *
         * @param stopId the ID of the stop
         * @return the corresponding {@link Stop} object
         */
        public Stop getStopByStopId(String stopId) {
        	return gtfsData.getStopByStopId(stopId);
        }
        
        /**
         * Loads and sets the GTFS data for this city manager.
         *
         * @param tdh a temporary data holder containing pre-parsed GTFS data
         */
        public void setGTFSData(TemporaryDataHolder tdh) {
        	gtfsData = new GTFSData(tdh);
        }
        
        /**
         * Returns the set of routes that serve the specified stop.
         *
         * @param stopId the ID of the stop
         * @return a set of routes passing through the stop
         */
        public Set<Route> getRoutesPerStop(String stopId){
        	return gtfsData.getRoutesPerStop(stopId);
        }
        
        /**
         * Returns the set of routes that include the specified stop.
         *
         * @param stop the {@link Stop} object
         * @return set of routes that serve the stop
         */
        public Set<Route> getRoutesPerStop(Stop stop){
    		return getRoutesPerStop(stop.getStopId());
    	}
       
        /**
         * Retrieves the list of trips assigned to a particular route.
         *
         * @param routeId the ID of the route
         * @return list of trips associated with the route
         */
        public List<Trip> getTripsPerRoute(String routeId){
        	return gtfsData.getTripsPerRoute(routeId);
        }
        
        
        /**
         * Retrieves all trips associated with a given route.
         *
         * @param route the {@link Route} object
         * @return list of trips belonging to the route
         */
        public  List<Trip> getTripsPerRoute(Route route){
    		return getTripsPerRoute(route.getRouteId());
    	}
        
        /**
         * Returns the stop sequence for a given trip.
         *
         * @param tripId the ID of the trip
         * @return list of triples (stop, arrival time, departure time)
         */
        public List<Triple<Stop, LocalTime, LocalTime>> getStopsPerTrip(String tripId){
        	return gtfsData.getStopsPerTrip(tripId);
        }
        
        /**
         * Returns the sequence of stops for a given trip.
         *
         * @param trip the {@link Trip} object
         * @return list of triples (stop, arrival time, departure time) for the trip
         */
        public List<Triple<Stop, LocalTime, LocalTime>> getStopsPerTrip(Trip trip){
    		return getStopsPerTrip(trip.getTrip_id());
    	}
    	
        /**
         * Returns all trips that include the specified stop.
         *
         * @param stopId the ID of the stop
         * @return set of triples (trip, arrival time, departure time)
         */
        public Set<Triple<Trip, LocalTime, LocalTime>> getTripsPerStop(String stopId){
        	return gtfsData.getTripsPerStop(stopId);
        }
        
        
        /**
         * Retrieves all trips that pass through the specified stop.
         *
         * @param stop the {@link Stop} object to query
         * @return set of triples (trip, arrival time, departure time) for the stop
         */
        public Set<Triple<Trip, LocalTime, LocalTime>> getTripsPerStop(Stop stop){
    		return getTripsPerStop(stop.getStopId());
    	}
        
        /**
         * Retrieves the active date associated with a given service ID.
         *
         * @param serviceId the service identifier
         * @return the date the service is scheduled for
         */
        public LocalDate getServiceIdDate(String serviceId) {
        	return gtfsData.getServiceIdDate(serviceId);
        }
        
        /**
         * Returns the operating date for a specific trip.
         *
         * @param trip the trip whose date is required
         * @return the local date of the trip
         */
        public LocalDate getTripDate(Trip trip) {
        	return gtfsData.getTripDate(trip);
        }
        
        
        /**
         * Computes all stops served by a route, taking directionality into account if applicable.
         *
         * @param routeId the ID of the route
         * @return list of unique stops served by the route
         */
        public List<Stop> getStopsPerRoute(String routeId){
        	ArrayList<Stop> stops = new ArrayList<>(); 	
        	if (!routeHasDirection(routeId)) {
        		if (gtfsData.getTripsPerRoute(routeId) == null) return null;
        		for (var stopData: gtfsData.getStopsPerTrip(gtfsData.getTripsPerRoute(routeId).getFirst().getTrip_id())){
        			stops.add(stopData.getX());
        		}
        	} else {
        		var stops1 = getStopsPerRouteAndDirection(routeId, "0");
        		var stops2 = new ArrayList<>(getStopsPerRouteAndDirection(routeId, "1"));
        		for (var stop: stops1) {
        			if (stops2.contains(stop)) stops2.remove(stop);
        		}
        		stops.addAll(stops1); stops.addAll(stops2);
        	}

        	return List.copyOf(stops);
        }
        
        
        /**
         * Determines if a route has trips with multiple travel directions (e.g., inbound/outbound).
         *
         * @param routeId the ID of the route
         * @return {@code true} if route has both direction "0" and "1"; {@code false} otherwise
         */
        public boolean routeHasDirection(String routeId) {
        	if (gtfsData.getTripsPerRoute(routeId) == null) return false;
        	return gtfsData.getTripsPerRoute(routeId).stream().anyMatch(trip -> trip.getDirection_id().equals("0")) &&
        			 gtfsData.getTripsPerRoute(routeId).stream().anyMatch(trip -> trip.getDirection_id().equals("1"));
        }
        
        /**
         * Retrieves the ordered list of stops for a specific route and direction.
         *
         * @param routeId the ID of the route
         * @param direction the direction ID ("0" or "1")
         * @return list of stops in the given direction
         */
        public List<Stop> getStopsPerRouteAndDirection(String routeId, String direction){
        	ArrayList<Stop> stops = new ArrayList<>();
        	
        	for (var stopData: gtfsData.getStopsPerTrip(
        			 gtfsData.getTripsPerRoute(routeId).stream().filter(trip -> trip.getDirection_id().equals(direction)).findFirst().get().getTrip_id()
        			)){
    			stops.add(stopData.getX());
    		}
        	
        	return List.copyOf(stops);
        }
        
        
        /**
         * Computes all stops served by a route, taking directionality into account if applicable.
         *
         * @param route the route for which the stops are needed
         * @return list of unique stops served by the route
         */
        public List<Stop> getStopsPerRoute(Route route){
        	return getStopsPerRoute(route.getRouteId());
        }
        
        
        /**
         * Returns a list of all stops available in the city.
         *
         * @return list of all stops
         */
        public List<Stop> getAllStops(){
        	return gtfsData.getAllStops();
        }
        
        
        /**
         * Returns a list of all transit routes in the city.
         *
         * @return list of all routes
         */
        public List<Route> getAllRoutes(){
        	return gtfsData.getAllRoutes();
        }
        


        /**
         * Returns the city name managed by this {@code CityManager}.
         * 
         * @return the city name
         */
        public String getCity() {
            return city;
        }
        
        /**
         * Returns an unmodifiable list containing the last state of tracked vehicles
         * 
         * @return immutable list of tracked vehicles
         */
        public List<Vehicle> getAllVehiclesRT(){
        	return List.copyOf(allVehiclesRT);
        }
        
        
        /**
         * Registers a new vehicle with this city manager.
         * This method always adds the vehicle without checking for duplicates,
         * due to potentially very large volume of vehicles.
         * 
         * @param vehicle the vehicle to register
         */
        public synchronized void registerVehicle(Vehicle vehicle) {
        	if (vehicle != null) allVehiclesRT.add(vehicle);
        }
        
        
        /**
         * Clears the list of tracked real-time vehicles.
         */
        public void resetVehiclesRT() {
        	allVehiclesRT.clear();
        }
        
        /**
         * Returns true if the cityManager is being updated by a parser
         * 
         * @return the update status
         */
        public boolean isInUpdate() {
        	return inUpdate;
        }


        
        /**
         * To use when starting to update the cityManager
         */
        public void isBeingUpdated() {
        	inUpdate = true;
        }
        
        
        /**
         * To use when the update is finished
         */
        public void terminateUpdate() {
        	inUpdate = false;
        }


    }
}

