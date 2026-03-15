// written with chatGPT's help

package view;

import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import utility.SysConstants;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;

import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;

import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.viewer.DefaultTileFactory;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import javax.swing.event.MouseInputListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;

import model.transitEngine.*;



/**
 * A Swing JPanel that encapsulates the interactive map view for the SmartTransit Viewer application.
 * <p>
 * This component integrates the JXMapViewer library to display an OpenStreetMap-based map, enriched
 * with overlays for transit stops and vehicle locations. It supports zoom controls, mode switching
 * between viewing stops and vehicles, and clickable waypoints that trigger detailed stop views.
 * </p>
 * 
 * <p><b>Features include:</b></p>
 * <ul>
 *   <li>Dynamic waypoint rendering based on zoom level and selected view mode (stops or vehicles).</li>
 *   <li>Automatic periodic refresh of vehicle and stop positions every 30 seconds.</li>
 *   <li>Map interaction handlers for panning, zooming, and waypoint click detection.</li>
 *   <li>Prefetching of map tiles in a background thread to improve user experience.</li>
 *   <li>Configurable zoom limits and responsive UI controls.</li>
 * </ul>
 * 
 * <p><b>Implementation details:</b></p>
 * <ul>
 *   <li>Uses {@link JXMapViewer} for map rendering and tile management with local caching.</li>
 *   <li>Waypoints are represented as either {@link ClickableWaypoint} or {@link VehicleWaypoint} 
 *       objects, with custom painters and renderers.</li>
 *   <li>View modes are toggled via a combo box UI allowing switching between stops and vehicles.</li>
 *   <li>Refreshes vehicle positions by delegating to {@link VehiclePositionAnalyzer} implementations.</li>
 *   <li>Thread safety is maintained by using Swing timers and the Event Dispatch Thread for UI updates.</li>
 * </ul>
 * 
 * <p>This class depends on the city-level transit data managed by {@link Manager.CityManager} for
 * retrieving stops and routes. It communicates with the main UI controller to display stop details
 * when waypoints are clicked.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class MapPanel extends JPanel {
    /**
     * Zoom threshold below which stops are displayed in stop view mode.
     */
	private static final int STOP_DISPLAY_ZOOM_THRESHOLD = 3;
	
    /**
     * Zoom threshold below which line stops are displayed (currently unused).
     */
	private static final int LINE_STOP_DISPLAY_ZOOM_THRESHOLD = 5;
	
    /**
     * Enum representing the current map display mode: either showing stops or vehicles.
     */
	private enum ViewMode { STOP_VIEW, VEHICLE_VIEW }
	
    /**
     * The defualt active map display mode.
     */
	private ViewMode currentMode = ViewMode.STOP_VIEW;
	
    /**
     * The set of clickable waypoints currently displayed on the map.
     */
	private Set<ClickableWaypoint> clickableWaypoints;
	
    /**
     * Reference to the city manager providing access to stops, routes, and vehicles data.
     */
    private final Manager.CityManager cityManager;
    
    /**
     * The JXMapViewer instance used for rendering the map and overlays.
     */
    private final JXMapViewer mapViewer;
    
    /**
     * Timer that triggers periodic refreshes of map overlays.
     */
    private Timer refreshTimer;
    
    /**
     * Analyzer component responsible for determining vehicle positions for display.
     */
    private VehiclePositionAnalyzer vpa = new SimpleVehiclePositionAnalyzer();

    /**
     * Refresh interval in milliseconds for updating map overlays.
     */
    private static final int REFRESH_INTERVAL_MS = 30_000;

    // Zoom limits for JXMapViewer
    private static final int MIN_ZOOM = 0;   
    private static final int MAX_ZOOM = 19;  

    
    
    /**
     * Constructs a MapPanel tied to a specific city manager, initializing the map viewer
     * and user interface controls.
     *
     * @param cityManager the city manager providing transit data and operations
     */
    public MapPanel(Manager.CityManager cityManager) {
        this.cityManager = cityManager;
        this.mapViewer = new JXMapViewer();
        this.clickableWaypoints = new HashSet<>();

        setLayout(new BorderLayout());
        add(mapViewer, BorderLayout.CENTER);
        add(buildZoomControls(), BorderLayout.SOUTH);

        initMap();
        startAutoRefresh();
    }
    
    
    /**
     * Returns the underlying JXMapViewer instance used by this panel.
     *
     * @return the JXMapViewer displaying the map and overlays
     */
    public JXMapViewer getMapViewer() {
        return mapViewer;
    }
    
    /*
    public void addTemporaryRouteOverlay(List<GeoPosition> positions, List<Stop> routeStops) {
        if (positions == null || positions.isEmpty()) return;

        // Delay the overlay setup by 300ms to allow map to initialize
        new Timer(300, e -> {
            RoutePainter routePainter = new RoutePainter(positions, Color.RED, 3f);

            Set<ClickableWaypoint> routeWaypoints = routeStops.stream()
                .map(ClickableWaypoint::new)
                .collect(Collectors.toSet());

            WaypointPainter<ClickableWaypoint> stopPainter = new WaypointPainter<>();
            stopPainter.setWaypoints(routeWaypoints);

            CompoundPainter<JXMapViewer> combined = new CompoundPainter<>();
            combined.addPainter(routePainter);
            combined.addPainter(stopPainter);

            mapViewer.setOverlayPainter(combined);
            mapViewer.repaint();

            zoomToFitPositions(positions);

            ((Timer) e.getSource()).stop();
        }).start();

        new Timer(15000, e -> {
            updateWaypoints(); // Reset to normal waypoints only
            ((Timer) e.getSource()).stop();
        }).start();
    }


    
    
    public void highlightStop(Stop stop) {
        if (stop == null) return;

        // Delay the highlighting by 300ms to let map initialize
        new Timer(300, e -> {
            GeoPosition pos = new GeoPosition(stop.getPosition().getLatitude(), stop.getPosition().getLongitude());

            WaypointPainter<Waypoint> painter = new WaypointPainter<>();
            painter.setWaypoints(Set.of(new DefaultWaypoint(pos)));

            CompoundPainter<JXMapViewer> combined = new CompoundPainter<>();
            combined.addPainter(painter);
            mapViewer.setOverlayPainter(combined);
            mapViewer.setAddressLocation(pos);
            mapViewer.setCenterPosition(pos);
            mapViewer.setZoom(3);
            mapViewer.repaint();

            ((Timer) e.getSource()).stop();

            // Reset waypoints after 5 seconds
            new Timer(5000, ev -> {
                updateWaypoints();
                ((Timer) ev.getSource()).stop();
            }).start();
        }).start();
    }
	*/
    
    
    /**
     * Stops the automatic refresh timer responsible for periodic map updates.
     * This method should be called when the panel is no longer displayed to
     * prevent resource leaks.
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
    
    


    /**
     * Builds and returns the panel containing zoom controls and the view mode selector.
     * This includes buttons to zoom in/out and a combo box to switch between stops and vehicles views.
     *
     * @return a JPanel containing zoom controls and mode selector UI elements
     */
    private JPanel buildZoomControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton zoomInButton = new JButton("+");
        zoomInButton.setToolTipText("Zoom In");
        zoomInButton.addActionListener(e -> {
            int currentZoom = mapViewer.getZoom();
            if (currentZoom > MIN_ZOOM) {
                mapViewer.setZoom(currentZoom - 1);
            }
        });

        JButton zoomOutButton = new JButton("-");
        zoomOutButton.setToolTipText("Zoom Out");
        zoomOutButton.addActionListener(e -> {
            int currentZoom = mapViewer.getZoom();
            if (currentZoom < MAX_ZOOM) {
                mapViewer.setZoom(currentZoom + 1);
            }
        });
        
        String[] options = {"Stops View", "Vehicles View"};
        JComboBox<String> viewSelector = new JComboBox<>(options);
        viewSelector.addActionListener(e -> {
            String selected = (String) viewSelector.getSelectedItem();
            currentMode = selected.equals("Stops View") ? ViewMode.STOP_VIEW : ViewMode.VEHICLE_VIEW;
            updateWaypoints();
        });
        panel.add(viewSelector);
        panel.add(zoomInButton);
        panel.add(zoomOutButton);
        return panel;
    }

    
    /**
     * Initializes the map viewer, setting tile factories, cache directories,
     * default zoom and center, and prepares map interaction listeners.
     */
    private void initMap() {
        OSMTileFactoryInfo osmInfo = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(osmInfo);

        File cacheDir = new File(SysConstants.CURRENTDIR, ".jxmapviewer");
        FileBasedLocalCache cache = new FileBasedLocalCache(cacheDir, false); // false = don't expire

        tileFactory.setLocalCache(cache);

        mapViewer.setTileFactory(tileFactory);

        mapViewer.setZoom(5);
        GeoPosition cityCenter = calculateCityCenter();
        mapViewer.setAddressLocation(cityCenter);

        setupMapInteractions();

        updateWaypoints();
        startTilePrefetchThread();
    }


    
    /**
     * Calculates the geographic center of the city based on the average latitude and longitude
     * of all stops managed by the city manager. Defaults to Milan's coordinates if no stops exist.
     *
     * @return the computed GeoPosition representing the city center
     */
    private GeoPosition calculateCityCenter() {
        List<Stop> stops = cityManager.getAllStops();
        if (stops.isEmpty()) {
            return new GeoPosition(45.4642, 9.19);
        }
        double avgLat = stops.stream()
                .mapToDouble(s -> s.getPosition().getLatitude())
                .average()
                .orElse(45.4642);
        double avgLon = stops.stream()
                .mapToDouble(s -> s.getPosition().getLongitude())
                .average()
                .orElse(9.19);
        return new GeoPosition(avgLat, avgLon);
    }

    
    /**
     * Updates the displayed waypoints on the map based on the current view mode and zoom level.
     * In STOP_VIEW mode, stops are shown or hidden depending on zoom thresholds.
     * In VEHICLE_VIEW mode, current vehicle positions for all routes are displayed.
     */
    private void updateWaypoints() {

        if (currentMode == ViewMode.STOP_VIEW) {
            int zoom = mapViewer.getZoom();

            if (zoom <= STOP_DISPLAY_ZOOM_THRESHOLD) {
                clickableWaypoints = cityManager.getAllStops().stream()
                    .map(ClickableWaypoint::new)
                    .collect(Collectors.toSet());
            } else {
                clickableWaypoints.clear();
            }

            WaypointPainter<ClickableWaypoint> stopPainter = new WaypointPainter<>();
            stopPainter.setWaypoints(clickableWaypoints);
            mapViewer.setOverlayPainter(stopPainter);
        } else {
        	List<Route> routes =  cityManager.getAllRoutes();
        	List<VehicleWaypoint> vehicleWaypoints = new ArrayList<>();
        	for (var route: routes) {
        		if (!cityManager.routeHasDirection(route.getRouteId())) {
        			vehicleWaypoints.addAll(vpa.findVehiclePositionsForRoute(route.getRouteId(), cityManager).stream().map(pos -> new VehicleWaypoint(pos,
                            "Line " + route.getRouteId())) .collect(Collectors.toList()));
        		} else {
        			vehicleWaypoints.addAll(vpa.findVehiclePositionsForRouteAndDirection(route.getRouteId(),"0", cityManager).stream()
        		            .map(pos -> new VehicleWaypoint(pos, "Line " + route.getRouteId() + " → " + "0")).collect(Collectors.toList()));
        			vehicleWaypoints.addAll(vpa.findVehiclePositionsForRouteAndDirection(route.getRouteId(),"1", cityManager).stream()
        		            .map(pos -> new VehicleWaypoint(pos, "Line " + route.getRouteId() + " → " + "1")).collect(Collectors.toList()));
        		}
        	}

            WaypointPainter<VehicleWaypoint> vehiclePainter = new WaypointPainter<>();
            vehiclePainter.setWaypoints(new HashSet<>(vehicleWaypoints));
            vehiclePainter.setRenderer(new VehicleWaypointRenderer());
            
            CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
            compoundPainter.addPainter(vehiclePainter);

            mapViewer.setOverlayPainter(compoundPainter);
        }

        if (MasterConnectionStatusChecker.getSingleton().isConnectionActive())
        mapViewer.repaint();
        
        

        
    }


    /**
     * Configures map interactions including panning, zooming via mouse wheel,
     * centering on double-click, and detecting waypoint clicks.
     */
    private void setupMapInteractions() {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        mapViewer.addPropertyChangeListener("zoom", evt -> updateWaypoints());

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point mousePos = e.getPoint();
                for (ClickableWaypoint wp : clickableWaypoints) {
                    Point2D geoPixel = mapViewer.getTileFactory().geoToPixel(wp.getPosition(), mapViewer.getZoom());
                    Point viewportPos = mapViewer.getViewportBounds().getLocation();

                    int x = (int) geoPixel.getX() - viewportPos.x;
                    int y = (int) geoPixel.getY() - viewportPos.y;

                    Rectangle rect = new Rectangle(x - 10, y - 10, 20, 20);
                    if (rect.contains(mousePos)) {
                        onWaypointClicked(wp);
                        break;
                    }
                }
            }
        });
    }
    
    
    
    /**
     * Handles the action triggered when a clickable waypoint is selected by the user.
     * This typically involves instructing the main UI controller to display detailed stop information.
     *
     * @param wp the ClickableWaypoint that was clicked
     */
    private void onWaypointClicked(ClickableWaypoint wp) {
        MainUIInitializer.getInstance().showStop(wp.getStop().getName());
    }
    
    
    // to use in automatic switch later (potentially) 
    
    
    /**
     * Adjusts the map zoom and center to fit the specified list of geographic positions.
     * This method attempts to calculate the bounding box enclosing the given positions
     * and centers the map accordingly, setting an appropriate zoom level.
     * 
     * (currently unused)
     *
     * @param positions a list of GeoPositions to fit into the map viewport
     */
    private void zoomToFitPositions(List<GeoPosition> positions) {
        if (positions == null || positions.isEmpty()) return;

        Rectangle viewport = mapViewer.getViewportBounds();
        if (viewport == null || viewport.getWidth() == 0 || viewport.getHeight() == 0) {
            System.out.println("Viewport size not ready. Defer zoom.");
            return;  // Viewport size not ready yet
        }

        double minLat = positions.stream().mapToDouble(GeoPosition::getLatitude).min().orElse(0);
        double maxLat = positions.stream().mapToDouble(GeoPosition::getLatitude).max().orElse(0);
        double minLon = positions.stream().mapToDouble(GeoPosition::getLongitude).min().orElse(0);
        double maxLon = positions.stream().mapToDouble(GeoPosition::getLongitude).max().orElse(0);

        GeoPosition center = new GeoPosition((minLat + maxLat) / 2, (minLon + maxLon) / 2);
        mapViewer.setAddressLocation(center);

        int bestZoom = 5;

        mapViewer.setZoom(bestZoom);
    }


    
    /**
     * Starts the timer responsible for automatically refreshing the map overlays
     * at fixed intervals defined by {@link #REFRESH_INTERVAL_MS}.
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> {
            updateWaypoints();
        });
        refreshTimer.setInitialDelay(0);
        refreshTimer.start();
    }
    
    
    /**
     * Starts a background thread to prefetch map tiles for the geographic bounding box
     * covering all stops in the city. Prefetching is performed for zoom levels 1 through 7
     * to improve map responsiveness.
     */
    private void startTilePrefetchThread() {
        new Thread(() -> {
            try {
                List<Stop> stops = cityManager.getAllStops();
                if (stops.isEmpty()) return;

                // Compute bounding box
                double minLat = stops.stream().mapToDouble(s -> s.getPosition().getLatitude()).min().orElse(0);
                double maxLat = stops.stream().mapToDouble(s -> s.getPosition().getLatitude()).max().orElse(0);
                double minLon = stops.stream().mapToDouble(s -> s.getPosition().getLongitude()).min().orElse(0);
                double maxLon = stops.stream().mapToDouble(s -> s.getPosition().getLongitude()).max().orElse(0);

                GeoPosition topLeft = new GeoPosition(maxLat, minLon);
                GeoPosition bottomRight = new GeoPosition(minLat, maxLon);

                // Use the same tile factory as your map
                DefaultTileFactory tileFactory = (DefaultTileFactory) mapViewer.getTileFactory();

                // Prefetch tiles from zoom 1 to 7
                TilePrefetcher.prefetchViaDisplay(tileFactory, topLeft, bottomRight, 1, 7);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "TilePrefetcherThread").start();
    }
}
