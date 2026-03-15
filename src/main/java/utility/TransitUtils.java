// written with chatGPT's help

package utility;

import java.time.Duration;

import org.jxmapviewer.viewer.GeoPosition;



/**
 * Utility class providing geographical and transit-related calculations.
 * <p>
 * This class includes logic for computing great-circle distances using the Haversine formula
 * and estimating public transport travel durations based on mode-specific average speeds.
 * </p>
 *
 * <h2>Supported Transit Modes</h2>
 * The {@code estimateTravelTime} method uses GTFS-like route type codes:
 * <ul>
 *   <li>{@code 0} - Tram/Streetcar</li>
 *   <li>{@code 1} - Metro/Subway</li>
 *   <li>{@code 2} - Rail/Train</li>
 *   <li>{@code 3} - Bus</li>
 * </ul>
 * Unknown route types default to bus speed.
 *
 * <p><b>Assumptions:</b> Travel time is based on direct line-of-sight distance and
 * average system-wide speeds. It does not account for traffic, stops, elevation, or real-time delays.</p>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class TransitUtils {

	
    /** Average bus speed in meters per second (~20 km/h). */
	private static final double AVERAGE_BUS_SPEED_MPS   = 5.5;
	
	 /** Average tram/streetcar speed in meters per second (~25 km/h). */
	private static final double AVERAGE_TRAM_SPEED_MPS  = 7.0;
	
	/** Average metro/subway speed in meters per second (~40 km/h). */
	private static final double AVERAGE_METRO_SPEED_MPS = 11.1;
	
    /** Average regional rail/train speed in meters per second (~60 km/h). */
	private static final double AVERAGE_RAIL_SPEED_MPS  = 16.7;
	
	
    /**
     * Computes the great-circle distance between two geographic positions using
     * the Haversine formula.
     *
     * <p>This method assumes a spherical Earth and is accurate for most transit applications
     * within urban and suburban ranges.</p>
     *
     * @param p1 the first geographic position
     * @param p2 the second geographic position
     * @return the distance between the two positions, in meters
     */
    public static double haversineDistance(GeoPosition p1, GeoPosition p2) {
        final int EARTH_RADIUS = 6371000; 

        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.pow(Math.sin(dlat / 2), 2)
                 + Math.cos(lat1) * Math.cos(lat2)
                 * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    
    /**
     * Estimates the travel duration between two locations based on transport mode and distance.
     *
     * <p>Speed assumptions are hardcoded per mode and may be fine-tuned to reflect city-specific data.</p>
     *
     * @param from the starting geographic position
     * @param to the destination geographic position
     * @param routeType the transport mode code (0=tram, 1=metro, 2=rail, 3=bus)
     * @return a {@link Duration} object representing the estimated travel time
     */
    public static Duration estimateTravelTime(GeoPosition from, GeoPosition to, int routeType) {
        double distanceMeters = haversineDistance(from, to);
        double averageSpeed;

        switch (routeType) {
            case 0 -> averageSpeed = AVERAGE_TRAM_SPEED_MPS;
            case 1 -> averageSpeed = AVERAGE_METRO_SPEED_MPS;
            case 2 -> averageSpeed = AVERAGE_RAIL_SPEED_MPS;
            case 3 -> averageSpeed = AVERAGE_BUS_SPEED_MPS;
            default -> averageSpeed = AVERAGE_BUS_SPEED_MPS; // fallback
        }

        double timeSeconds = distanceMeters / averageSpeed;
        return Duration.ofSeconds((long) timeSeconds);
    }

}
