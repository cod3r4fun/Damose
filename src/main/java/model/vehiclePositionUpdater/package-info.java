/**
 * Package model.vehiclePositionUpdater
 * 
 * Provides tools to parse, load, and update both static and real-time GTFS (General Transit Feed Specification) data.
 * The central class {@link Updater} facilitates the integration of GTFS data into the system's {@code CityManager}, 
 * enabling vehicle tracking and route management for a specific city.
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Loading and parsing GTFS static files (e.g., stops, routes, trips, calendar_dates, stop_times)</li>
 *   <li>Updating vehicle positions from GTFS real-time feeds</li>
 *   <li>Maintaining a synchronized data manager per city</li>
 * </ul>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */

package model.vehiclePositionUpdater;