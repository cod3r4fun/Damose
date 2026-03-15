package model.transitEngine;
import java.time.LocalTime;

import model.vehicles.*;
import model.vehicles.Manager.CityManager;


/**
 * Interface defining methods to estimate the expected arrival time of vehicles at stops.
 * <p>
 * Implementations provide logic to calculate when a given vehicle is expected to arrive at a
 * specific stop, either referenced by a {@link Stop} object or by a stop ID.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public interface VehicleTransitEngine {
	
    /**
     * Calculates the expected arrival time of the specified vehicle at the given stop.
     * 
     * @param vehicle the vehicle whose arrival time is to be estimated
     * @param stop the stop where arrival time is estimated
     * @param cityManager the city manager providing access to transit data
     * @return the expected arrival time as a {@link LocalTime}, or {@code null} if unavailable
     */
	public LocalTime expectedArrivalTimeToStop(Vehicle vehicle, Stop stop, Manager.CityManager cityManager);
	
	
    /**
     * Calculates the expected arrival time of the specified vehicle at the stop identified by stopId.
     * 
     * @param vehicle the vehicle whose arrival time is to be estimated
     * @param stopId the identifier of the stop where arrival time is estimated
     * @param cityManager the city manager providing access to transit data
     * @return the expected arrival time as a {@link LocalTime}, or {@code null} if unavailable
     */
	public LocalTime expectedArrivalTimeToStop(Vehicle vehicle, String stopId, CityManager cityManager);
}
