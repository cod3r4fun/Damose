package model.vehicles;

/**
 * Represents a single scheduled trip in a public transit system as defined by the
 * GTFS (General Transit Feed Specification) format.
 * 
 * <p>The {@code Trip} class encapsulates key data fields from the {@code trips.txt}
 * file, such as the route association, service schedule, unique trip identifier,
 * destination headsign, and direction.
 * 
 * <p>This class is immutable and thread-safe. All fields are {@code final}, and 
 * values are set only through the constructor.
 *
 * <p><strong>Typical Usage:</strong>
 * <pre>{@code
 * Trip t = new Trip("route_100", "weekday", "trip_456", "Downtown", "0");
 * System.out.println(t.getTrip_id());
 * }</pre>
 *
 * <p>Fields align with:
 * <ul>
 *   <li>{@code route_id} — Refers to the route this trip is part of</li>
 *   <li>{@code service_id} — Indicates when the trip runs (e.g., weekdays, weekends)</li>
 *   <li>{@code trip_id} — Uniquely identifies a trip</li>
 *   <li>{@code trip_headsign} — Describes the destination or direction shown to passengers</li>
 *   <li>{@code direction_id} — Indicates travel direction (e.g., 0 = outbound, 1 = inbound)</li>
 * </ul>
 *
 * @see <a href="https://gtfs.org/schedule/reference/#tripstxt">GTFS Reference: trips.txt</a>
 * @see model.vehicles.Route
 * @see model.stops.Stop
 * @see model.time.StopTime
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Trip {

    /** The route ID to which this trip belongs (from {@code routes.txt}). */
    private final String route_id;

    /** The service ID that defines when the trip is active (from {@code calendar.txt} or {@code calendar_dates.txt}). */
    private final String service_id;

    /** Unique identifier for the trip (from {@code trips.txt}). */
    private final String trip_id;

    /** Text shown to passengers describing the trip's destination (e.g., "Downtown"). */
    private final String trip_headsign;

    /** Direction of travel: typically {@code 0} for one direction and {@code 1} for the opposite. */
    private final String direction_id;

    /**
     * Constructs an immutable {@code Trip} object with the specified values.
     *
     * @param route_id        the ID of the route this trip belongs to
     * @param service_id      the service schedule ID indicating operational days
     * @param trip_id         the unique identifier of the trip
     * @param trip_headsign   the passenger-facing destination text
     * @param direction_id    the direction ID (typically "0" or "1")
     */
    public Trip(String route_id, String service_id, String trip_id, 
                String trip_headsign, String direction_id) {
    	
    	if (route_id == null) throw new IllegalArgumentException("route_id cannot be null");
    	if( service_id == null) throw new IllegalArgumentException("service_id cannot be null");
    	if (trip_id == null) throw new IllegalArgumentException("trip_id cannot be null");
        this.route_id = route_id;
        this.service_id = service_id;
        this.trip_id = trip_id;
        this.trip_headsign = trip_headsign;
        this.direction_id = direction_id;
    }

    /** @return the route ID for this trip */
    public String getRoute_id() {
        return route_id;
    }

    /** @return the service schedule ID for this trip */
    public String getService_id() {
        return service_id;
    }

    /** @return the unique trip ID */
    public String getTrip_id() {
        return trip_id;
    }

    /** @return the text shown to passengers describing this trip */
    public String getTrip_headsign() {
        return trip_headsign;
    }

    /** @return the direction ID (usually "0" or "1") */
    public String getDirection_id() {
        return direction_id;
    }
}
