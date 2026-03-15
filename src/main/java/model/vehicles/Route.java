package model.vehicles;

import java.io.Serializable;


/**
 * Represents a public transit route, which defines a group of trips that are displayed
 * to riders as a single service.
 * 
 * <p>This class maps directly to a row in the GTFS {@code routes.txt} file. A {@code Route}
 * encapsulates basic route metadata, including its identifier, display name, transport type,
 * and an optional map visualization reference (e.g., a color code or URL).
 *
 * <p>Each route may include many {@link Trip} instances, but that relationship is managed externally.
 *
 * <p><strong>GTFS Fields Represented:</strong>
 * <ul>
 *   <li>{@code route_id} – Unique identifier for the route</li>
 *   <li>{@code route_short_name} – Short name for display purposes (e.g., "22", "Red")</li>
 *   <li>{@code route_type} – Mode of transportation (e.g., bus, tram, subway)</li>
 *   <li>{@code route_url} or visual tag – Used here as {@code mapVisualiser} for visual reference</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * Route route = new Route("route_12", "Blue Line", "1", "URL...");
 * System.out.println("Route name: " + route.getRouteName());
 * }</pre>
 *
 * @see model.vehicles.Trip
 * @see <a href="https://gtfs.org/schedule/reference/#routestxt">GTFS Reference: routes.txt</a>
 * 
 * @author	Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Route implements Serializable {
	
    private static final long serialVersionUID = 1L;

    /**
     * GTFS-defined unique identifier for this route.
     * Typically corresponds to the {@code route_id} in {@code routes.txt}.
     */
    private final String routeId;

    /**
     * Display-oriented short name for the route (e.g., "22", "Blue").
     * Maps to {@code route_short_name} in GTFS.
     */
    private final String route_short_name;

    /**
     * Transport mode identifier, often numerical as per GTFS (e.g., "0" for tram, "1" for subway, "3" for bus).
     * Corresponds to {@code route_type}.
     */
    private final String type;

    /**
     * Visual representation of the route for use in mapping or UI elements.
     * This could be a hex color code, image URL, or other visual asset identifier.
     * Maps conceptually to {@code route_color} or {@code route_url}.
     */
    private final String mapVisualiser;

    /**
	/**
     * Constructs a new {@code Route} with the specified metadata.
     *
     * @param routeId        unique identifier for the route; must not be {@code null}
     * @param route_short    display name or shorthand label for the route
     * @param type           GTFS-defined transport mode type (e.g., "1" = subway, "3" = bus)
     * @param mapVisualiser  optional visual representation (e.g., color or URL) for use in maps and UIs
     * @throws IllegalArgumentException if {@code routeId} is {@code null}
     */

    public Route(String routeId, String route_short, String type, String mapVisualiser) {
    	
    	if (routeId == null) throw new IllegalArgumentException("route_id cannot be null");
        this.routeId = routeId;
        this.route_short_name = route_short;
        this.type = type;
        this.mapVisualiser = mapVisualiser;
    }

    /**
     * Retrieves the unique identifier for the route.
     *
     * @return the {@code routeId}
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Retrieves the route's short name used for display purposes.
     *
     * @return a user-friendly string label for the route
     */
    public String getRouteName() {
        return route_short_name;
    }

    /**
     * Returns the transport mode type associated with this route.
     *
     * @return a string code (e.g., "0", "1", "3") identifying the type of transit
     */
    public String getType() {
        return type;
    }

    /**
     * Provides a visual indicator or asset reference associated with the route.
     *
     * @return a visual element string (e.g., color code or image URL)
     */
    public String getMapVisualiser() {
        return mapVisualiser;
    }
    
    /**
     * Retrieves the serial version UID used for serialization compatibility.
     *
     * @return the {@code serialVersionUID}
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    
    /**
     * Determines equality between this {@code Route} and another object.
     * Two routes are considered equal if their {@code routeId} values match.
     *
     * @param s the object to compare against
     * @return {@code true} if the other object is a {@code Route} with the same {@code routeId}, {@code false} otherwise
     */
    @Override
    public boolean equals(Object s) {
    	if (s instanceof Route route) {
    		return this.routeId.equals(route.getRouteId());
    	}
    	return false;
    }
    
}
