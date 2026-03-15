	package controller.connectionMakerAndControlUnit;
	
	import java.io.IOException;
	import java.net.HttpURLConnection;
	import java.net.MalformedURLException;
	import java.net.URI;
	import java.net.URISyntaxException;
	import java.net.URL;
	import java.util.concurrent.CopyOnWriteArrayList;
	
	import utility.Observer;
	import utility.Subject;
	
	/**
	 * Singleton class that checks internet connection availability using a list of URLs.
	 * Implements both Subject and Observer interfaces.
	 * <p>
	 * It notifies all attached observers when the connection status changes.
	 * It also reacts to updates from other subjects by re-checking the connection.
	 * </p>
	 * 
	 * Thread-safe collections are used to support safe concurrent access.
	 * 
	 * @author Franco Della Negra
	 * @version 1.0
	 * @since 1.0
	 */
	public class OfflineConnectionChecker implements Subject, Observer {
	
	    /** Indicates whether a valid internet connection is currently detected. */
	    private boolean connectionActive;
	
	    /**
	     * The list of observers that are subscribed to be notified when the connection
	     * status changes.
	     */
	    private final CopyOnWriteArrayList<Observer> observers;
	
	    /**
	     * The list of URLs that are used to test whether a network connection is active.
	     */
	    private final CopyOnWriteArrayList<URL> urls;
	
	    
	    /** Singleton instance of the connection checker. */
	    private static final OfflineConnectionChecker singleton = new OfflineConnectionChecker();
	
	    /**
	     * Private constructor to enforce singleton usage.
	     * Initializes the observer and URL lists, and defaults connection state.
	     */
	    private OfflineConnectionChecker() {
	        connectionActive = false;
	        observers = new CopyOnWriteArrayList<>();
	        urls = new CopyOnWriteArrayList<>();
	    }
	
	    /**
	     * Adds a {@link URL} to the list of URLs used to check connection status.
	     * 
	     * @param url the URL to be added. Null values are ignored.
	     */
	    public void addURL(URL url) {
	        if (url != null) {
	            urls.add(url);
	        }
	    }
	
	    /**
	     * Adds a {@link URI} to the list of URLs after converting it to a URL.
	     * 
	     * @param uri the URI to be converted and added. Null values are ignored.
	     */
	    public void addURL(URI uri) {
	        try {
	            if (uri != null) {
	                var url = uri.toURL();
	                urls.add(url);
	            }
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }
	    }
	
	    /**
	     * Parses a String to a {@link URI}, converts it to a URL, and adds it to the
	     * list of connection test URLs.
	     * 
	     * @param s the String to convert to a URI and add. Null or invalid inputs are ignored.
	     */
	    public void addURL(String s) {
	        if (s != null) {
	            URI uri = null;
	            try {
	                uri = new URI(s);
	                this.addURL(uri);
	            } catch (URISyntaxException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	
	    /**
	     * Checks the current connection status by trying to reach the first URL in the list.
	     * If the connection status changes, observers are notified.
	     */
	    private void checkConnectionStatus() {
	        URL url;
	        try {
	            if (urls.isEmpty()) {
	            	url = new URI("https://www.google.com/generate_204").toURL();
	            }
	
	            else {url = urls.get(0);}
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setUseCaches(false);
	            connection.setRequestMethod("HEAD");
	            connection.setConnectTimeout(3000); // 3 seconds
	            connection.setReadTimeout(3000);
	
	            int responseCode = connection.getResponseCode();
	            connectionActive = (responseCode >= 200 && responseCode < 400); // 2xx or 3xx
	
	            
	        } catch (IOException | URISyntaxException e) {
	            connectionActive = false;
	            
	        }
	    }
	
	    /**
	     * Returns whether a valid connection is currently detected.
	     * 
	     * @return true if connected, false otherwise.
	     */
	    public boolean isConnectionActive() {
	        return connectionActive;
	    }
	
	   
	
	    /**
	     * Returns the list of registered observers.
	     * 
	     * @return a thread-safe list of observers.
	     */
	    public CopyOnWriteArrayList<Observer> getObservers() {
	        return observers;
	    }
	
	    /**
	     * Returns the list of URLs used for checking connection.
	     * 
	     * @return a thread-safe list of URLs.
	     */
	    public CopyOnWriteArrayList<URL> getUrls() {
	        return urls;
	    }
	
	    /**
	     * Returns the singleton instance of this class.
	     * 
	     * @return the {@link OfflineConnectionChecker} singleton.
	     */
	    public static OfflineConnectionChecker getSingleton() {
	        return singleton;
	    }
	
	    /**
	     * Attaches an observer to be notified when connection status changes.
	     * 
	     * @param o the observer to attach.
	     */
	    @Override
	    public void attach(Observer o) {
	        observers.add(o);
	    }
	
	    /**
	     * Removes an observer from the notification list.
	     * 
	     * @param o the observer to detach.
	     */
	    @Override
	    public void detach(Observer o) {
	        observers.remove(o);
	    }
	
	    /**
	     * Notifies all observers that the connection status has changed.
	     */
	    @Override
	    public void notifyObservers() {
	        for (var obs : observers) {
	            obs.update();
	        }
	    }
	
	    /**
	     * Observer's update method implementation.
	     * Triggers a new connection status check.
	     */
	    @Override
	    public void update() {
	        this.checkConnectionStatus();
	    }
	
	    /**
	     * Main method for basic testing.
	     * Adds a sample URL and checks connection status.
	     */
	    public static void main(String[] args) {
	        String url = "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
	        OfflineConnectionChecker.getSingleton().addURL(url);
	        OfflineConnectionChecker.getSingleton().checkConnectionStatus();
	        System.out.println(OfflineConnectionChecker.getSingleton().isConnectionActive());
	    }
	}

