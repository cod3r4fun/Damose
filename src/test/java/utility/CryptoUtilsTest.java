package utility;

import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    private static final String TEST_PLAINTEXT = "This is a secret message.";

    @BeforeEach
    void resetKey() {
        // Reset the key to a known default before each test
        CryptoUtils.setKey("16CharSecretKey!");
    }

    @Test
    void testEncryptAndDecrypt_shouldReturnOriginalPlaintext() throws Exception {
        String cipherText = CryptoUtils.encrypt(TEST_PLAINTEXT.getBytes(StandardCharsets.UTF_8));
        byte[] decrypted = CryptoUtils.decrypt(cipherText);
        assertEquals(TEST_PLAINTEXT, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void testSetInvalidKey_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.setKey("short"));
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.setKey(null));
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.setKey("waytoolongforaeskey123"));
    }

    @Test
    void testDecryptWithCorruptedBase64_shouldThrow() {
        String badBase64 = "%%%badbase64%%%";
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.decrypt(badBase64));
    }

    @Test
    void testDecryptWithMissingIV_shouldThrow() {
        // Manually encode too-short data (less than 16 bytes)
        byte[] tooShort = new byte[8]; // must be at least 16 for IV
        String encoded = java.util.Base64.getEncoder().encodeToString(tooShort);
        assertThrows(Exception.class, () -> CryptoUtils.decrypt(encoded));
    }

    @Test
    void testDecryptWithWrongKey_shouldFailGracefully() throws Exception {
        String cipher = CryptoUtils.encrypt("SecretMsg".getBytes(StandardCharsets.UTF_8));
        CryptoUtils.setKey("WrongKey12345678");
        assertThrows(Exception.class, () -> CryptoUtils.decrypt(cipher));
    }

    @Test
    void testEncryptWithDifferentIVs_shouldProduceDifferentCiphertexts() throws Exception {
        String msg = "SameMessage";
        String encrypted1 = CryptoUtils.encrypt(msg.getBytes(StandardCharsets.UTF_8));
        String encrypted2 = CryptoUtils.encrypt(msg.getBytes(StandardCharsets.UTF_8));
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testDecryptAfterKeyChange_shouldFail() throws Exception {
        String encrypted = CryptoUtils.encrypt("PersistentData".getBytes(StandardCharsets.UTF_8));
        CryptoUtils.setKey("AnotherKey123456");
        assertThrows(Exception.class, () -> CryptoUtils.decrypt(encrypted));
    }

    @Test
    void testResetToDefaultKey_shouldAllowRecovery() throws Exception {
        String originalKey = "16CharSecretKey!";
        String encrypted = CryptoUtils.encrypt("Restorable".getBytes(StandardCharsets.UTF_8));
        assertThrows(Exception.class, () ->  CryptoUtils.setKey("BreakDecryption!!"));
        CryptoUtils.setKey("BreakDecryption!");
        assertThrows(Exception.class, () -> CryptoUtils.decrypt(encrypted));

        // Restore original key and try again
        CryptoUtils.setKey(originalKey);
        byte[] decrypted = CryptoUtils.decrypt(encrypted);
        assertEquals("Restorable", new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void testPreferencesPersistence() throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(CryptoUtils.class);
        String oldKey = prefs.get("aes_key", null);

        CryptoUtils.setKey("PersistedKeyABC!");
        String storedKey = prefs.get("aes_key", null);
        assertEquals("PersistedKeyABC!", storedKey);

        // Restore old key after test
        if (oldKey != null) {
            prefs.put("aes_key", oldKey);
        }
    }

    @Test
    void testEncryptEmptyData_shouldStillWork() throws Exception {
        byte[] empty = new byte[0];
        String encrypted = CryptoUtils.encrypt(empty);
        byte[] decrypted = CryptoUtils.decrypt(encrypted);
        assertEquals(0, decrypted.length);
    }

    @Test
    void testEncryptAndDecryptLongMessage() throws Exception {
        String longText = "A".repeat(100_000);
        String encrypted = CryptoUtils.encrypt(longText.getBytes(StandardCharsets.UTF_8));
        byte[] decrypted = CryptoUtils.decrypt(encrypted);
        assertEquals(longText, new String(decrypted, StandardCharsets.UTF_8));
    }
}
