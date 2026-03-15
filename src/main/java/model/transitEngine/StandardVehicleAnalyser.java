package model.transitEngine;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.Manager.CityManager;
import model.vehicles.Stop;
import model.vehicles.Trip;
import model.vehicles.Vehicle;
import utility.TransitUtils;
import utility.Triple;


/**
 * Standard implementation of {@link VehicleTransitEngine} providing expected arrival time
 * calculations for vehicles at stops based on real-time vehicle data and scheduled trip information.
 * <p>
 * This analyser focuses on a specific vehicle to compute expected arrival times to a given stop.
 * It integrates real-time vehicle position, last known stop sequence, and scheduled trip stop times
 * to estimate delays and expected arrival times.
 * </p>
 * <p>
 * If no relevant trip or valid stop sequence is found, or if the expected arrival is too far in the past,
 * the method returns {@code null} indicating that the estimate is unavailable.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class StandardVehicleAnalyser implements VehicleTransitEngine {
	
	/**
	 * The vehicle in analysis
	 */
	private Vehicle vehicleInAnalysis;
	
    /**
     * Constructs a new StandardVehicleAnalyser with no vehicle focused initially.
     */
	public StandardVehicleAnalyser() {
		vehicleInAnalysis = null;
	}

    /**
     * Sets the vehicle currently under analysis.
     * 
     * @param v the vehicle to focus on
     */
	private void focusOn(Vehicle v) {
		vehicleInAnalysis = v;
	}
	
    /**
     * {@inheritDoc}
     * <p>
     * Calculates the expected arrival time of the specified vehicle at the given stop.
     * </p>
     * @param vehicle the vehicle to analyze
     * @param stop the stop to estimate arrival time at
     * @param cityManager the city manager providing trip and stop data
     * @return the expected arrival time as a {@link LocalTime}, or {@code null} if unavailable
     */
	@Override
	public LocalTime expectedArrivalTimeToStop(Vehicle vehicle, Stop stop, CityManager cityManager) {
		if (vehicle.getTimestamp() < 0) return null;
		return expectedArrivalTimeToStop(vehicle, stop.getStopId(), cityManager);
	}
	
    /**
     * {@inheritDoc}
     * <p>
     * Calculates the expected arrival time of the specified vehicle at the stop identified by stopId.
     * </p>
     * 
     * @param vehicle the vehicle to analyze
     * @param stopId the ID of the stop to estimate arrival time at
     * @param cityManager the city manager providing trip and stop data
     * @return the expected arrival time as a {@link LocalTime}, or {@code null} if unavailable
     */
	@Override
	public LocalTime expectedArrivalTimeToStop(Vehicle vehicle, String stopId, CityManager cityManager) {
		if (vehicle.getTimestamp() < 0) return null;
		focusOn(vehicle);
		return expectedArrivalTimeToStop(stopId, cityManager);
	}


	
	 /**
     * Internal method to estimate the expected arrival time of the focused vehicle at the stop with the given ID.
     * <p>
     * Uses vehicle's last known position and timestamp, scheduled trips matching the vehicle route and direction,
     * and estimates delays to compute the arrival time.
     * </p>
     * 
     * @param stopId the ID of the target stop
     * @param cityManager the city manager providing trip and stop data
     * @return the estimated arrival time as {@link LocalTime}, or {@code null} if no estimate is possible
     */
	private LocalTime expectedArrivalTimeToStop(String stopId, CityManager cityManager) {
	    
	    int currentSeq = vehicleInAnalysis.getCurrentStopSequence();
	    
	    GeoPosition lastPos = vehicleInAnalysis.getPosition();
	    
	    LocalTime lastUpdateTime = Instant.ofEpochSecond(vehicleInAnalysis.getTimestamp())
	        .atZone(ZoneId.systemDefault()).toLocalTime();
	    
	    List<Trip> relevantTrips = cityManager.getTripsPerRoute(vehicleInAnalysis.getRouteId()).stream()
	    	    .filter(t -> t.getDirection_id().equals(vehicleInAnalysis.getDirection()))
	    	    .filter(t -> {
	    	        var stops = cityManager.getStopsPerTrip(t).stream()
	    	            .map(triple -> triple.getX().getStopId()).toList();
	    	        return stops.contains(stopId);
	    	    }).collect(Collectors.toList());

	    if (relevantTrips.isEmpty()) {
	    	return null;
	    }
	    

	    Trip anyTrip = relevantTrips.get(0);
	    List<Triple<Stop, LocalTime, LocalTime>> stopTimePairs = cityManager.getStopsPerTrip(anyTrip);
	    if (currentSeq <= 1 || currentSeq > stopTimePairs.size()) {
	    	return null;
	    }

	    
	    Stop lastStop = stopTimePairs.get(currentSeq - 1).getX();

	    Duration estimatedTravelTime = TransitUtils.
	    		estimateTravelTime(lastStop.getPosition(), lastPos, 
	    				cityManager.getRouteTypeById(vehicleInAnalysis.getRouteId()));
	    
	    
	    LocalTime reconstructedTimeAtLastStop = lastUpdateTime.minus(estimatedTravelTime);

	    Trip mostRelevantTrip = relevantTrips.stream()
	        .filter(t -> cityManager.getStopsPerTrip(t).stream()
	            .anyMatch(pair -> pair.getX().getStopId().equals(lastStop.getStopId())))
	        .min(Comparator.comparing(t -> {
	            Optional<LocalTime> scheduled = cityManager.getStopsPerTrip(t).stream()
	                .filter(triple -> triple.getX().getStopId().equals(lastStop.getStopId()))
	                .map(triple -> triple.getY()).findFirst();
	            return scheduled.map(sched -> Duration.between(reconstructedTimeAtLastStop, sched).abs())
	                            .orElse(Duration.ofHours(24)); 
	        }))
	        .orElse(null);

	    if (mostRelevantTrip == null) {
	    	return null;
	    }
	    

	    Optional<LocalTime> schedLastStop = cityManager.getStopsPerTrip(mostRelevantTrip).stream()
	        .filter(triple -> triple.getX().getStopId().equals(lastStop.getStopId()))
	        .map(triple -> triple.getY())
	        .findFirst();

	    Optional<LocalTime> schedTargetStop = cityManager.getStopsPerTrip(mostRelevantTrip).stream()
	        .filter(triple -> triple.getX().getStopId().equals(stopId))
	        .map(triple -> triple.getY()).findFirst();
	    
	    
	    /*
	    if (schedTargetStop == null || schedTargetStop.isEmpty()) {
	    	System.out.print("stopId ricercato" + stopId + "stopId nella sequenza");
	    	System.out.println(cityManager.getStopsPerTrip(mostRelevantTrip).stream()
	    			.map(triple -> triple.getX().getStopId()));
	    }
	    
	    if (schedLastStop == null || schedLastStop.isEmpty()) {
	    	System.out.print("stopId ricercato (last stop)" + lastStop.getStopId() + "stopId nella sequenza");
	    	System.out.println(cityManager.getStopsPerTrip(mostRelevantTrip).stream()
	    			.map(triple -> triple.getX().getStopId()));
	    }
	    */
	    

	    if (schedLastStop.isEmpty() || schedTargetStop.isEmpty()) {
	    	return null;
	    }

	    Duration delay = Duration.between(schedLastStop.get(), reconstructedTimeAtLastStop);

	    schedTargetStop.get().plus(delay);
	    if (schedTargetStop.get().plus(delay).isBefore(LocalTime.now().minusMinutes(2)) && !LocalTime.now().isAfter(LocalTime.of(23, 0, 0))) {
	    	return null;
	    }
	    return schedTargetStop.get().plus(delay);
	}
	
}
