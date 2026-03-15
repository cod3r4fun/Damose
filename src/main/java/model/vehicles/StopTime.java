package model.vehicles;

import java.time.LocalTime;


/**
 * Represents a stop time entry in a public transit trip based on the GTFS specification.
 * 
 * <p>This class corresponds to a row in the {@code stop_times.txt} GTFS file. Each
 * {@code StopTime} object defines the arrival and departure times at a particular stop
 * for a specific trip, as well as the position of the stop within the trip sequence.
 *
 * <p>This class is immutable and thread-safe. All fields are {@code final}.
 *
 * <p><strong>GTFS fields represented:</strong>
 * <ul>
 *   <li>{@code trip_id} — Identifies the trip this stop time belongs to</li>
 *   <li>{@code arrival_time} — Time the vehicle arrives at the stop</li>
 *   <li>{@code departure_time} — Time the vehicle leaves the stop</li>
 *   <li>{@code stop_id} — Identifies the stop</li>
 *   <li>{@code stop_sequence} — Order of the stop in the trip</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * StopTime st = new StopTime("trip_101", LocalTime.of(8, 30), LocalTime.of(8, 31), "stop_22", 5);
 * System.out.println("Arrives at: " + st.getArrival_time());
 * }</pre>
 *
 * @see model.vehicles.Trip
 * @see model.stops.Stop
 * @see <a href="https://gtfs.org/schedule/reference/#stop_timestxt">GTFS Reference: stop_times.txt</a>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class StopTime {

    /** Unique ID of the trip to which this stop time belongs. */
    private final String trip_id;

    /** Time when the vehicle arrives at this stop. */
    private final LocalTime arrival_time;

    /** Time when the vehicle departs from this stop. */
    private final LocalTime departure_time;

    /** Unique ID of the stop where this event takes place. */
    private final String stop_id;

    /** Position of the stop in the sequence of stops for the trip (starting from 1). */
    private final int stop_sequence;

    /**
     * Constructs a new {@code StopTime} object with the specified details.
     *
     * @param trip_id         the ID of the trip
     * @param arrival_time    the time the vehicle arrives at the stop
     * @param departure_time  the time the vehicle departs from the stop
     * @param stop_id         the ID of the stop
     * @param stop_sequence   the sequence number of the stop in the trip
     */
    public StopTime(String trip_id, LocalTime arrival_time,
                    LocalTime departure_time, String stop_id, int stop_sequence) {
    	
    	if (trip_id == null)  throw new IllegalArgumentException("trip_id cannot be null");
    	if(stop_id == null) throw new IllegalArgumentException("stop_id cannot be null");
        this.trip_id = trip_id;
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
        this.stop_id = stop_id;
        this.stop_sequence = stop_sequence;
    }

    /**
     * @return the trip ID this stop time is associated with
     */
    public String getTrip_id() {
        return trip_id;
    }

    /**
     * @return the arrival time at the stop
     */
    public LocalTime getArrival_time() {
        return arrival_time;
    }

    /**
     * @return the departure time from the stop
     */
    public LocalTime getDeparture_time() {
        return departure_time;
    }

    /**
     * @return the stop ID
     */
    public String getStop_id() {
        return stop_id;
    }

    /**
     * @return the sequence order of the stop in the trip
     */
    public int getStop_sequence() {
        return stop_sequence;
    }
}
