package view;

import model.searchEngine.Searcher;
import model.user.AccountManager;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import utility.exceptionUtils.FavoriteAlreadyPresent;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;





/**
 * The {@code LinesPanel} class provides a Swing-based user interface
 * for searching and exploring public transit routes and their stops,
 * as well as managing route favorites.
 *
 * <p>This panel allows users to:
 * <ul>
 *   <li>Search for transit lines by name</li>
 *   <li>View stops and upcoming arrival times for each route</li>
 *   <li>Visualize selected routes on the map</li>
 *   <li>Mark routes as favorites</li>
 * </ul>
 *
 * <p>The component integrates with the city’s data model and search engine,
 * ensuring dynamic and context-aware responses to user interactions.
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0	
 */
public class LinesPanel extends JPanel {
	
    /** Interface to query real-time stop data and arrival times. */
    private final Searcher searchEngine;
    
    /** Interface to access city-managed routes and stops. */
    private final Manager.CityManager cityManager;

    /** Input field for user search queries. */
    private JTextField searchField;
    
    /** Pane displaying HTML-formatted search results with hyperlinks. */
    private JEditorPane resultsPane;

    /** Formatter for displaying arrival times in HH:mm format. */
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructs a new {@code LinesPanel} instance.
     *
     * @param cityManager the city-wide manager of routes and stops
     * @param searchEngine the backend engine to perform time-based searches
     */
    public LinesPanel(Manager.CityManager cityManager, Searcher searchEngine) {
        this.cityManager = cityManager;
        this.searchEngine = searchEngine;
        initUI();
    }

    
    /**
     * Initializes the UI components, event listeners, and layout.
     */
    private void initUI() {
        setLayout(new BorderLayout());

        searchField = new JTextField();
        resultsPane = new JEditorPane("text/html", "");
        resultsPane.setEditable(false);
        resultsPane.setContentType("text/html");
        resultsPane.setText("Enter a line name to begin...");

        resultsPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String desc = e.getDescription();

                    if (desc.startsWith("favoriteRoute:")) {
                        String routeId = desc.substring("favoriteRoute:".length());
                        Route routeToFavorite = cityManager.getAllRoutes().stream()
                            .filter(r -> r.getRouteId().equals(routeId))
                            .findFirst().orElse(null);

                        if (routeToFavorite != null) {
                            try {
								AccountManager.getInstance().saveFavoriteRoute(routeToFavorite);
								JOptionPane.showMessageDialog(LinesPanel.this, "Added route '" 
		                                + routeToFavorite.getRouteName() + "' to favorites.");
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(LinesPanel.this, "failed to register due to" + e.toString());
							} catch (FavoriteAlreadyPresent e1) {
								JOptionPane.showMessageDialog(LinesPanel.this, "The stop was already a favorite :-)");
							}
                        } else {
                            JOptionPane.showMessageDialog(LinesPanel.this, "Route not found.");
                        }
                    } else {
                    	Route selectedRoute = cityManager.getAllRoutes().stream()
                    		    .filter(r -> r.getRouteId().equals(desc))
                    		    .findFirst().orElse(null);

                    		if (selectedRoute != null) {
                    		    boolean hasDirections = cityManager.routeHasDirection(selectedRoute.getRouteId());

                    		    if (hasDirections) {
                    		        List<Stop> direction0 = cityManager.getStopsPerRouteAndDirection(selectedRoute.getRouteId(), "0");
                    		        List<Stop> direction1 = cityManager.getStopsPerRouteAndDirection(selectedRoute.getRouteId(), "1");

                    		        try {
                    		            MainUIInitializer.getInstance().showMultipleDirectionsOnMap(selectedRoute, direction0, direction1);
                    		        } catch (Exception ex) {
                    		            JOptionPane.showMessageDialog(LinesPanel.this, "Failed to display both directions on map: " + ex.getMessage());
                    		        }

                    		    } else {
                    		        try {
                    		            MainUIInitializer.getInstance().showRouteOnMapTemporarily(selectedRoute);
                    		        } catch (Exception ex) {
                    		            JOptionPane.showMessageDialog(LinesPanel.this, "Failed to display route on map: " + ex.getMessage());
                    		        }
                    		    }
                    		}
                    }	
                }
            }
        });


        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(resultsPane), BorderLayout.CENTER);

        searchField.addActionListener(e -> searchLines());
    }

    
    /**
     * Performs a search over all available routes and updates the result pane with
     * route details, stops, and upcoming arrival times.
     */
    private void searchLines() {
        String query = searchField.getText().trim().toLowerCase();
        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");

        for (Route route : cityManager.getAllRoutes()) {
            if (route.getRouteName().toLowerCase().contains(query)) {
                html.append("<b><a href=\"").append(route.getRouteId()).append("\">")
                    .append("Line: ").append(route.getRouteName()).append("</a></b> ")
                    .append("[<a href=\"favoriteRoute:").append(route.getRouteId()).append("\">Favorite</a>] ")
                    .append("(routeId: ").append(route.getRouteId()).append(")<br>");

                boolean hasDirections = cityManager.routeHasDirection(route.getRouteId());

                if (hasDirections) {
                    for (String direction : List.of("0", "1")) {
                        html.append("&nbsp;&nbsp;<i>Direction ").append(direction).append(":</i><br>");

                        List<Stop> stops = cityManager.getStopsPerRouteAndDirection(route.getRouteId(), direction);
                        Map<Stop, List<LocalTime>> stopsMap = searchEngine.searchStopTimesPerRoute(route.getRouteId());
                        
                        boolean alreadBold = false;

                        for (Stop stop : stops) {
                            List<LocalTime> arrivals = Optional.ofNullable(stopsMap.get(stop))
                                    .orElseGet(ArrayList::new).stream()
                                    .filter(Objects::nonNull)
                                    .sorted()
                                    .limit(5)
                                    .collect(Collectors.toList());

                            String arrivalsFormatted = arrivals.stream()
                                    .map(t -> t.format(timeFormatter))
                                    .collect(Collectors.joining(", "));

                            html.append("&nbsp;&nbsp;&nbsp;&nbsp;→ Stop: ").append(stop.getName())
                                .append(" - Arrivals: ").append(arrivalsFormatted).append("<br>");
                        }
                    }
                } else {
                    List<Stop> stops = cityManager.getStopsPerRoute(route);
                    Map<Stop, List<LocalTime>> stopsMap = searchEngine.searchStopTimesPerRoute(route.getRouteId());
                    
                    boolean alreadBold = false;

                    for (Stop stop : stops) {
                        List<LocalTime> arrivals = Optional.ofNullable(stopsMap.get(stop))
                                .orElseGet(ArrayList::new).stream()
                                .filter(Objects::nonNull)
                                .sorted()
                                .limit(5)
                                .collect(Collectors.toList());

                        String arrivalsFormatted = arrivals.stream()
                                .map(t -> t.format(timeFormatter))
                                .collect(Collectors.joining(", "));

                        html.append("&nbsp;&nbsp;→ Stop: ").append(stop.getName())
                            .append(" - Arrivals: ").append(arrivalsFormatted).append("<br>");
                    }
                }

                html.append("<br>");
            }
        }

        if (html.toString().equals("<html><body style='font-family:sans-serif;'>")) {
            html.append("No lines found.");
        }

        html.append("</body></html>");
        resultsPane.setText(html.toString());
        resultsPane.setCaretPosition(0);
    }

}

