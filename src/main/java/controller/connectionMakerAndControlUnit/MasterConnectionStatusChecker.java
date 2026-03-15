package controller.connectionMakerAndControlUnit;

import utility.Observer;

/**
 * Singleton class responsible for aggregating and managing the overall connection
 * status by monitoring connection checkers, specifically {@link OfflineConnectionChecker}.
 * 
 * <p>This class implements the {@link Observer} interface and updates its connection
 * status based on the state of the monitored controllers.
 * 
 * <p>The update strategy is to check the offline connection checker status.
 * 
 * <p>This class exposes methods to get and set the overall connection status,
 * allowing other components to query or update the connection state as needed.
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Example usage: attach the master checker to offline checker, trigger update,
 * // and then retrieve overall connection status.
 * String url = "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
 * OfflineConnectionChecker.getSingleton().addURL(url);
 * OfflineConnectionChecker.getSingleton().attach(MasterConnectionStatusChecker.getSingleton());
 * OfflineConnectionChecker.getSingleton().update();
 * 
 * System.out.println(MasterConnectionStatusChecker.getSingleton().isConnectionActive());
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * This class is a singleton and the internal state variable {@code connectionActive}
 * is accessed without explicit synchronization. If used in multithreaded environments,
 * additional synchronization might be necessary.
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * @see utility.Observer
 * @see OfflineConnectionChecker
 * @see RealTimeConnectionChecker
 */
public class MasterConnectionStatusChecker implements Observer {
    
    /**
     * Stores the current overall connection status.
     */
    private boolean connectionActive;
    
    /**
     * Singleton instance of {@code MasterConnectionStatusChecker}.
     */
    private static final MasterConnectionStatusChecker Singleton = new MasterConnectionStatusChecker();
    
    /**
     * Private constructor to enforce singleton pattern.
     * Initializes connection status to inactive (false).
     */
    private MasterConnectionStatusChecker() {
        connectionActive = false;
    }
    
    /**
     * Called when an observed subject changes.
     * Delegates the update to check controllers' statuses.
     */
    @Override
    public void update() {
        this.checkControllersStatus();
    }
    
    /**
     * Checks the connection status of monitored controllers and updates the overall
     * connection status accordingly.
     *   <li> Updates from {@link OfflineConnectionChecker}</li>
     * </ol>
     */
    private void checkControllersStatus() {
            connectionActive = OfflineConnectionChecker.getSingleton().isConnectionActive();
    }
    
    /**
     * Returns whether the overall connection is currently active.
     * 
     * @return true if the connection is active, false otherwise
     */
    public boolean isConnectionActive() {
        return connectionActive;
    }
    
    /**
     * Sets the overall connection active status.
     * 
     * @param connectionIsActive true if connection is active, false otherwise
     */
    public void setConnectionIsActive(boolean connectionIsActive) {
        this.connectionActive = connectionIsActive;
    }
    
    /**
     * Returns the singleton instance of the {@code MasterConnectionStatusChecker}.
     * 
     * @return the singleton instance
     */
    public static MasterConnectionStatusChecker getSingleton() {
        return Singleton;
    }
    
    /**
     * Main method demonstrating example usage of this class.
     * 
     * <p>This example adds a URL to the {@link OfflineConnectionChecker},
     * attaches the master checker as an observer to it, triggers an update,
     * and then prints the connection status.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String url = "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
        OfflineConnectionChecker.getSingleton().addURL(url);
        OfflineConnectionChecker.getSingleton().attach(MasterConnectionStatusChecker.getSingleton());
        OfflineConnectionChecker.getSingleton().update();
        
        System.out.println(MasterConnectionStatusChecker.getSingleton().isConnectionActive());
    }
}
