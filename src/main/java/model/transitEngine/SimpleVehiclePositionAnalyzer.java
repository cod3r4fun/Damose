package model.transitEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import model.vehicles.Manager;
import model.vehicles.Stop;
import model.vehicles.Trip;
import utility.Triple;

public class SimpleVehiclePositionAnalyzer implements VehiclePositionAnalyzer{
	
	/**
	 * A simple/standard implementation of {@link VehiclePositionAnalyzer} that retrieves vehicle positions
	 * either from real-time data or static scheduled data depending on connection availability.
	 * <p>
	 * If a live connection is active, real-time vehicle positions are returned.
	 * Otherwise, positions are estimated based on scheduled trips and stops.
	 * </p>
	 * <p>
	 * Uses {@link Manager.CityManager} to access trip and stop data and
	 * {@link MasterConnectionStatusChecker} to determine connection status.
	 * </p>
	 * 
	 * @author Franco Della Negra
	 * @version 1.0
	 * @since 1.0
	 */

	
	
	
	
    /**
     * Finds the list of vehicle positions for the given route ID.
     * <p>
     * If a real-time connection is active, returns real-time positions.
     * Otherwise, estimates vehicle positions based on static trip data.
     * </p>
     * 
     * @param routeId the route identifier
     * @param cityManager the city manager to access transit data
     * @return list of {@link GeoPosition} objects representing vehicle locations on the route
     */
	@Override
	public List<GeoPosition> findVehiclePositionsForRoute(String routeId, Manager.CityManager cityManager) {
		//System.out.println("launched for route: " + routeId);
		if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return findVeihclePositionsForRouteRT(routeId, cityManager);
		//System.out.println(findVeihclePositionsForRouteStatic(routeId, cityManager));
		return findVeihclePositionsForRouteStatic(routeId, cityManager);
	}

	
	
	
    /**
     * Finds the list of vehicle positions for the given route ID and direction.
     * <p>
     * If a real-time connection is active, returns real-time positions.
     * Otherwise, estimates vehicle positions based on static trip data filtered by direction.
     * </p>
     * 
     * @param routeId the route identifier
     * @param direction the direction identifier
     * @param cityManager the city manager to access transit data
     * @return list of {@link GeoPosition} objects representing vehicle locations on the route and direction
     */
	@Override
	public List<GeoPosition> findVehiclePositionsForRouteAndDirection(String routeId, String direction, Manager.CityManager cityManager) {
		//System.out.println("launched for route: " + routeId +" and direction: " +direction);
		if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return findVeihclePositionsForRouteAndDirectionRT(routeId, direction, cityManager);
		//System.out.println(findVeihclePositionsForRouteAndDirectionStatic(routeId, direction, cityManager));
		return findVeihclePositionsForRouteAndDirectionStatic(routeId, direction, cityManager);
	}
	
	
	
	
    /**
     * Retrieves static estimated positions of vehicles currently on the route based on ongoing trips.
     * 
     * @param routeId the route identifier
     * @param cityManager the city manager providing trip and stop data
     * @return list of estimated {@link GeoPosition} objects of vehicles on the route
     */
	private List<GeoPosition> findVeihclePositionsForRouteStatic(String routeId, Manager.CityManager cityManager){
		List<Trip> relevantTrips = cityManager.getTripsPerRoute(routeId).stream()
				.filter(trip -> isTripOngoingNow(trip, cityManager)).toList();
		//System.out.println(relevantTrips.size());
		List<GeoPosition> positions = new ArrayList<>();
		for (var trip: relevantTrips) {
			var pos = findPositionPerTrip(trip, cityManager);
			if (pos != null) {
				positions.add(pos);
			}
		}
		return positions;
	}
	
	
    /**
     * Retrieves static estimated positions of vehicles currently on the route and direction based on ongoing trips.
     * 
     * @param routeId the route identifier
     * @param direction the direction identifier
     * @param cityManager the city manager providing trip and stop data
     * @return list of estimated {@link GeoPosition} objects of vehicles on the route and direction
     */
	private List<GeoPosition> findVeihclePositionsForRouteAndDirectionStatic(String routeId, String direction, Manager.CityManager cityManager){
		List<Trip> relevantTrips = cityManager.getTripsPerRoute(routeId).stream()
				.filter(trip -> trip.getDirection_id().equals(direction))
				.filter(trip -> isTripOngoingNow(trip, cityManager)).toList();
		//System.out.println(relevantTrips.size());
		List<GeoPosition> positions = new ArrayList<>();
		for (var trip: relevantTrips) {
			var pos = findPositionPerTrip(trip, cityManager);
			if (pos != null) {
				positions.add(pos);
			}
		}
		return positions;
	}
	
	
	
    /**
     * Retrieves real-time positions of vehicles running on the specified route.
     * 
     * @param routeId the route identifier
     * @param cityManager the city manager providing real-time vehicle data
     * @return list of {@link GeoPosition} objects representing real-time vehicle positions on the route
     */
	private List<GeoPosition> findVeihclePositionsForRouteRT(String routeId, Manager.CityManager cityManager){
		 return cityManager.getAllVehiclesRT().stream().filter(vehicle -> vehicle.getRouteId().equals(routeId))
		.map(vehicle -> vehicle.getPosition()).toList();
	}
	
	
    /**
     * Retrieves real-time positions of vehicles running on the specified route and direction.
     * 
     * @param routeId the route identifier
     * @param direction the direction identifier
     * @param cityManager the city manager providing real-time vehicle data
     * @return list of {@link GeoPosition} objects representing real-time vehicle positions on the route and direction
     */
	private List<GeoPosition> findVeihclePositionsForRouteAndDirectionRT(String routeId, String direction, Manager.CityManager cityManager){
		 return cityManager.getAllVehiclesRT().stream().filter(vehicle -> vehicle.getRouteId().equals(routeId))
		.filter(vehicle -> vehicle.getDirection().equals(direction)).map(vehicle -> vehicle.getPosition()).toList();
	}
	
	
	
	
	
	
    /**
     * Determines if a given trip is currently ongoing based on current system time and trip schedule.
     * Handles overnight trips spanning past midnight.
     * 
     * @param trip the trip to check
     * @param cityManager the city manager to retrieve trip date and stop times
     * @return {@code true} if the trip is ongoing now, {@code false} otherwise
     */
	// written by chatGPT with small changes
	private boolean isTripOngoingNow(Trip trip, Manager.CityManager cityManager) {
	    LocalDate tripDate = cityManager.getTripDate(trip);
	    if (tripDate == null) {
	    	return false;
	    }

	    List<Triple<Stop, LocalTime, LocalTime>> stops = cityManager.getStopsPerTrip(trip);
	    if (stops == null || stops.isEmpty()) {
	    	return false;
	    }

	    Triple<Stop, LocalTime, LocalTime> firstStop = stops.get(0);
	    Triple<Stop, LocalTime, LocalTime> lastStop = stops.get(stops.size() - 1);

	    LocalTime firstDeparture = firstStop.getZ();
	    LocalTime lastArrival = lastStop.getY();

	    LocalDateTime startDateTime = LocalDateTime.of(tripDate, firstDeparture);
	    LocalDateTime endDateTime = LocalDateTime.of(tripDate, lastArrival);

	    	if (endDateTime.isBefore(startDateTime)) {
	        endDateTime = endDateTime.plusDays(1);
	    }
	   
	    LocalDateTime now = LocalDateTime.now();
	    

	    return startDateTime.isBefore(now) && endDateTime.isAfter(now);
	}
	
	
	
	
    /**
     * Estimates the vehicle position for a given trip by returning the position of the closest stop.
     * 
     * @param trip the trip to analyze
     * @param cityManager the city manager providing stop position data
     * @return the {@link GeoPosition} of the closest stop, or {@code null} if unavailable
     */
	private GeoPosition findPositionPerTrip(Trip trip, Manager.CityManager cityManager) {
		var stop = getClosestStop(trip, cityManager);
		if (stop == null) return null;
		return stop.getPosition();
	}
	

	
	
	
    /**
     * Finds the closest stop to the current time in the specified trip.
     * <p>
     * Used for estimating the vehicle's approximate position when real-time data is unavailable.
     * </p>
     * 
     * @param trip the trip to analyze
     * @param cityManager the city manager providing trip stop information
     * @return the {@link Stop} closest to the current time in the trip, or {@code null} if none found
     */
	// written by chatGPT with small changes
	private Stop getClosestStop(Trip trip, Manager.CityManager cityManager) {
	    LocalDate tripDate = cityManager.getTripDate(trip);
	    if (tripDate == null) return null;

	    List<Triple<Stop, LocalTime, LocalTime>> stops = cityManager.getStopsPerTrip(trip);
	    if (stops == null || stops.isEmpty()) return null;

	    LocalDateTime now = LocalDateTime.now();
	    int closestIndex = -1;
	    long minDifference = Long.MAX_VALUE;

	    LocalDateTime previousDateContext = LocalDateTime.of(tripDate, LocalTime.MIDNIGHT);

	    for (int i = 0; i < stops.size(); i++) {
	        Triple<Stop, LocalTime, LocalTime> stopData = stops.get(i);

	        LocalTime arrival = stopData.getY();
	        LocalTime departure = stopData.getZ();

	        // Convert to LocalDateTime with potential midnight wrap
	        LocalDateTime arrivalDT = LocalDateTime.of(tripDate, arrival);
	        LocalDateTime departureDT = LocalDateTime.of(tripDate, departure);

	        if (i > 0) {
	            if (arrivalDT.isBefore(previousDateContext)) arrivalDT = arrivalDT.plusDays(1);
	            if (departureDT.isBefore(previousDateContext)) departureDT = departureDT.plusDays(1);
	        }

	        previousDateContext = departureDT;

	        // Compare both arrival and departure
	        long diffArrival = Math.abs(java.time.Duration.between(arrivalDT, now).toSeconds());
	        long diffDeparture = Math.abs(java.time.Duration.between(departureDT, now).toSeconds());

	        long minStopDiff = Math.min(diffArrival, diffDeparture);

	        if (minStopDiff < minDifference) {
	            minDifference = minStopDiff;
	            closestIndex = i;
	        }
	    }
	    
	    if (closestIndex <0) return null;
	    return stops.get(closestIndex).getX();
	}

	
}
