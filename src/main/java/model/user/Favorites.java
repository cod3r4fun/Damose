package model.user;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import utility.exceptionUtils.*;

import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import utility.CityTrack;


/**
 * Represents a user's collection of favorite transit routes and stops,
 * categorized by route type for efficient access and management.
 * <p>
 * Supports addition, retrieval, and removal of favorite routes and stops.
 * Routes are grouped into metro, tram, rail, and bus categories based on their type.
 * </p>
 * <p>
 * Provides serialization and deserialization support for persistent storage of favorites.
 * </p>
 * 
 * <p><b>Thread Safety:</b> This class is not thread-safe and should be externally synchronized
 * if accessed concurrently.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Favorites implements Serializable {
	
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * List of favorite tram routes (type "0").
     */
	private final List<Route> favoriteMetroRoutes;
	
	 /**
     * List of favorite metro routes (type "1").
     */
	private final List<Route> favoriteRailRoutes;
	
    /**
     * List of favorite rail routes (type "2").
     */
	private final List<Route> favoriteTramRoutes;
	
    /**
     * List of favorite bus routes (type "3").
     */
	private final List<Route> favoriteBusRoutes;
	
    /**
     * List of favorite stops.
     */
	private final List<Stop> favoriteStops;
	
	
    /**
     * Constructs a new, empty Favorites collection.
     */
	public Favorites() {
		favoriteMetroRoutes = new ArrayList<>();
		favoriteTramRoutes = new ArrayList<>();
		favoriteRailRoutes = new ArrayList<>();
		favoriteBusRoutes = new ArrayList<>();
		favoriteStops = new ArrayList<>();
	}
	
	
	
    /**
     * Saves a route as a favorite by its ID.
     * <p>
     * The route is looked up in the current tracked city. The favorite is
     * added to the appropriate route type list.
     * </p>
     * 
     * @param routeId the ID of the route to add as favorite
     * @throws FavoriteAlreadyPresent if the route is already in the favorites
     */
	public void saveFavoriteRoute(String routeId) throws FavoriteAlreadyPresent {
		saveFavoriteRoute(Manager
				.searchCityManager(CityTrack.getTrackedCity()).getAllRoutes()
				.stream().filter((route) -> route.getRouteId().equals(routeId))
				.findFirst().get());
	}
	
	
    /**
     * Saves a route as a favorite.
     * <p>
     * Adds the route to the list corresponding to its type.
     * </p>
     * 
     * @param route the route to add as favorite
     * @throws FavoriteAlreadyPresent if the route is already present in favorites
     */
	public void saveFavoriteRoute(Route route) throws FavoriteAlreadyPresent {
		switch (route.getType()) {
		case ("0"): if(!favoriteTramRoutes.contains(route)) {
			favoriteTramRoutes.add(route); return;
		} else  throw new FavoriteAlreadyPresent();
		case ("1"): if(!favoriteMetroRoutes.contains(route)) {
			favoriteMetroRoutes.add(route); return;
		} else  throw new FavoriteAlreadyPresent();
		case ("2"): if(!favoriteRailRoutes.contains(route)) {
			favoriteRailRoutes.add(route); return;
		} else  throw new FavoriteAlreadyPresent();
		case ("3"): if(!favoriteBusRoutes.contains(route)) {
			favoriteBusRoutes.add(route); return;
		} else  throw new FavoriteAlreadyPresent();
		}
	}
	
	
    /**
     * Saves a stop as a favorite.
     * 
     * @param stop the stop to add as favorite
     * @throws FavoriteAlreadyPresent if the stop is already present in favorites
     */
	public void saveFavoriteStop(Stop stop) throws FavoriteAlreadyPresent {
		if (favoriteStops.contains(stop)) throw new FavoriteAlreadyPresent();
		favoriteStops.add(stop);
	}
	
    /**
     * Saves a stop as a favorite by its stop ID.
     * <p>
     * The stop is looked up in the current tracked city.
     * </p>
     * 
     * @param stopId the ID of the stop to add as favorite
     * @throws FavoriteAlreadyPresent if the stop is already present in favorites
     */
	public void saveFavoriteStop(String stopId) throws FavoriteAlreadyPresent {
		saveFavoriteStop(Manager
				.searchCityManager(CityTrack.getTrackedCity()).getStopByStopId(stopId));
	}
	
	
    /**
     * Returns an unmodifiable copy of the list of favorite stops.
     * 
     * @return list of favorite stops
     */
	public List<Stop> getFavoriteStops(){
		return List.copyOf(favoriteStops);
	}
	
    /**
     * Returns an unmodifiable copy of the list of favorite metro routes.
     * 
     * @return list of favorite metro routes
     */
	public List<Route> getFavoriteMetroRoutes(){
		return List.copyOf(favoriteMetroRoutes);
	}
	
	
    /**
     * Returns an unmodifiable copy of the list of favorite tram routes.
     * 
     * @return list of favorite tram routes
     */
	public List<Route> getFavoriteTramRoutes(){
		return List.copyOf(favoriteTramRoutes);
	}
	
    /**
     * Returns an unmodifiable copy of the list of favorite rail routes.
     * 
     * @return list of favorite rail routes
     */
	public List<Route> getFavoriteRailRoutes(){
		return List.copyOf(favoriteRailRoutes);
	}
	
    /**
     * Returns an unmodifiable copy of the list of favorite bus routes.
     * 
     * @return list of favorite bus routes
     */
	public List<Route> getFavoriteBusRoutes(){
		return List.copyOf(favoriteBusRoutes);
	}
	
	
    /**
     * Serializes the current Favorites object to the specified file.
     * 
     * @param filename the path of the file to serialize to
     * @throws IOException if an I/O error occurs
     */
    public void serializeToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    
    /**
     * Deserializes a Favorites object from the specified file.
     * 
     * @param filename the path of the file to deserialize from
     * @return the deserialized Favorites object
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static Favorites deserializeFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Favorites) ois.readObject();
        }
    }
    
    
    /**
     * Removes a stop from the favorites.
     * 
     * @param stop the stop to remove
     */
    public void removeFavoriteStop(Stop stop) {
    	favoriteStops.remove(stop);
    }
    
    /**
     * Removes a stop from the favorites by stop ID.
     * <p>
     * The stop is looked up in the current tracked city.
     * </p>
     * 
     * @param stopId the ID of the stop to remove
     */
    public void removeFavoriteStop(String stopId) {
    	removeFavoriteStop(Manager.searchCityManager(CityTrack.getTrackedCity()).getStopByStopId(stopId));
    }
    
    /**
     * Removes a route from the favorites.
     * <p>
     * The route is removed from whichever route type list it belongs to.
     * </p>
     * 
     * @param route the route to remove
     */
    public void removeFavoriteRoute(Route route) {
    	if (favoriteMetroRoutes.contains(route)) {
    		favoriteMetroRoutes.remove(route); 
    	} else if(favoriteTramRoutes.contains(route)) {
    		favoriteTramRoutes.remove(route);
    	} else if (favoriteRailRoutes.contains(route)) {
    		favoriteRailRoutes.remove(route);
    	} else if (favoriteBusRoutes.contains(route)){
    		favoriteBusRoutes.remove(route);
    	} return;
    }
    
    
    /**
     * Returns the serial version UID for serialization compatibility.
     * 
     * @return serialVersionUID value
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
