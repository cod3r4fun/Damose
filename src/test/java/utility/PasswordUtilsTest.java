package utility;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void testPasswordHashAndVerify_validPassword_shouldMatch() throws Exception {
        String password = "Str0ngP@ssw0rd!";
        String hash = PasswordUtils.generateSecurePassword(password);
        assertTrue(PasswordUtils.verifyPassword(password, hash));
    }

    @Test
    void testPasswordVerify_wrongPassword_shouldNotMatch() throws Exception {
        String password = "Str0ngP@ssw0rd!";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtils.generateSecurePassword(password);
        assertFalse(PasswordUtils.verifyPassword(wrongPassword, hash));
    }

    @Test
    void testPasswordHash_randomness_shouldProduceDifferentHashesForSamePassword() throws Exception {
        String password = "password123";
        Set<String> hashes = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            hashes.add(PasswordUtils.generateSecurePassword(password));
        }
        // Should be all unique due to salt
        assertEquals(10, hashes.size());
    }

    @Test
    void testVerifyPassword_malformedStoredHash_missingColon_shouldThrow() {
        String malformed = "justbase64nosplit";
        assertThrows(Exception.class, () -> PasswordUtils.verifyPassword("any", malformed));
    }

    @Test
    void testVerifyPassword_malformedBase64_shouldThrow() {
        String malformed = "%%%invalidbase64%%:%%%invalidhash%%";
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("any", malformed));
    }

    @Test
    void testVerifyPassword_nullInputs_shouldThrow() {
        assertThrows(NullPointerException.class, () -> PasswordUtils.verifyPassword(null, null));
        assertThrows(NullPointerException.class, () -> PasswordUtils.verifyPassword("pass", null));
        assertThrows(NullPointerException.class, () -> PasswordUtils.verifyPassword(null, "salt:hash"));
    }

    @Test
    void testGenerateSecurePassword_nullPassword_shouldThrow() {
        assertThrows(NullPointerException.class, () -> PasswordUtils.generateSecurePassword(null));
    }

    @Test
    void testHashingProducesSameOutputWithSameSalt() throws Exception {
        String password = "consistentPassword";
        byte[] fixedSalt = new byte[16];
        fixedSalt[0] = 42; // ensure not all zeros

        // Accessing hashPassword via reflection
        var method = PasswordUtils.class.getDeclaredMethod("hashPassword", char[].class, byte[].class);
        method.setAccessible(true);
        String hash1 = (String) method.invoke(null, password.toCharArray(), fixedSalt);
        String hash2 = (String) method.invoke(null, password.toCharArray(), fixedSalt);

        assertEquals(hash1, hash2);
    }

    @Test
    void testDifferentPasswordsShouldNeverMatch() throws Exception {
        String p1 = "password1";
        String p2 = "password2";
        String hash1 = PasswordUtils.generateSecurePassword(p1);
        String hash2 = PasswordUtils.generateSecurePassword(p2);
        assertFalse(PasswordUtils.verifyPassword(p2, hash1));
        assertFalse(PasswordUtils.verifyPassword(p1, hash2));
    }

    @Test
    void testVeryLongPassword_shouldStillWork() throws Exception {
        String longPassword = "A".repeat(10_000); // 10,000 characters
        String hash = PasswordUtils.generateSecurePassword(longPassword);
        assertTrue(PasswordUtils.verifyPassword(longPassword, hash));
    }

    @Test
    void testEmptyPassword_shouldWorkButWarnable() throws Exception {
        String emptyPassword = "";
        String hash = PasswordUtils.generateSecurePassword(emptyPassword);
        assertTrue(PasswordUtils.verifyPassword(emptyPassword, hash));
    }

}
