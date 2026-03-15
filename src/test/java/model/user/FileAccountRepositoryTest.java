// written by chatGPT

package model.user;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileAccountRepositoryTest {
    private static final String TEST_FOLDER = "test_accounts_data";
    private static final String TEST_FILE = "test_accounts.enc";

    private FileAccountRepository repository;
    private Path dataFile;

    @BeforeEach
    void setUp() throws IOException {
        repository = new FileAccountRepository(TEST_FOLDER, TEST_FILE);
        dataFile = Path.of(System.getProperty("user.dir"), TEST_FOLDER, TEST_FILE);

        // Clean up before test
        if (Files.exists(dataFile)) {
            Files.delete(dataFile);
        }
        Path folder = dataFile.getParent();
        if (Files.exists(folder) && folder.toFile().list().length == 0) {
            Files.delete(folder);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up after test
        if (Files.exists(dataFile)) {
            Files.delete(dataFile);
        }
        Path folder = dataFile.getParent();
        if (Files.exists(folder) && folder.toFile().list().length == 0) {
            Files.delete(folder);
        }
    }

    @Test
    void testLoadReturnsEmptyWhenFileDoesNotExist() throws Exception {
        List<Account> accounts = repository.load();
        assertNotNull(accounts, "Loaded list should not be null");
        assertTrue(accounts.isEmpty(), "Loaded list should be empty when file is missing");
    }

    @Test
    void testSaveCreatesFile() throws Exception {
        List<Account> accountsToSave = new ArrayList<>();
        accountsToSave.add(new Account("userA", "passA"));
        repository.save(accountsToSave);

        assertTrue(Files.exists(dataFile), "Data file should exist after save");
        assertTrue(Files.size(dataFile) > 0, "Data file should not be empty");
    }

    @Test
    void testSaveAndLoadMultipleAccounts() throws Exception {
        List<Account> accountsToSave = List.of(
            new Account("user1", "password1"),
            new Account("user2", "password2"),
            new Account("user3", "password3")
        );

        repository.save(accountsToSave);
        List<Account> loadedAccounts = repository.load();

        assertEquals(accountsToSave.size(), loadedAccounts.size(), "Loaded account count mismatch");

        for (int i = 0; i < accountsToSave.size(); i++) {
            assertEquals(accountsToSave.get(i).getUsername(), loadedAccounts.get(i).getUsername(), "Username mismatch");
            assertEquals(accountsToSave.get(i).getPassword(), loadedAccounts.get(i).getPassword(), "Password mismatch");
        }
    }

    @Test
    void testLoadThrowsOnInvalidEncryptedData() throws Exception {
        // Write invalid data directly to file to simulate corruption
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, "invalid encrypted data");

        Exception exception = assertThrows(Exception.class, () -> {
            repository.load();
        });

        String expectedMessagePart = "javax.crypto"; // or part of CryptoUtils decrypt exception message
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage != null && actualMessage.contains(expectedMessagePart) || actualMessage.toLowerCase().contains("illegal"),
                "Expected decryption failure but got: " + actualMessage);
    }
}
