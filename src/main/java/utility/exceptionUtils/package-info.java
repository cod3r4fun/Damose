/**
 * Provides custom exception classes used across the application to handle
 * specific error conditions related to user actions, data fetching, and validation.
 * <p>
 * This package contains checked exceptions that enforce explicit error handling
 * for scenarios such as duplicate entries, missing data sources, and invalid operations.
 * </p>
 *
 * <p>Exceptions included:
 * <ul>
 *	 <li>{@link DuplicationNotAccepted} - Indicates a uniqueness constraint violated.
 *   <li>{@link FavoriteAlreadyPresent} - Indicates an attempt to add a duplicate favorite.</li>
 *   <li>{@link FetcherGTFS_RTNonExistent} - Indicates absence of a GTFS real-time data fetcher.</li>
 *   <li>{@link PasswordNotEqual} - Indicates password mismatch during verification.</li>
 *   <li>{@link RouteNonExistentException} - Indicates a requested route does not exist.</li>
 * </ul>
 * </p>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
package utility.exceptionUtils;