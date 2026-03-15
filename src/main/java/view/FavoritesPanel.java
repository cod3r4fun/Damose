// written almost entirely by chatGPT 

package view;

import model.user.AccountManager;
import model.vehicles.Route;
import model.vehicles.Stop;
import model.vehicles.Manager.CityManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;




/**
 * A panel that displays and manages a user's favorite transit stops and routes.
 * <p>
 * The {@code FavoritesPanel} allows users to:
 * <ul>
 *     <li>View their favorite stops and routes (Bus, Metro, Rail, Tram)</li>
 *     <li>Switch between views using a dropdown selector</li>
 *     <li>Double-click or use a button to display selected favorites on the map</li>
 *     <li>Remove favorites from the list</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Headers such as "Bus Routes:" are rendered with custom styling and cannot be selected or interacted with.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class FavoritesPanel extends JPanel {

    private final CityManager cityManager;
    private final JList<String> favoritesList;
    private DefaultListModel<String> listModel;

    private Map<String, Stop> favoriteStops;
   
    private Map<String, Route> favoriteRoutes;

    private JComboBox<String> viewSelector;  // to switch between stops/routes

    // Set to hold header labels, so we can render them differently and skip selection
    private final Set<String> headers = new HashSet<>(Arrays.asList(
            "Bus Routes:", "Metro Routes:", "Rail Routes:", "Tram Routes:"));

    
    /**
     * Constructs a new {@code FavoritesPanel} for managing and interacting with favorite stops and routes.
     *
     * @param cityManager the city manager that provides stop and route information
     */
    public FavoritesPanel(CityManager cityManager) {
        this.cityManager = cityManager;
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        favoritesList = new JList<>(listModel);
        favoritesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer to style headers differently and disable selection for them
        favoritesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String val = (String) value;
                if (headers.contains(val)) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setForeground(Color.BLACK);
                    label.setEnabled(false);
                } else {
                    label.setEnabled(true);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(favoritesList);

        // Combo box for switching between Stops and Routes view
        viewSelector = new JComboBox<>(new String[]{"Stops", "Routes"});
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("View Favorites:"), BorderLayout.WEST);
        topPanel.add(viewSelector, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        JButton showButton = new JButton("Show");
        JButton removeButton = new JButton("Remove");
        buttonsPanel.add(showButton);
        buttonsPanel.add(removeButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Load favorites initially (default to "Stops")
        loadFavoritesFromAccount("Stops");

        // Change favorites display when view changes
        viewSelector.addActionListener(e -> {
            String selectedView = (String) viewSelector.getSelectedItem();
            loadFavoritesFromAccount(selectedView);
        });

        showButton.addActionListener(e -> showSelectedFavorite());
        removeButton.addActionListener(e -> removeSelectedFavorite());

        favoritesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showSelectedFavorite();
                }
            }
        });

        // Prevent selection of headers
        favoritesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = favoritesList.getSelectedValue();
                if (selected != null && headers.contains(selected)) {
                    favoritesList.clearSelection();
                }
            }
        });
    }
    
    
    /**
     * Loads the user's favorite stops or routes into the list view, based on the selected view type.
     *
     * @param viewType either "Stops" or "Routes"
     */

    private void loadFavoritesFromAccount(String viewType) {
        listModel.clear();
        favoriteStops = new HashMap<>();
        favoriteRoutes = new HashMap<>();

        var favorites = AccountManager.getInstance().getCurrentFavorites();

        if ("Stops".equals(viewType)) {
            // Load favorite stops only
            List<Stop> stops = favorites.getFavoriteStops();

            for (Stop stop : stops) {
                String name = stop.getName();
                favoriteStops.put(name, stop);
                listModel.addElement("Stop: " + name);
            }
        } else if ("Routes".equals(viewType)) {
        	java.util.function.BiConsumer<String, List<Route>> addRouteGroup = (header, routes) -> {
        	    if (routes != null && !routes.isEmpty()) {
        	        listModel.addElement(header);
        	        for (Route r : routes) {
        	            String name = r.getRouteName();
        	            favoriteRoutes.put(name, r);
        	            listModel.addElement("Route: " + name);
        	        }
        	    }
        	};

        	// Assuming these methods return lists of favorite routes per type:
        	addRouteGroup.accept("Bus Routes:", AccountManager.getInstance().getCurrentFavorites().getFavoriteBusRoutes());
        	addRouteGroup.accept("Metro Routes:", AccountManager.getInstance().getCurrentFavorites().getFavoriteMetroRoutes());
        	addRouteGroup.accept("Rail Routes:", AccountManager.getInstance().getCurrentFavorites().getFavoriteRailRoutes());
        	addRouteGroup.accept("Tram Routes:", AccountManager.getInstance().getCurrentFavorites().getFavoriteTramRoutes());
        }
    }

    
    
    /**
     * Shows the selected stop or route on the map.
     * <p>For stops, both map and detail views are triggered.</p>
     * <p>For routes, only a temporary visual overlay is shown.</p>
     */
    private void showSelectedFavorite() {
        String selected = favoritesList.getSelectedValue();
        if (selected == null || headers.contains(selected)) return;

        try {
            if (selected.startsWith("Stop: ")) {
                String stopName = selected.substring(6);
                Stop stop = favoriteStops.get(stopName);
                if (stop != null) {
                    MainUIInitializer.getInstance().showStopOnMap(stop);
                    MainUIInitializer.getInstance().showStop(stopName);
                }
            } else if (selected.startsWith("Route: ")) {
                String routeName = selected.substring(7);
                Route route = favoriteRoutes.get(routeName);
                if (route != null) {
                    MainUIInitializer.getInstance().showRouteOnMapTemporarily(route);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error showing favorite: " + e.getMessage());
        }
    }

    
    
    /**
     * Removes the selected stop or route from the user's favorites
     * and refreshes the list view.
     */
    private void removeSelectedFavorite() {
        String selected = favoritesList.getSelectedValue();
        if (selected == null || headers.contains(selected)) return;

        try {
            if (selected.startsWith("Stop: ")) {
                String stopName = selected.substring(6);
                Stop stop = favoriteStops.get(stopName);
                if (stop != null) {
                    AccountManager.getInstance().removeFavoriteStop(stop);
                }
            } else if (selected.startsWith("Route: ")) {
                String routeName = selected.substring(7);
                Route route = favoriteRoutes.get(routeName);
                if (route != null) {
                    AccountManager.getInstance().removeFavoriteRoute(route);
                }
            }
            // Refresh list based on current view
            loadFavoritesFromAccount((String) viewSelector.getSelectedItem());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error removing favorite: " + ex.getMessage());
        }
    }
}

