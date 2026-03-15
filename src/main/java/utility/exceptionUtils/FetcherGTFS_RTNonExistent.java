package utility.exceptionUtils;


/**
 * Exception thrown when the GTFS-RT fetcher does not exist or cannot be found.
 * 
 * <p>This exception indicates that an operation requiring a GTFS-RT fetcher
 * failed because the fetcher instance was not available.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class FetcherGTFS_RTNonExistent extends Throwable{

	private static final long serialVersionUID = 1L;

}
