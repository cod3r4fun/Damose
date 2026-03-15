package model.vehicles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * Represents a tracked vehicle within the transit system.
 * <p>
 * This class encapsulates all relevant information about a vehicle's current state,
 * including its position, route, trip, and operational parameters. It is intended
 * for use in real-time tracking and reporting applications.
 * </p>
 * <p>
 * Instances of this class are immutable after construction.
 * </p>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */


public class Vehicle {
	/**
	 * Unique identifier for the vehicle.
	 */

	private final String vehicleId;
	
	/**
	 * Human-readable label or name for the vehicle.
	 */
	private final String label;
	
	/**
	 * Identifier of the next stop the vehicle is scheduled to reach.
	 */
	private final String nextStopId;
	
	/**
	 * Epoch timestamp (milliseconds) representing the last update time for the vehicle's data.
	 */
	private final long timestamp;
	
	/**
	 * Sequence number of the current stop in the trip.
	 */
	private final int currentStopSequence;
	
	/**
	 * Current geographical position of the vehicle.
	 */
	private final GeoPosition position;
	
	/**
	 * Name of the city in which the vehicle operates.
	 */
	private final String city;
	
	/**
	 * Current bearing (direction) of the vehicle in degrees.
	 */
	private final float bearing;
	
	/**
	 * Odometer reading (distance traveled) in kilometers.
	 */
	private final double odometer;
	
	/**
	 * Current speed of the vehicle in kilometers per hour.
	 */
	private final double speed;
	
	
	/*
	/**
	 * Identifier of the associated static trip, determined by route and direction.
	 *\/
	private final String associatedStaticTripId;    // (not needed as of now)
	*/
	
	/**
	 * Identifier of the current trip assigned to the vehicle.
	 */
	private final String tripId;
	
	/**
	 * Scheduled start time of the trip.
	 */
	private final LocalTime startTime;
	
	/**
	 * Scheduled start date of the trip.
	 */
	private final LocalDate startDate;
	
	/**
	 * Identifier of the route on which the vehicle is operating.
	 */
	private final String routeId;
	
	/**
	 * Direction of travel for the current trip (e.g., "0" or "1").
	 */
	private final String direction;
	
	
	/**
	 * Constructs a new {@code Vehicle} instance with the specified parameters.
	 *
	 * @param vehicleId           Unique identifier for the vehicle; must not be {@code null}.
	 * @param label               Human-readable label or name for the vehicle.
	 * @param nextStopId          Identifier of the next stop the vehicle is scheduled to reach; must not be {@code null}.
	 * @param timestamp           Epoch timestamp (milliseconds) representing the last update time.
	 * @param currentStopSequence Sequence number of the current stop in the trip.
	 * @param latitude            Current latitude of the vehicle.
	 * @param longitude           Current longitude of the vehicle.
	 * @param city                Name of the city in which the vehicle operates.
	 * @param bearing             Current bearing (direction) of the vehicle in degrees.
	 * @param odometer            Odometer reading (distance traveled) in kilometers.
	 * @param speed               Current speed of the vehicle in kilometers per hour.
	 * @param tripId              Identifier of the current trip.
	 * @param startTime           Scheduled start time of the trip.
	 * @param startDate           Scheduled start date of the trip.
	 * @param routeId             Identifier of the route; must not be {@code null}.
	 * @param direction           Direction of travel (e.g., "0" or "1"); must not be {@code null}.
	 * @throws IllegalArgumentException if any required parameter is {@code null} or if the route is not found in static data.
	 */

	
	public Vehicle(String vehicleId, String label, String nextStopId,
			long timestamp, int currentStopSequence, 
			float latitude, float longitude, String city,
			float bearing, double odometer, double speed,
			String tripId, LocalTime startTime, LocalDate startDate,
			String routeId, String direction) {
		
		if (vehicleId == null) throw new IllegalArgumentException("vehicleId cannot be null");
		if(nextStopId == null)  throw new IllegalArgumentException("nextStopId cannot be null");
		if( routeId == null)  throw new IllegalArgumentException("routeId cannot be null");
		if( direction == null) throw new IllegalArgumentException("direction cannot be null");
		if (city == null) throw new IllegalArgumentException("city cannot be null");
		//if ( Manager.searchCityManager(city).getTripsPerRoute(routeId) == null) throw new IllegalArgumentException("route not found in static");
		
		this.vehicleId = vehicleId;
		this.label = label;
		this.nextStopId = nextStopId;
		this.timestamp = timestamp;
		this.currentStopSequence = currentStopSequence;
		this.position = new GeoPosition(latitude, longitude);
		this.city = city;
		this.bearing = bearing;
		this.odometer = odometer;
		this.speed = speed;
		this.tripId = tripId;
		this.startTime = startTime;
		this.startDate = startDate;
		this.routeId = routeId;
		this.direction = direction;
		
		//this.associatedStaticTripId = Manager.searchCityManager(city).getTripsPerRoute(routeId).stream()
		//		.filter((t) -> (t.getDirection_id().equals(direction))).collect(Collectors.toList()).getFirst().getTrip_id();
		
	}




	/**
	 * Returns the unique identifier for the vehicle.
	 *
	 * @return the vehicle ID
	 */
	public String getVehicleId() {
		return vehicleId;
	}



	/**
	 * Returns the human-readable label or name for the vehicle.
	 *
	 * @return the vehicle label
	 */
	public String getLabel() {
		return label;
	}



	/**
	 * Returns the identifier of the next stop the vehicle is scheduled to reach.
	 *
	 * @return the next stop ID
	 */
	public String getNextStopId() {
		return nextStopId;
	}



	/**
	 * Returns the epoch timestamp (milliseconds) representing the last update time.
	 *
	 * @return the timestamp of the last update
	 */
	public long getTimestamp() {
		return timestamp;
	}



	/**
	 * Returns the sequence number of the current stop in the trip.
	 *
	 * @return the current stop sequence number
	 */
	public int getCurrentStopSequence() {
		return currentStopSequence;
	}



	/**
	 * Returns the current geographical position of the vehicle.
	 *
	 * @return the vehicle's position as a {@link GeoPosition}
	 */
	public GeoPosition getPosition() {
		return position;
	}



	/**
	 * Returns the name of the city in which the vehicle operates.
	 *
	 * @return the city name
	 */
	public String getCity() {
		return city;
	}



	/**
	 * Returns the current bearing (direction) of the vehicle in degrees.
	 *
	 * @return the vehicle's bearing
	 */
	public double getBearing() {
		return bearing;
	}



	/**
	 * Returns the odometer reading (distance traveled) in kilometers.
	 *
	 * @return the odometer value
	 */
	public double getOdometer() {
		return odometer;
	}



	/**
	 * Returns the current speed of the vehicle in kilometers per hour.
	 *
	 * @return the vehicle's speed
	 */
	public double getSpeed() {
		return speed;
	}


	/*
	/**
	 * Returns the identifier of the associated static trip, determined by route and direction.
	 *
	 * @return the associated static trip ID
	 *\/
	public String getAssociatedStaticTripId() {
		return associatedStaticTripId;
	}
	*/



	/**
	 * Returns the identifier of the current trip assigned to the vehicle.
	 *
	 * @return the trip ID
	 */
	public String getTripId() {
		return tripId;
	}



	/**
	 * Returns the scheduled start time of the trip.
	 *
	 * @return the trip's start time
	 */
	public LocalTime getStartTime() {
		return startTime;
	}



	/**
	 * Returns the scheduled start date of the trip.
	 *
	 * @return the trip's start date
	 */
	public LocalDate getStartDate() {
		return startDate;
	}



	/**
	 * Returns the identifier of the route on which the vehicle is operating.
	 *
	 * @return the route ID
	 */
	public String getRouteId() {
		return routeId;
	}



	/**
	 * Returns the direction of travel for the current trip (e.g., "0" or "1").
	 *
	 * @return the direction of travel
	 */
	public String getDirection() {
		return direction;
	}
	
	
}
