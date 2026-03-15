package view;

import model.searchEngine.Searcher;
import model.searchEngine.simpleSearch;
import model.transitEngine.SimpleVehiclePositionAnalyzer;
import model.transitEngine.VehiclePositionAnalyzer;
import model.user.AccountManager;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import utility.CityTrack;
import utility.Triple;
import utility.exceptionUtils.PasswordNotEqual;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import controller.ClockOrchestrator;
import controller.DateController;
import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;

import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;




/**
 * The {@code MainUIInitializer} class serves as the central controller for the user interface
 * of the SmartTransit Viewer application. It follows the singleton pattern and provides
 * functionality to initialize and manage the core GUI components including tabs, maps,
 * account access, connection status, and map visualizations of stops and routes.
 * <p>
 * This class coordinates UI rendering, user interaction handling, and backend integrations
 * with the transit and user management subsystems. It uses JXMapViewer for geographic
 * visualization and Swing components for interaction.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class MainUIInitializer {
	private static MainUIInitializer instance;
    private final JFrame mainFrame;
    private final JTabbedPane tabbedPane;
    private final Searcher searchEngine;
    private VehiclePositionAnalyzer vpa;
    private final Manager.CityManager cityManager;
    private JXMapViewer mapViewer;
    private JLabel connectionStatusLabel;
    private JLabel referenceDateLabel;
    private JDialog accountDialog;
    private JButton accountButton;


    
    /**
     * Starts the UI initializer process after ensuring the ClockOrchestrator has completed
     * its update cycle. This method blocks recursively until synchronization is complete.
     * 
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    public static void start() throws InterruptedException {
        while (!ClockOrchestrator.isUpdateFinished()) {
            Thread.sleep(1000);
        }

        if (instance == null)
            instance = new MainUIInitializer();
    }
    
    /**
     * Returns the singleton instance of the {@code MainUIInitializer}.
     * 
     * @return the singleton instance.
     */
    public static MainUIInitializer getInstance() {
    	return instance;
    }

    
    /**
     * Displays a loading splash screen in the center of the screen while a long-running
     * initialization task executes. The screen disappears automatically after the task
     * completes.
     * 
     * @param onComplete a {@code Runnable} task to be executed during the splash screen display.
     */
    // written by chatGPT
    public static void showLoadingScreen(Runnable onComplete) {
        JWindow splash = new JWindow();
        JPanel content = (JPanel) splash.getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new BorderLayout());

        int width = 400;
        int height = 200;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        splash.setBounds(x, y, width, height);

        JLabel mainLabel = new JLabel("Loading Damose!", SwingConstants.CENTER);
        mainLabel.setFont(new Font("Sans-Serif", Font.BOLD, 16));
        content.add(mainLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Please wait, load in progress...", SwingConstants.CENTER);
        subLabel.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
        content.add(subLabel, BorderLayout.SOUTH);

        splash.setVisible(true);

        new Thread(() -> {
            onComplete.run(); // blocks while initializing
            SwingUtilities.invokeLater(() -> {
                splash.setVisible(false);
                splash.dispose();
            });
        }).start();
    }

    
    /**
     * Switches to the "Stops" tab and populates the search field with the provided stop name.
     * 
     * @param stopName the name of the stop to be displayed.
     */
    public void showStop(String stopName) {
        tabbedPane.setSelectedIndex(0); //it switches to the stop tab (it is 0, it could be access through a constant for better generalization)

        // Assuming StopsPanel exposes a method to set the search and trigger search:
        StopsPanel stopsPanel = (StopsPanel) tabbedPane.getComponentAt(0);
        stopsPanel.setSearchText(stopName);
    }
    
    
    
    
    /**
     * Highlights a specific stop on the map for a limited duration (5 seconds), drawing
     * a temporary marker and recentring the map view.
     * 
     * @param stop the {@code Stop} to be displayed on the map.
     */
    @SuppressWarnings({ "unchecked" })
	public void showStopOnMap(Stop stop) {
        tabbedPane.setSelectedIndex(2); // it switches to the map tab (it is 2, it could be access through a constant for better generalization)

        GeoPosition position = new GeoPosition(
            stop.getPosition().getLatitude(),
            stop.getPosition().getLongitude()
        );

        mapViewer.setZoom(3);
        mapViewer.setAddressLocation(position);
        mapViewer.setCenterPosition(position);

        Set<Waypoint> singleStop = new HashSet<>();
        singleStop.add(new DefaultWaypoint(position));

        WaypointPainter<Waypoint> painter = new WaypointPainter<>();
        painter.setWaypoints(singleStop);

		CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painter);
        mapViewer.setOverlayPainter(compoundPainter);
       

        mapViewer.revalidate();
        mapViewer.repaint();

        new javax.swing.Timer(5000, e -> {
            mapViewer.setOverlayPainter(null);
            mapViewer.repaint();
            ((javax.swing.Timer) e.getSource()).stop(); 
        }).start();
    }
    
    
    
    /**
     * Displays a connection error dialog and terminates the application if no active connection
     * is found.
     * 
     * @param connectionActive {@code true} if the connection is active; otherwise {@code false}.
     */
    public static void showConnectionError(boolean connectionActive) {
    	if(!connectionActive) {
    		JOptionPane.showMessageDialog(null,
                    "Connection is not active.\nPlease enable the connection and relaunch the application.",
                    "Connection Required",
                    JOptionPane.WARNING_MESSAGE);
                System.exit(0);
    	}
    }
    
    
    
    /**
     * Temporarily displays a selected route on the map, along with all associated stops and
     * current vehicle positions. The visual overlay persists for 15 seconds.
     * 
     * @param selectedRoute the {@code Route} to be visualized.
     */
    public void showRouteOnMapTemporarily(Route selectedRoute) {
        tabbedPane.setSelectedIndex(2);

        List<Stop> stops = cityManager.getStopsPerTrip(cityManager.getTripsPerRoute(selectedRoute).get(0))
        		.stream().map((triple) -> triple.getX()).toList();
        if (stops == null || stops.isEmpty()) return;

        List<GeoPosition> positions = stops.stream()
                .map(stop -> new GeoPosition(
                        stop.getPosition().getLatitude(),
                        stop.getPosition().getLongitude()))
                .collect(Collectors.toList());

        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(positions.get(0));

        RoutePainter routePainter = new RoutePainter(positions, Color.RED, 3f); // Custom class
        

		CompoundPainter<JXMapViewer> compound = new CompoundPainter<>(routePainter);
        mapViewer.setOverlayPainter(compound);
        
        var clickableWaypoints = stops.stream()
                .map(ClickableWaypoint::new)
                .collect(Collectors.toSet());


        WaypointPainter<ClickableWaypoint> painter = new WaypointPainter<>();
        painter.setWaypoints(clickableWaypoints);
        compound.addPainter(painter);
        
        List<VehicleWaypoint> vehicleWaypoints = vpa.findVehiclePositionsForRoute(selectedRoute.getRouteId(), cityManager).stream()
        		.map(pos -> new VehicleWaypoint(pos, "Line " + selectedRoute.getRouteId())) .collect(Collectors.toList());
        
        WaypointPainter<VehicleWaypoint> vehiclePainter = new WaypointPainter<>();
        vehiclePainter.setWaypoints(new HashSet<>(vehicleWaypoints));
        vehiclePainter.setRenderer(new VehicleWaypointRenderer());
        
        compound.addPainter(vehiclePainter);

        mapViewer.revalidate();
        mapViewer.repaint();

        new Timer(15000, e -> {
            mapViewer.setOverlayPainter(null);
            mapViewer.repaint();
            ((Timer) e.getSource()).stop();
        }).start();
    }
    
    
    
    /**
     * Renders both directional paths of a given route on the map, using color differentiation
     * and markers for clarity. Vehicle positions per direction are also visualized.
     * 
     * @param route the {@code Route} for which directions are visualized.
     * @param direction0Stops the list of stops for direction 0.
     * @param direction1Stops the list of stops for direction 1.
     */
    @SuppressWarnings("unchecked")
	public void showMultipleDirectionsOnMap(Route route, List<Stop> direction0Stops, List<Stop> direction1Stops) {
        tabbedPane.setSelectedIndex(2);

        List<GeoPosition> dir0Positions = direction0Stops.stream()
                .map(stop -> 
                        stop.getPosition())
                .collect(Collectors.toList());

        List<GeoPosition> dir1Positions = direction1Stops.stream()
                .map(stop -> 
                        stop.getPosition())
                .collect(Collectors.toList());

        if (dir0Positions.isEmpty() && dir1Positions.isEmpty()) return;

        GeoPosition center = !dir0Positions.isEmpty() ? dir0Positions.get(0) : dir1Positions.get(0);
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(center);
        mapViewer.setCenterPosition(center);

        RoutePainter dir0Painter = new RoutePainter(dir0Positions, Color.BLUE, 3f);
        RoutePainter dir1Painter = new RoutePainter(dir1Positions, Color.MAGENTA, 3f);

        Set<ClickableWaypoint> allWaypoints = new HashSet<>();
        direction0Stops.forEach(stop -> allWaypoints.add(new ClickableWaypoint(stop, Color.BLUE)));
        direction1Stops.forEach(stop -> allWaypoints.add(new ClickableWaypoint(stop, Color.MAGENTA)));

        WaypointPainter<ClickableWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(allWaypoints);

        CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
        compound.setPainters(dir0Painter, dir1Painter, waypointPainter);
        mapViewer.setOverlayPainter(compound);
        
        List<VehicleWaypoint> vehicleWaypoints = new ArrayList<>();
        vehicleWaypoints.addAll(vpa.findVehiclePositionsForRouteAndDirection(route.getRouteId(),"0", cityManager).stream()
	            .map(pos -> new VehicleWaypoint(pos, "Line " + route.getRouteId() + " → " + "0")).collect(Collectors.toList()));
		vehicleWaypoints.addAll(vpa.findVehiclePositionsForRouteAndDirection(route.getRouteId(),"1", cityManager).stream()
	            .map(pos -> new VehicleWaypoint(pos, "Line " + route.getRouteId() + " → " + "1")).collect(Collectors.toList()));
		
		WaypointPainter<VehicleWaypoint> vehiclePainter = new WaypointPainter<>();
        vehiclePainter.setWaypoints(new HashSet<>(vehicleWaypoints));
        vehiclePainter.setRenderer(new VehicleWaypointRenderer());
        
        compound.addPainter(vehiclePainter);
        
        

        mapViewer.revalidate();
        mapViewer.repaint();

        new Timer(15000, e -> {
            mapViewer.setOverlayPainter(null);
            mapViewer.repaint();
            ((Timer) e.getSource()).stop();
        }).start();
    }

    
    
    
    
    /**
     * Constructs and initializes the main UI components, panels, and data models.
     * This method is private to enforce singleton pattern use via {@code start()}.
     */
    private MainUIInitializer() {
    	showConnectionError(MasterConnectionStatusChecker.getSingleton().isConnectionActive());

        String cityName = CityTrack.getTrackedCity();
        this.cityManager = Manager.searchCityManager(cityName);
        this.searchEngine = new simpleSearch(cityName);
        vpa = new SimpleVehiclePositionAnalyzer();
        mainFrame = new JFrame("SmartTransit Viewer - " + cityName);
        tabbedPane = new JTabbedPane();
        initUI();
    }
    
    
    /**
     * Initializes and arranges the top-level components of the main window including
     * tabs, account panel, and status controls.
     */
    private void initUI() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);
        
        String username = model.user.AccountManager.getInstance().getCurrentAccountUsername();
        accountButton = new JButton("Account: " + username);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buildStatusPanel(), BorderLayout.NORTH);
        topPanel.add(tabbedPane, BorderLayout.CENTER);

        MapPanel mapPanel = new MapPanel(cityManager);
        tabbedPane.addTab("Stops", new StopsPanel(cityManager, searchEngine));
        tabbedPane.addTab("Lines",new LinesPanel(cityManager, searchEngine));
        tabbedPane.addTab("Map", mapPanel);
        tabbedPane.addTab("Favorites", new FavoritesPanel(cityManager));
        // to remove tabbedPane.addTab("User", new JLabel("User panel (coming soon)"));
        
        
        this.mapViewer = mapPanel.getMapViewer();
        mainFrame.getContentPane().add(topPanel);
        mainFrame.setVisible(true);
    }
    


    /**
     * Builds the panel displayed at the top of the UI containing connection status,
     * date controls, and account access.
     * 
     * @return the fully constructed top status {@code JPanel}.
     */
    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        connectionStatusLabel = new JLabel();
        referenceDateLabel = new JLabel("Visualize results until: " + DateController.getReferenceDate());

        JButton advanceDateButton = new JButton("Next Day");
        advanceDateButton.addActionListener(e -> {
            DateController.moveReferenceOneDayAhead();
            referenceDateLabel.setText("Reference Date: " + DateController.getReferenceDate());
        });

        JButton resetDateButton = new JButton("Reset Date");
        resetDateButton.addActionListener(e -> {
            DateController.reset();
            referenceDateLabel.setText("Reference Date: " + DateController.getReferenceDate());
        });

        leftGroup.add(connectionStatusLabel);
        leftGroup.add(referenceDateLabel);
        leftGroup.add(advanceDateButton);
        leftGroup.add(resetDateButton);

        accountButton.addActionListener(e -> showAccountPanel());

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightGroup.add(accountButton);

        panel.add(leftGroup, BorderLayout.WEST);
        panel.add(rightGroup, BorderLayout.EAST);
        
        startConnectionStatusUpdater();

        return panel;
    }



    /**
     * Periodically updates the UI label representing the master connection status.
     * Status is polled via the {@code MasterConnectionStatusChecker}.
     */
    private void updateConnectionStatusLabel() {
        boolean isConnected = MasterConnectionStatusChecker.getSingleton().isConnectionActive();
        connectionStatusLabel.setText("Connection: " + (isConnected ? "Online" : "Offline"));
        connectionStatusLabel.setForeground(isConnected ? Color.GREEN.darker() : Color.RED);
    }

    
    
    /**
     * Initializes a repeating timer task that updates the connection status label
     * at a fixed interval defined in {@code ClockOrchestrator}.
     */
    private void startConnectionStatusUpdater() {
        var connectionStatusTimer = new Timer(ClockOrchestrator.CONNECTION_UPDATE_TIME_SECONDS*1000, e -> {
            updateConnectionStatusLabel();
        });
        connectionStatusTimer.setInitialDelay(0);
        connectionStatusTimer.start();
    }
    
    
    
    
    
    
    /**
     * Displays a modal dialog allowing users to either log in or register an account.
     * Captures user input for username and passwords and communicates with the
     * {@code AccountManager}.
     */
    private void showAccountPanel() {
        if (accountDialog == null) {
            accountDialog = new JDialog(mainFrame, "Account Access", true);
            accountDialog.setSize(400, 300);
            accountDialog.setLocationRelativeTo(mainFrame);

            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPasswordField passwordConfirmField = new JPasswordField();

            JButton loginButton = new JButton("Login");
            JButton registerButton = new JButton("Register");
            JLabel feedbackLabel = new JLabel();

            loginButton.addActionListener(e -> {
                try {
                    AccountManager.getInstance().logIn(usernameField.getText(), new String(passwordField.getPassword()));
                    feedbackLabel.setText("Login successful.");
                    accountDialog.dispose();
                } catch (Exception ex) {
                    feedbackLabel.setText("Login failed.");
                }
                MainUIInitializer.getInstance().refreshAccountButtonLabel();
            });

            registerButton.addActionListener(e -> {
                try {
                    AccountManager.getInstance().createAnAccount(
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        new String(passwordConfirmField.getPassword())
                    );
                    feedbackLabel.setText("Registration successful.");
                    accountDialog.dispose();
                } catch (PasswordNotEqual pne) {
                    feedbackLabel.setText("Passwords do not match.");
                } catch (Exception ex) {
                    feedbackLabel.setText("Error: " + ex.getMessage());
                }
                MainUIInitializer.getInstance().refreshAccountButtonLabel();
            });

            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("Confirm Password:"));
            panel.add(passwordConfirmField);
            panel.add(loginButton);
            panel.add(registerButton);
            panel.add(new JLabel()); // filler
            panel.add(feedbackLabel);

            accountDialog.add(panel);
        }

        accountDialog.setVisible(true);
    }
    
    
    /**
     * Updates the account button text with the currently logged-in user's username.
     */
    private void refreshAccountButtonLabel() {
        if (accountButton != null) {
            String username = AccountManager.getInstance().getCurrentAccountUsername();
            accountButton.setText("Account: " + username);
            System.out.println(AccountManager.getInstance().getCurrentAccountUsername());
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainUIInitializer::new);
    }
}
