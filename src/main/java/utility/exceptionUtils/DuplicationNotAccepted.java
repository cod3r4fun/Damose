package utility.exceptionUtils;

//documentation written with the help of chatGPT

/**
 * Exception indicating that a duplication was detected where it is not permitted.
 * 
 * <p>This custom exception can be used to signal scenarios where a unique constraint
 * is violated, such as encountering duplicate entries in a file, database, collection, or during validation logic.
 * 
 * <p>Typical use cases might include:
 * <ul>
 *   <li>Parsing GTFS files where duplicate IDs (e.g., {@code stop_id}, {@code trip_id}) are not allowed</li>
 *   <li>Enforcing uniqueness in sets, maps, or during data ingestion</li>
 *   <li>Validating configuration or input files</li>
 * </ul>
 *
 * <p>This exception extends {@link Throwable}, but it is recommended to extend {@link Exception}
 * unless you have a specific reason to make it non-standard.
 *
 * <p><strong>Suggested Change:</strong> Consider changing the superclass to {@code Exception}
 * or {@code RuntimeException} for compatibility with standard exception handling flows.
 * 
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * if (seenIds.contains(id)) {
 *     throw new DuplicationNotAccepted("Duplicate ID found: " + id);
 * }
 * }</pre>
 *
 * @see java.lang.Exception
 * @see java.lang.RuntimeException
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class DuplicationNotAccepted extends Throwable {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new DuplicationNotAccepted exception with no detail message.
     */
    public DuplicationNotAccepted() {
        super();
    }

    /**
     * Constructs a new DuplicationNotAccepted exception with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public DuplicationNotAccepted(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicationNotAccepted exception with the specified cause.
     *
     * @param cause the cause (a throwable cause saved for later retrieval)
     */
    public DuplicationNotAccepted(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DuplicationNotAccepted exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause (can be null)
     */
    public DuplicationNotAccepted(String message, Throwable cause) {
        super(message, cause);
    }
}
