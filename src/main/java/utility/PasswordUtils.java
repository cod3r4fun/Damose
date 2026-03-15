// class written by chatGPT

package utility;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;



/**
 * Utility class for secure password hashing and verification using PBKDF2 with HMAC-SHA256.
 *
 * <p>This class provides methods to generate salted password hashes and verify them securely.
 * It is suitable for storing passwords in authentication systems where one-way, computationally
 * expensive password protection is required.</p>
 *
 * <h2>Security Properties</h2>
 * <ul>
 *   <li>Hashing algorithm: PBKDF2 with HMAC-SHA256</li>
 *   <li>Iterations: 65,536 (configurable in source)</li>
 *   <li>Salt: 128-bit (16 bytes) random value per password</li>
 *   <li>Hash length: 256 bits (Base64-encoded)</li>
 *   <li>Hash format: {@code base64(salt):base64(hash)}</li>
 * </ul>
 *
 * <h2>Recommended Usage</h2>
 * <pre>{@code
 * // Store password
 * String storedHash = PasswordUtils.generateSecurePassword("myPassword123");
 *
 * // Later: verify login
 * boolean matches = PasswordUtils.verifyPassword("myPassword123", storedHash);
 * }</pre>
 *
 * <p><b>Note:</b> Never store plaintext passwords. This implementation provides a strong
 * but local-only password hashing system. For high-security environments, consider using
 * hardware-backed secrets or password storage frameworks like Argon2 or bcrypt.</p>
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class PasswordUtils {
	
	/** Length of the randomly generated salt in bytes (128 bits). */
    private static final int SALT_LENGTH = 16; 
    
    /** Length of the resulting hash in bits. */
    private static final int HASH_LENGTH = 256; 
    
    /** Number of iterations for PBKDF2 (affects computational cost). */
    private static final int ITERATIONS = 65536;

    
    /**
     * Generates a secure salted password hash using PBKDF2 with HMAC-SHA256.
     *
     * <p>This method generates a new random salt, hashes the password, and returns a string
     * of the format {@code base64(salt):base64(hash)}.</p>
     *
     * @param password the plaintext password to hash
     * @return a string containing salt and hash, separated by a colon
     * @throws Exception if a cryptographic operation fails
     */
    public static String generateSecurePassword(String password) throws Exception {
        byte[] salt = generateSalt();
        String hash = hashPassword(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    
    /**
     * Verifies a password against a previously stored salted hash.
     *
     * <p>Internally, this extracts the salt from the stored string, rehashes the input
     * password with the same parameters, and compares the hashes securely.</p>
     *
     * @param password the input password to verify
     * @param stored the stored password string in {@code base64(salt):base64(hash)} format
     * @return {@code true} if the password matches, {@code false} otherwise
     * @throws Exception if the input is malformed or hashing fails
     */
    public static boolean verifyPassword(String password, String stored) throws Exception {
        String[] parts = stored.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String hashOfInput = hashPassword(password.toCharArray(), salt);
        return hashOfInput.equals(parts[1]);
    }
    
    
    /**
     * Generates a cryptographically secure 128-bit random salt.
     *
     * @return a random salt byte array
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    
    /**
     * Hashes the provided password using PBKDF2 with HMAC-SHA256.
     *
     * <p>This method uses the provided salt, iteration count, and hash length to produce
     * a Base64-encoded string representation of the hash.</p>
     *
     * @param password the password to hash, as a character array
     * @param salt the salt to use for hashing
     * @return Base64-encoded hash
     * @throws NoSuchAlgorithmException if the PBKDF2 algorithm is unavailable
     * @throws InvalidKeySpecException if the key specification is invalid
     */
    private static String hashPassword(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
