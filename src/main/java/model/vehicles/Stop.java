package model.vehicles;

import java.io.Serializable;

import org.jxmapviewer.viewer.GeoPosition;


/**
 * Represents a physical transit stop or station where passengers board or alight from vehicles.
 * 
 * <p>This class is designed to model an individual stop as described in the GTFS {@code stops.txt} file,
 * enriched with geospatial data for integration with mapping libraries such as JXMapViewer.
 *
 * <p>Each {@code Stop} object contains:
 * <ul>
 *   <li>{@code name} – Human-readable stop name (e.g., "Main Street Station")</li>
 *   <li>{@code stopId} – Unique identifier used to correlate with GTFS stop records</li>
 *   <li>{@code position} – Geographical coordinates of the stop ({@link GeoPosition})</li>
 *   <li>{@code active} – A flag indicating whether the stop is currently in use</li>
 * </ul>
 *
 * <p>Stops can be suspended or reactivated via {@link #suspend()} and {@link #reactivate()} respectively.
 * The {@code active} flag is useful for handling temporary closures or filtering inactive locations in maps.
 *
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * Stop stop = new Stop("Central Station", "stop_001", new GeoPosition(48.8566, 2.3522));
 * stop.suspend();
 * if (!stop.isActive()) {
 *     System.out.println(stop.getName() + " is temporarily inactive.");
 * }
 * }</pre>
 *
 * @see org.jxmapviewer.viewer.GeoPosition
 * @see model.vehicles.StopTime
 * @see <a href="https://gtfs.org/schedule/reference/#stopstxt">GTFS Reference: stops.txt</a>
 * 
 * @author	Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Stop implements Serializable{
	
	private static final long serialVersionUID = 1L;

    /** Descriptive name of the stop (e.g., "Broadway & 3rd Ave"). */
    private String name;

    /**
     * Unique identifier for this stop, corresponding to the GTFS {@code stop_id} value.
     */
    private final String stopId;

    /**
     * Geospatial coordinates of the stop represented by a {@link GeoPosition} instance.
     * Used for visual mapping and spatial queries.
     */
    private final GeoPosition position;

    /**
     * Operational status flag indicating whether the stop is currently active (available for service).
     */
    private boolean active;

    /**
     * Initializes a new instance of {@code Stop} with the specified name, identifier, and location.
     *
     * @param name     the display name of the stop; must not be {@code null}
     * @param stopId   the unique GTFS or system-assigned identifier; must not be {@code null}
     * @param position the geospatial coordinates of the stop; must not be {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public Stop(String name, String stopId, GeoPosition position) {
    	
    	if (name == null) throw new IllegalArgumentException("name cannot be null");
    	if(stopId == null) throw new IllegalArgumentException("stopId cannot be null");
    	if(position==null) throw new IllegalArgumentException("the position cannot be null");
        this.name = name;
        this.stopId = stopId;
        this.position = position;
        this.active = true;
    }

    /**
     * Suspends the stop by setting its {@code active} status to {@code false}.
     * Useful for modeling temporary closures or inactive stations.
     */
    public void suspend() {
        active = false;
    }

    /**
     * Reactivates the stop by setting its {@code active} status to {@code true}.
     */
    public void reactivate() {
        active = true;
    }

    /**
     * @return the human-readable name of the stop
     */
    public String getName() {
        return name;
    }

    /**
     * @return the unique identifier of the stop
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * Retrieves the geographical coordinates of the stop.
     *
     * @return a {@link GeoPosition} object representing the stop's location
     */
    public GeoPosition getPosition() {
        return position;
    }

    /**
     * @return {@code true} if the stop is active, {@code false} if suspended
     */
    public boolean isActive() {
        return active;
    }
    
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Returns a string representation of the stop, including its name, ID, and coordinates.
     *
     * @return formatted string describing the stop
     */
    @Override
    public String toString() {
        return "name: " + name + "; stopId: " + stopId + "; position: " + position.toString();
    }
    
    /**
     * Compares this stop with another object for equality. Two stops are considered equal if
     * their name, ID, and position match.
     *
     * @param s the object to compare with
     * @return {@code true} if the objects represent the same stop; {@code false} otherwise
     */
    @Override
    public boolean equals(Object s) {
    	if (s instanceof Stop stop) {
    		return this.name.equals(stop.getName()) && this.stopId.equals(stop.getStopId()) && this.position.equals(stop.getPosition());
    	}
    	return false;
    }
}

