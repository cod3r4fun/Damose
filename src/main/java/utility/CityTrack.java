package utility;

import java.util.Map;


/**
 * Utility class for managing GTFS/GTFS-RT endpoint tracking for supported cities.
 *
 * <p>{@code CityTrack} acts as a centralized registry and reference holder for cities
 * supported by the application, associating each city with its corresponding static GTFS and
 * real-time GTFS-RT feed URLs via {@link Tuple} mappings.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Maintain a read-only map of statically tracked cities and their data sources</li>
 *   <li>Expose an API for retrieving or setting the currently tracked city in the application context</li>
 *   <li>Expose metadata such as the number of cities being tracked</li>
 * </ul>
 *
 * <h2>Usage Pattern</h2>
 * <pre>{@code
 * // Set current operational city context
 * CityTrack.setTrackedCity("Rome");
 *
 * // Access static and RT feed URLs
 * Tuple<String, String> romeFeeds = CityTrack.TRACKEDCITIES.get("Rome");
 * String staticUrl = romeFeeds.getX();  // GTFS URL
 * String realtimeUrl = romeFeeds.getY();  // GTFS-RT URL
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is not thread-safe for concurrent modifications to the tracked city. The
 * {@code trackedCity} field is mutable and should be used with caution in multi-threaded
 * contexts.</p>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class CityTrack {
	
	
    /**
     * A constant unmodifiable mapping of city names to their GTFS static and GTFS-RT URLs.
     *
     * <p>The first component of the {@link Tuple} represents the static GTFS feed URL,
     * while the second represents the GTFS-Realtime vehicle positions feed URL.</p>
     */
	public static final Map<String, Tuple<String, String>> TRACKEDCITIES= 

				Map.of("Rome", 
						new Tuple<String, String>("https://romamobilita.it/sites/default/files/rome_static_gtfs.zip", 
				"https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb")
						, "Berlin", 
						new Tuple<String, String>("https://unternehmen.vbb.de/fileadmin/user_upload/VBB/Dokumente/API-Datensaetze/gtfs-mastscharf/GTFS.zip",
								"https://production.gtfsrt.vbb.de/data")
						
				);
	
	
    /**
     * The currently active city being tracked by the application.
     *
     * <p>This value is mutable and intended to be set at application startup or during
     * reconfiguration.</p>
     */
	private static String trackedCity;
	
	
    /**
     * Sets the name of the currently tracked city.
     *
     * @param city the name of the city to track; must exist in {@link #TRACKEDCITIES}
     */
	public static void setTrackedCity(String city) {
		trackedCity = city;
	}
	
    /**
     * Retrieves the name of the currently tracked city.
     *
     * @return the city currently being tracked
     */
	public static String getTrackedCity() {
		return trackedCity;
	}
	
	
	
    /**
     * The number of cities for which GTFS data sources are statically defined.
     */
	
	public static final int NUMBEROFCITIESTRACKED = TRACKEDCITIES.keySet().size();
}
