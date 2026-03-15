/**
 * The root package for the transit application’s domain model, encompassing all core entities,
 * management components, analysis engines, and search facilities used to represent and operate
 * on public transportation data.
 *
 * <p><strong>Overview:</strong>
 * <ul>
 *   <li><b>Vehicle Modeling:</b>  
 *     <ul>
 *       <li>{@link model.vehicles.Vehicle} – Represents a real‐time tracked vehicle, including its position, route ID, direction, and timestamp.</li>
 *       <li>{@link model.vehicles.Stop} – Models a physical boarding/alighting point with GTFS metadata (ID, name, geolocation) and activation state.</li>
 *       <li>{@link model.vehicles.Route} – Encapsulates GTFS route data (ID, short name, type, visual tag) and maps to {@code routes.txt}.</li>
 *       <li>{@link model.vehicles.Trip} – Immutable representation of a scheduled trip, linking a route, service ID, headsign, and direction.</li>
 *       <li>{@link model.vehicles.StopTime} – Immutable entry pairing a trip with arrival/departure times and stop sequence.</li>
 *       <li>{@link model.vehicles.CalendarDates} - Immutable entry pairing a serviceId to a scheduled calendar. </li>
 *     </ul>
 *   </li>
 *
 *   <li><b>City Data Management:</b>
 *     <ul>
 *       <li>{@link model.vehicles.Manager} – Static factory and registry for {@link model.vehicles.Manager.CityManager} instances per city.</li>
 *       <li>{@link model.vehicles.Manager.CityManager} – Holds all GTFS and real‐time data for one city, offering thread‐safe registration of live vehicles, accessors for stops/routes/trips/service dates, and combined queries (e.g., stops per route, trips per stop).</li>
 *       <li>{@link model.vehicles.GTFSData} – Internal loader that parses and indexes GTFS static files from a {@link utility.TemporaryDataHolder} into optimized maps for quick lookups of routes, stops, trips, and calendar dates.</li>
 *       <li>{@link model.vehiclePositionUpdater} - manager for all "database"-data changes inside the model. <li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Real-Time Analysis Engines:</b>
 *     <ul>
 *       <li>{@link model.transitEngine.VehicleTransitEngine} – Interface defining expected‐arrival‐time calculations.</li>
 *       <li>{@link model.transitEngine.StandardVehicleAnalyser} – Implements logic to estimate arrival times by combining real‐time GPS position, historical schedule data, delay reconstruction, and GTFS stop sequences.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Vehicle Positioning:</b>
 *     <ul>
 *       <li>{@link model.transitEngine.VehiclePositionAnalyzer} – Interface for retrieving vehicle locations on a route.</li>
 *       <li>{@link model.transitEngine.SimpleVehiclePositionAnalyzer} – Provides both real‐time (from live feeds) and static (schedule‐based) vehicle position estimates, including “closest stop” heuristics.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Search Services:</b>
 *     <ul>
 *       <li>{@link model.searchEngine.Searcher} – Interface defining searches for stop‐time schedules by route, by stop, or by route+stop.</li>
 *       <li>{@link model.searchEngine.simpleSearch} – Concrete implementation that dynamically switches between static schedule queries and real‐time expected‐arrival lookups via {@link model.transitEngine.StandardVehicleAnalyser}.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>User Account Management (via sub-package model.user):</b>
 *     <ul>
 *       <li>{@link model.user.Account} and {@link model.user.Favorites} – Represent user credentials (securely hashed) and per‐user favorites (routes & stops) with serialization support.</li>
 *       <li>{@link model.user.AccountManager} – Singleton service that handles signup, login (with {@link utility.PasswordUtils}), session state, and persistence through {@link model.user.AccountRepository} implementations.</li>
 *       <li>{@link model.user.FileAccountRepository} – Default encrypted‐file backend for {@code AccountRepository}, using {@link utility.CryptoUtils} to secure serialized accounts.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Notable Utility Dependencies:</strong>
 * <ul>
 *   <li>{@link utility.Triple} – General‐purpose tuple for grouping stops with arrival/departure times.</li>
 *   <li>{@link utility.TransitUtils} – Algorithms for travel‐time estimation based on geospatial distances and route types.</li>
 *   <li>{@link utility.TemporaryDataHolder} – Staging area for raw GTFS data before indexing into {@link model.vehicles.GTFSData}.</li>
 *   <li>{@link controller.DateController} – Central reference for current application date logic (e.g., historical vs. future trips).</li>
 *   <li>{@link controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker} – Determines whether to use live or static data at query time.</li>
 * </ul>
 *
 * <p>This package structure ensures a clean separation of concerns between data modeling,
 * real-time analysis, and user‐facing search functionality, facilitating maintainable,
 * testable, and extensible transit application development.
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * 
 */
package model;
