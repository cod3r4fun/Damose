/**
 * Provides the core user account management functionality including
 * account creation, authentication, favorites management, and persistence.
 *
 * <p>This package contains classes that handle user credentials securely,
 * maintain user session state, and interact with persistent storage
 * through pluggable repository interfaces.</p>
 * 
 * <p>Key components include:</p>
 * <ul>
 *   <li>{@link model.user.Account} - Represents a user account with credentials and favorites.</li>
 *   <li>{@link model.user.AccountManager} - Singleton service managing account lifecycle, login, and favorites.</li>
 *   <li>{@link model.user.AccountRepository} - Interface for persisting accounts.</li>
 *   <li>{@link model.user.FileAccountRepository} - File-based encrypted storage implementation.</li>
 * </ul>
 * 
 * <p>Security considerations:</p>
 * <ul>
 *   <li>Passwords are securely hashed using utilities in {@code utility.PasswordUtils}.</li>
 *   <li>Account data is encrypted at rest via the repository implementation.</li>
 * </ul>
 * 
 * <p>This package follows best practices for thread safety, immutability where applicable,
 * and clean separation of concerns.</p>
 *
 * @since 1.0
 * @version 1.0
 * @author Franco Della Negra
 */
package model.user;