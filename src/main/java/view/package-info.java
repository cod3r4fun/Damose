/**
 * Provides the user interface components and classes for the SmartTransit Viewer application.
 * <p>
 * This package contains the main UI controller, panels, and map visualization classes
 * responsible for rendering and managing the application's graphical interface.
 * It integrates Swing components with geographic visualization via the JXMapViewer library
 * and handles user interactions related to transit data display, route navigation,
 * and account management.
 * </p>
 * 
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link view.MainUIInitializer} - The singleton controller responsible for initializing
 *       and orchestrating the main application window and tabs.</li>
 *   <li>{@link view.StopsPanel} - UI component displaying transit stops and enabling search.</li>
 *   <li>{@link view.LinesPanel} - UI component for browsing transit lines.</li>
 *   <li>{@link view.MapPanel} - Contains the map viewer and handles map-related rendering.</li>
 *   <li>{@link view.FavoritesPanel} - Manages user favorite stops and routes.</li>
 *   <li>Additional supporting UI and map helper classes such as route painters and waypoints.</li>
 * </ul>
 * 
 * <h2>Dependencies</h2>
 * <ul>
 *   <li>JXMapViewer for geographic mapping and waypoint visualization.</li>
 *   <li>Java Swing for GUI components.</li>
 *   <li>Backend models and controllers for data retrieval and business logic integration.</li>
 * </ul>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */


package view;