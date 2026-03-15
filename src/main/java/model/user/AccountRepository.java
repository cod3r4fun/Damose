package model.user;

import java.util.List;


/**
 * Defines a contract for persistent storage and retrieval of {@link Account} instances.
 * <p>
 * Implementing classes must provide concrete mechanisms for:
 * <ul>
 *     <li>Persisting a collection of user accounts to a durable storage backend (e.g., file system, database, etc.)</li>
 *     <li>Restoring user accounts from storage into memory for runtime access</li>
 * </ul>
 * This interface allows for decoupling of persistence logic from business logic,
 * enabling flexible implementations suitable for various environments (e.g., desktop, web, cloud).
 *
 * @author Franco Della Negra
 * @since 1.0
 * @version 1.0
 */
public interface AccountRepository {
	
    /**
     * Persists a list of {@link Account} objects to the underlying storage mechanism.
     * <p>
     * The implementation should ensure data consistency, integrity, and (if applicable)
     * confidentiality during the persistence process. This typically involves:
     * <ul>
     *     <li>Serializing the data structure (e.g., to binary or JSON)</li>
     *     <li>Writing the data to a file or remote system</li>
     *     <li>Applying encryption or other forms of data protection</li>
     * </ul>
     * 
     * @param accounts a non-null list of {@code Account} instances to be saved;
     *                 the list may be empty but not {@code null}
     * @throws Exception if an I/O error, serialization failure, or encryption issue occurs
     */
    public void save(List<Account> accounts) throws Exception;
    
    
    /**
     * Loads and returns a list of {@link Account} objects from persistent storage.
     * <p>
     * <p>
     * Implementations are expected to:
     * <ul>
     *     <li>Read raw data from the underlying storage medium</li>
     *     <li>Decrypt and/or deserialize the data into valid {@code Account} objects</li>
     *     <li>Handle missing or corrupted data gracefully</li>
     * </ul>
     * 
     * @return a list of deserialized {@code Account} instances;
     * 
     * @throws Exception if an I/O error, deserialization failure, or decryption issue occurs
     */
    public List<Account> load() throws Exception;
}

