package model.user;

import utility.PasswordUtils;
import utility.exceptionUtils.FavoriteAlreadyPresent;
import java.io.*;

import model.vehicles.Route;
import model.vehicles.Stop;


/**
 * Represents a user account within the application, encapsulating authentication credentials
 * and user-specific favorite transit data.
 *
 * <p>This class supports secure password storage, user identification via username,
 * and management of favorite routes and stops. The class is serializable and can be persisted
 * to and restored from the filesystem via object serialization.</p>
 *
 * <p>Note: Instances of {@code Account} should only be manipulated through authenticated interfaces
 * to ensure data integrity and access control.</p>
 *
 * @author Franco Della Negra
 *
 * @see model.user.Favorites
 * @see model.vehicles.Route
 * @see model.vehicles.Stop
 * @see utility.PasswordUtils
 * @see utility.exceptionUtils.FavoriteAlreadyPresent
 * @see utility.exceptionUtils.PasswordNotEqual
 * @since 1.0
 * @version 1.0
 */
class Account implements Serializable {
	
	 /**
     * A unique version identifier for serialization interoperability.
     * Ensures consistent deserialization of objects across different application versions.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The user's password, securely hashed using a cryptographic algorithm.
     * <p>Never stored in plaintext.</p>
     */
    private String password;
    
    /**
     * The unique username identifying the account.
     */
    private String username;
    
    /**
     * Stores the user's favorite transit entities, such as routes and stops.
     */
    private final Favorites favorites;

    
    
    /**
     * Constructs a new {@code Account} instance with a securely hashed password
     * and a specified username. Initializes an empty set of favorites.
     *
     * @param password the plaintext password to be securely hashed
     * @param username the desired username for the account
     * @throws Exception if password hashing fails due to internal cryptographic issues
     */
    protected Account(String password, String username) throws Exception {
        this.password = PasswordUtils.generateSecurePassword(password);
        this.username = username;
        favorites = new Favorites();
    }

    
    /**
     * Returns the stored hashed password of the account.
     *
     * @return a hashed password string
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Retrieves the username associated with this account.
     *
     * @return the account's username
     */
    protected String getUsername() {
        return username;
    }

    /**
     * Returns the {@link Favorites} object associated with this account.
     *
     * @return user's {@code Favorites} instance
     */
    protected Favorites getFavorites() {
        return favorites;
    }
    
    
    /**
     * Saves a route to the user's favorite routes by its route ID.
     *
     * @param routeId the ID of the route to be added
     * @throws FavoriteAlreadyPresent if the route is already marked as a favorite
     */
    protected void saveFavoriteRoute(String routeId) throws FavoriteAlreadyPresent {
    	favorites.saveFavoriteRoute(routeId);
    }
    
    /**
     * Saves a route to the user's favorite routes.
     *
     * @param route the {@link Route} object to be added
     * @throws FavoriteAlreadyPresent if the route is already marked as a favorite
     */
    protected void saveFavoriteRoute(Route route) throws FavoriteAlreadyPresent {
    	favorites.saveFavoriteRoute(route);
    }
    
    /**
     * Saves a stop to the user's favorite stops by its stop ID.
     *
     * @param stopId the ID of the stop to be added
     * @throws FavoriteAlreadyPresent if the stop is already marked as a favorite
     */
    protected void saveFavoriteStop(String stopId) throws FavoriteAlreadyPresent {
    	favorites.saveFavoriteStop(stopId);
    }
    
    /**
     * Saves a stop to the user's favorite stops.
     *
     * @param stop the {@link Stop} object to be added
     * @throws FavoriteAlreadyPresent if the stop is already marked as a favorite
     */
    protected void saveFavoriteStop(Stop stop) throws FavoriteAlreadyPresent {
    	favorites.saveFavoriteStop(stop);
    }
    
    /**
     * Removes a stop from the user's favorites.
     *
     * @param stop the {@link Stop} object to be removed
     */
    protected void reomvefavoriteStop(Stop stop) {
    	favorites.removeFavoriteStop(stop);
    }
    
    /**
     * Removes a stop from the user's favorites using its stop ID.
     *
     * @param stopId the ID of the stop to be removed
     */
    protected void reomvefavoriteStop(String stopId) {
    	favorites.removeFavoriteStop(stopId);
    }
    
    /**
     * Removes a route from the user's favorites.
     *
     * @param route the {@link Route} object to be removed
     */
    protected void reomvefavoriteRoute(Route route) {
    	favorites.removeFavoriteRoute(route);
    }

    /**
     * Serializes the current {@code Account} object and writes it to a file.
     *
     * @param filename the name of the file to write the serialized object to
     * @throws IOException if an I/O error occurs during file creation or writing
     */
    public void serializeToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    /**
     * Reads and deserializes an {@code Account} object from a file.
     *
     * @param filename the path to the file containing the serialized object
     * @return a deserialized {@code Account} instance
     * @throws IOException if the file cannot be read or the stream is corrupted
     * @throws ClassNotFoundException if the {@code Account} class cannot be resolved
     */
    public static Account deserializeFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Account) ois.readObject();
        }
    }
    
    /**
     * Returns the serialization version UID for this class.
     *
     * @return the {@code serialVersionUID} used for versioning
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}

