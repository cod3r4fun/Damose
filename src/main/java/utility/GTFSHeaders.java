package utility;

//documentation written with the help of chatGPT


/**
 * The {@code GTFSHeaders} class defines a set of constant values representing 
 * the standard column headers used in GTFS (General Transit Feed Specification) files.
 * 
 * <p>Each GTFS file (e.g., {@code stops.txt}, {@code routes.txt}, {@code trips.txt}, etc.) 
 * contains specific headers that must be used when parsing and mapping data fields. 
 * This class provides a reusable and type-safe reference to those headers, reducing 
 * the risk of typos and ensuring consistency throughout the codebase.
 * 
 * <p>It also includes grouped string arrays for each GTFS file, allowing for easy 
 * batch processing or mapping operations when reading the corresponding files.
 *
 * <p>Intended for use with file-reading utilities such as {@link ReadFile}, and 
 * particularly when defining the {@code headersInOrder} parameter.
 * 
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * ReadFile.readAndDoWithFirstLineMapper("stops.txt", processor, ",", GTFSHeaders.STOPS);
 * }</pre>
 *
 * @see ReadFile
 * @see OperationOnLineMapped
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class GTFSHeaders {

    /** Header for stop name in {@code stops.txt}. */
    public static final String STOP1 = "stop_name";

    /** Header for stop ID in {@code stops.txt}. */
    public static final String STOP2 = "stop_id";

    /** Header for stop latitude in {@code stops.txt}. */
    public static final String STOP3 = "stop_lat";

    /** Header for stop longitude in {@code stops.txt}. */
    public static final String STOP4 = "stop_lon";

    /** Grouped array of all expected headers in {@code stops.txt}. */
    public static final String[] STOPS = { STOP1, STOP2, STOP3, STOP4 };


    /** Header for route ID in {@code routes.txt}. */
    public static final String ROUTE1 = "route_id";

    /** Header for route short name in {@code routes.txt}. */
    public static final String ROUTE2 = "route_short_name";

    /** Header for route type in {@code routes.txt}. */
    public static final String ROUTE3 = "route_type";

    /** Header for route URL in {@code routes.txt}. */
    public static final String ROUTE4 = "route_url";

    /** Grouped array of all expected headers in {@code routes.txt}. */
    public static final String[] ROUTES = { ROUTE1, ROUTE2, ROUTE3, ROUTE4 };


    /** Header for route ID in {@code trips.txt}. */
    public static final String TRIP1 = "route_id";

    /** Header for service ID in {@code trips.txt}. */
    public static final String TRIP2 = "service_id";

    /** Header for trip ID in {@code trips.txt}. */
    public static final String TRIP3 = "trip_id";

    /** Header for trip headsign in {@code trips.txt}. */
    public static final String TRIP4 = "trip_headsign";

    /** Header for direction ID in {@code trips.txt}. */
    public static final String TRIP5 = "direction_id";

    /** Grouped array of all expected headers in {@code trips.txt}. */
    public static final String[] TRIPS = { TRIP1, TRIP2, TRIP3, TRIP4, TRIP5 };


    /** Header for service ID in {@code calendar_dates.txt}. */
    public static final String CALENDARDATES1 = "service_id";

    /** Header for service date in {@code calendar_dates.txt}. */
    public static final String CALENDARDATES2 = "date";

    /** Header for exception type in {@code calendar_dates.txt}. */
    public static final String CALENDARDATES3 = "exception_type";

    /** Grouped array of all expected headers in {@code calendar_dates.txt}. */
    public static final String[] CALENDARDATES = { CALENDARDATES1, CALENDARDATES2, CALENDARDATES3 };


    /** Header for trip ID in {@code stop_times.txt}. */
    public static final String STOPTIME1 = "trip_id";

    /** Header for arrival time in {@code stop_times.txt}. */
    public static final String STOPTIME2 = "arrival_time";

    /** Header for departure time in {@code stop_times.txt}. */
    public static final String STOPTIME3 = "departure_time";

    /** Header for stop ID in {@code stop_times.txt}. */
    public static final String STOPTIME4 = "stop_id";

    /** Header for stop sequence in {@code stop_times.txt}. */
    public static final String STOPTIME5 = "stop_sequence";

    /** Grouped array of all expected headers in {@code stop_times.txt}. */
    public static final String[] STOPTIMES = { STOPTIME1, STOPTIME2, STOPTIME3, STOPTIME4, STOPTIME5 };

    // No constructor to ensure this utility class is not instantiated.
    private GTFSHeaders() {
        throw new UnsupportedOperationException("GTFSHeaders is a utility class and should not be instantiated.");
    }
}

