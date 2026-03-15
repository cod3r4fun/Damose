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
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A Swing JPanel component that provides a user interface for searching and displaying transit stops.
 * <p>
 * The panel allows users to enter stop names, view matching results with clickable links,
 * mark stops as favorites, and view stop locations on a map.
 * </p>
 * <p>
 * Results include upcoming arrival times for each stop, grouped by routes.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class StopsPanel extends JPanel {
    private final Searcher searchEngine;
    private final Manager.CityManager cityManager;

    private JTextField searchField;
    private JEditorPane resultsPane;
    
    
    /**
     * Constructs a StopsPanel tied to a specific city and search engine.
     * 
     * @param cityManager the city manager providing access to stops; must not be null
     * @param searchEngine the search engine to query stop times; must not be null
     */
    public StopsPanel(Manager.CityManager cityManager, Searcher searchEngine) {
        this.cityManager = cityManager;
        this.searchEngine = searchEngine;
        initUI();
    }

    
    /**
     * Sets the search field text and triggers a search for stops matching the text.
     * 
     * @param text the search text to set
     */
    public void setSearchText(String text) {
        searchField.setText(text);
        searchStops();
    }

    
    /**
     * Initializes the UI components: search field and results pane with hyperlink support.
     * <p>
     * The results pane renders HTML with clickable links for stops and favorite marking.
     * Clicking a stop link centers the map on that stop.
     * Clicking a favorite link attempts to save the stop as a user favorite.
     * </p>
     */
    private void initUI() {
        setLayout(new BorderLayout());

        searchField = new JTextField();
        resultsPane = new JEditorPane("text/html", "");
        resultsPane.setEditable(false);
        resultsPane.setContentType("text/html");
        resultsPane.setText("Enter a stop name to begin...");

        resultsPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                	String desc = e.getDescription();
                    if (desc.startsWith("favorite:")) {
                        String stopId = desc.substring("favorite:".length());
                        Stop stop = cityManager.getStopByStopId(stopId);
                        if (stop != null) {
                            try {
								AccountManager.getInstance().saveFavoriteStop(stop);
								 JOptionPane.showMessageDialog(StopsPanel.this, "Marked stop '" + stop.getName() + "' as favorite!");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								JOptionPane.showMessageDialog(StopsPanel.this, "failed to register due to" + e.toString());
							} catch (FavoriteAlreadyPresent e1) {
								JOptionPane.showMessageDialog(StopsPanel.this, "The stop was already a favorite :-)");
							} 
                        }
                    } else {
                        Stop selectedStop = cityManager.getAllStops().stream()
                            .filter(s -> s.getStopId().equals(desc))
                            .findFirst().orElse(null);
                        if (selectedStop != null) {
                            MainUIInitializer.getInstance().showStopOnMap(selectedStop);
                        }
                    }
                }
            }
        });


        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(resultsPane), BorderLayout.CENTER);

        searchField.addActionListener(e -> searchStops());
    }
    
    
    
    /**
     * Searches stops based on the current search field text, updating the results pane.
     * <p>
     * Results are shown as HTML with each matching stop linked. Each stop entry includes:
     * <ul>
     *   <li>A link to center the map on the stop</li>
     *   <li>A link to mark the stop as favorite</li>
     *   <li>Upcoming arrival times per route (up to 5)</li>
     * </ul>
     * If no stops match, a message is displayed.
     * </p>
     */

    private void searchStops() {
        String query = searchField.getText().trim().toLowerCase();
        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");

        for (Stop stop : cityManager.getAllStops()) {
            if (stop.getName().toLowerCase().contains(query)) {
                html.append("<b><a href=\"").append(stop.getStopId()).append("\">")
                    .append("Stop: ").append(stop.getName())
                    .append("</a></b> ");

                html.append("[<a href=\"favorite:").append(stop.getStopId()).append("\">Favorite</a>] ");

                html.append(" (").append(stop.getStopId()).append(")")
                    .append("<br>");
                
                Map<Route, List<LocalTime>> times;
                
                try {
                	times = searchEngine.searchStopTimesPerStop(stop.getStopId());
                    for (Route route : times.keySet()) {
                        List<LocalTime> arrivals = times.get(route).stream()
                                .filter(Objects::nonNull).toList();

                        if (arrivals.size() >= 5) {
                            arrivals = arrivals.subList(0, 5);
                        }

                        html.append("&nbsp;&nbsp;→ Line: ").append(route.getRouteName())
                            .append(" - Arrivals: ").append(arrivals.toString()).append("<br>");
                    }
                } catch (NullPointerException e) {
                	JOptionPane.showMessageDialog(this, "sorry, seems something went wrong; the app is probably still initializing or there are no routes; \n please try again later :-(");
                	break;
                }
               
                
                // with sorting order
                
                /*
                for (Route route : times.keySet()) {
                    List<LocalTime> arrivals = times.get(route).stream()
                            .filter(Objects::nonNull).sorted().toList();

                    if (arrivals.size() >= 5) {
                        arrivals = arrivals.subList(0, 5);
                    }

                    html.append("&nbsp;&nbsp;→ Line: ").append(route.getRouteName())
                        .append(" - Arrivals: ").append(arrivals).append("<br>");
                }
                */
                

                
                html.append("<br>");
            }
        }

        if (html.toString().equals("<html><body style='font-family:sans-serif;'>")) {
            html.append("No stops found.");
        }

        html.append("</body></html>");
        resultsPane.setText(html.toString());
        resultsPane.setCaretPosition(0); 
    }
}

