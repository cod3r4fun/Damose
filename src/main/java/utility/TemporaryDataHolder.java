package utility;

import java.util.ArrayList;
import java.util.List;

import model.vehicles.CalendarDates;
import model.vehicles.Route;
import model.vehicles.Stop;
import model.vehicles.StopTime;
import model.vehicles.Trip;


/**
 * Manages transit-related data .
 * 
 * <p>This class maintains collections for stops, routes, trips, calendar dates, and stop times.
 * Registration methods ensure that duplicate objects are not added except for {@code StopTime}s,
 * which are always added as the collection is expected to be very large.
 * 
 * <p>All registration methods are {@code synchronized} to guarantee thread-safe concurrent access.
 * 
 
 * 
 * <p><strong>Thread Safety:</strong> Internal collections are protected by method-level synchronization
 * during modification to avoid concurrent modification issues.
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * 
 */
public class TemporaryDataHolder {
	private final ArrayList<Stop> allStops;
    private final ArrayList<Route> allRoutes;
    private final ArrayList<Trip> allTrips;
    private final ArrayList<CalendarDates> allDates;
    private final ArrayList<StopTime> allStopTimes;
    
    
    public TemporaryDataHolder() {
    	this.allStops = new ArrayList<>();
        this.allRoutes = new ArrayList<>();
        this.allTrips = new ArrayList<>();
        this.allDates = new ArrayList<>();
        // Pre-sizing to 5 million anticipating large datasets for stop times
        this.allStopTimes = new ArrayList<>(5_000_000);
    }
    
    /**
     * Returns an unmodifiable list of all registered stops.
     * 
     * @return immutable list of stops
     */
    public List<Stop> getAllStops() {
        return List.copyOf(allStops);
    }

    /**
     * Returns an unmodifiable list of all registered trips.
     * 
     * @return immutable list of trips
     */
    public List<Trip> getAllTrips() {
        return List.copyOf(allTrips);
    }
    
    
    /**
     * Returns an unmodifiable list of all registered routes.
     * 
     * @return immutable list of routes
     */
    public List<Route> getAllRoutes(){
    	return List.copyOf(allRoutes);
    }

    /**
     * Returns an unmodifiable list of all registered calendar dates (service exceptions).
     * 
     * @return immutable list of calendar dates
     */
    public List<CalendarDates> getAllDates() {
        return List.copyOf(allDates);
    }

    /**
     * Returns an unmodifiable list of all registered stop times.
     * 
     * @return immutable list of stop times
     */
    public List<StopTime> getAllStopTimes() {
        return List.copyOf(allStopTimes);
    }
    
    
    
    /**
     * Registers a new stop.
     * If the stop already exists, the operation is a no-op.
     * 
     * @param stop the stop to register
     */
    public synchronized void registerStop(Stop stop) {
        if (!allStops.contains(stop)) {
             if(stop != null) allStops.add(stop);
        }
    }

    /**
     * Registers a new route.
     * If the route already exists, the operation is a no-op.
     * 
     * @param route the route to register
     */
    public synchronized void registerRoute(Route route) {
        if (!allRoutes.contains(route)) {
            if (route != null) allRoutes.add(route);
        }
    }

    /**
     * Registers a new trip.
     * If the trip already exists, the operation is a no-op.
     * 
     * @param trip the trip to register
     */
    public synchronized void registerTrip(Trip trip) {
        if (!allTrips.contains(trip)) {
            if (trip != null) allTrips.add(trip);
        }
    }

    /**
     * Registers a new calendar date (service exception).
     * If the calendar date already exists, the operation is a no-op.
     * 
     * @param date the calendar date to register
     */
    public synchronized void registerCalendarDate(CalendarDates date) {
        if (!allDates.contains(date)) {
            if (date!= null) allDates.add(date);
        }
    }

    /**
     * Registers a new stop time.
     * This method always adds the stop time without checking for duplicates,
     * due to potentially very large volume of stop times.
     * 
     * @param stopTime the stop time to register
     */
    public synchronized void registerStopTime(StopTime stopTime) {
        if (stopTime != null) allStopTimes.add(stopTime);
    }
    
   
   

    /**
     * Clears all registered data for this city manager.
     * Useful for resetting state during data reload or cleanup.
     */
    public void reset() {
        allStops.clear();
        allRoutes.clear();
        allTrips.clear();
        allDates.clear();
        allStopTimes.clear();
    }
}
