package controller.connectionMakerAndControlUnit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.transit.realtime.GtfsRealtime;

/**
 * FetcherGTFS_RT handles downloading GTFS-Realtime feeds and parsing them into a raw
 * {@link GtfsRealtime.FeedMessage} object. It does <em>not</em> interpret individual
 * entities; that responsibility is delegated to a separate parser/interpreter class.
 *
 * <p>Each instance maintains its source URL and stores the last fetched
 * {@code FeedMessage}. Instances are tracked in a thread-safe global list for monitoring
 * or batch operations.</p>
 *
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>Create instance with a URL, URI, or String specifying the GTFS-RT endpoint.</li>
 *   <li>Call {@link #update()} to perform a network fetch and parse the protobuf payload
 *       into {@link GtfsRealtime.FeedMessage}.</li>
 *   <li>Retrieve the raw feed via {@link #getFeed()} and pass it to an entity parser.</li>
 * </ol>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class FetcherGTFS_RT {
    /**
     * The URL pointing to the GTFS-Realtime data source.
     */
    private final URL referenceGTFS_RT;
    
    /**
     * associated city
     */
    private final String city;

    /**
     * Counter tracking how many FetcherGTFS_RT objects have been created.
     */
    private static int allObjectsNumber = 0;

    /**
     * Unique ID for this FetcherGTFS_RT instance.
     */
    private final int thisIdObject;

    /**
     * Thread-safe list holding all FetcherGTFS_RT instances.
     */
    private static final CopyOnWriteArrayList<FetcherGTFS_RT> allFetchers = new CopyOnWriteArrayList<>();
    
    /**
     * The last-parsed GTFS-Realtime feed message. Populated by {@link #fetch()}.
     */
    private GtfsRealtime.FeedMessage feed;

    /**
     * Constructs a FetcherGTFS_RT with a specified URL.
     * <p>
     * Increments the global instance counter, assigns a unique ID,
     * and adds this instance to the global fetcher list.
     * </p>
     *
     * @param url the URL to fetch GTFS-RT data from
     * @param city the city associated to the URL
     */
    public FetcherGTFS_RT(URL url, String city) {
    	this.city = city;
        this.referenceGTFS_RT = url;
        this.thisIdObject = allObjectsNumber++;
        allFetchers.add(this);
    }
    
    /**
     * Constructs a FetcherGTFS_RT from a {@link URI}.
     *
     * @param uri the URI pointing to the GTFS-RT data
     * @param city the city associated to the URL
     * @throws MalformedURLException if the URI cannot be converted to a valid URL
     */
    public FetcherGTFS_RT(URI uri, String city) throws MalformedURLException {
        this(uri.toURL(), city);
    }
    
    /**
     * Constructs a FetcherGTFS_RT from a string representation of a URL.
     *
     * @param s string representing the GTFS-RT feed URL
     * @param city the city associated to the URL
     * @throws MalformedURLException if the URL is malformed
     * @throws URISyntaxException    if the string is not a valid URI
     */
    public FetcherGTFS_RT(String s, String city) throws MalformedURLException, URISyntaxException {
        this(new URI(s), city);
    }
    
    /**
     * Returns a String representing the city associated
     * 
     * @return a string representing the city
     */
    public String getCity() {
    		return city;
    }
    
    /**
     * Observer-style update trigger. Invokes {@link #fetch()} to download and parse the feed.
     */
    public void update() {
        fetch();
    }
    
    /**
     * Downloads the GTFS-Realtime feed from {@link #referenceGTFS_RT} and parses it
     * into a {@link GtfsRealtime.FeedMessage} stored in {@link #feed}.
     * <p>
     * Any {@link IOException} during download or parsing is caught and logged.
     * </p>
     */
    private void fetch() {
    	if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) {
    		try (var inputStream = referenceGTFS_RT.openStream()){
    			feed = GtfsRealtime.FeedMessage.parseFrom(inputStream);
    		} catch (IOException e){
    			e.printStackTrace();
    		}
    	}
        
    }
    
    /**
     * Returns an unmodifiable snapshot of all FetcherGTFS_RT instances created so far.
     *
     * @return a {@link List} of all fetcher instances
     */
    public static List<FetcherGTFS_RT> getAllFetchers(){
        return List.copyOf(allFetchers);
    }
    
    /**
     * Retrieves the most recently parsed GTFS-Realtime feed.
     *
     * @return the {@link GtfsRealtime.FeedMessage} parsed by the last fetch
     */
    public GtfsRealtime.FeedMessage getFeed(){
        return feed;
    }
    
    public int getThisIdObject() {
    	return thisIdObject;
    }
    
    /**
     * Main method for simple demonstration.
     * <p>
     * Creates a fetcher, invokes {@link #update()}, and prints the parsed feed.
     * </p>
     *
     * @param args command-line arguments (ignored)
     * @throws MalformedURLException if the demo URL is malformed
     * @throws URISyntaxException    if the demo string URL is not a valid URI
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws MalformedURLException, URISyntaxException, InterruptedException {
    	while (true) {
    	Thread.sleep(10000);
    	OfflineConnectionChecker.getSingleton().update();
    	MasterConnectionStatusChecker.getSingleton().update();
    	var fetcher = new FetcherGTFS_RT(
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb", "Rome"
        );
        fetcher.update();
        if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) System.out.println(fetcher.getFeed());
    	}
    }
}
