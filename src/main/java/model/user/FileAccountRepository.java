// written by chatGPT

package model.user;

import utility.CryptoUtils;
import utility.SysConstants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * Concrete implementation of {@link AccountRepository} that stores and retrieves account data
 * from a local encrypted file on disk.
 *
 * <p>Accounts are serialized and encrypted using {@link CryptoUtils} before being saved,
 * and decrypted and deserialized when loaded. This class ensures secure, file-based persistence
 * for account objects.
 *
 * <p>By default, account data is stored in the {@code accounts_data/accounts.enc} file,
 * but this can be customized via the constructor.
 *
 * <p><strong>Note:</strong> This implementation assumes the provided encryption utilities
 * are properly configured and may throw exceptions related to IO or cryptography.
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */

public class FileAccountRepository implements AccountRepository {
	/**
	 * Default name of the folder where account data is stored.
	 * 
	 * <p>Used when no custom folder is provided during repository construction.
	 */
    private static final String DEFAULT_DATA_FOLDER = "accounts_data";
    
    /**
     * Default file name for storing encrypted account data.
     * 
     * <p>Used when no custom file name is provided during repository construction.
     */
    private static final String DEFAULT_DATA_FILE = "accounts.enc";

    /**
     * Path to the folder on disk where the account data file is stored.
     *
     * <p>Resolved from the working directory and either a default or provided folder name.
     */
    private final Path dataFolder;
    
    /**
     * Path to the encrypted account data file within the {@code dataFolder}.
     *
     * <p>Determined using the file name (default or custom) and used for all read/write operations.
     */
    private final Path dataFile;
    
    /**
     * Constructs a repository using a custom folder and file name for account storage.
     *
     * @param folderName the name of the folder where account data will be stored
     * @param fileName   the name of the file where encrypted account data will be written
     */

    public FileAccountRepository(String folderName, String fileName) {
        this.dataFolder = Path.of(SysConstants.CURRENTDIR, folderName);
        this.dataFile = dataFolder.resolve(fileName);
    }
    
    /**
     * Constructs a repository using the default folder and file:
     * {@code accounts_data/accounts.enc}.
     */
    public FileAccountRepository() {
        this(DEFAULT_DATA_FOLDER, DEFAULT_DATA_FILE);
    }

    
    /**
     * Saves the given list of accounts to an encrypted file on disk.
     * 
     * <p>This method serializes the list, encrypts the resulting byte stream,
     * and writes it to a file in the configured data directory. If the directory
     * does not exist, it will be created.
     *
     * @param accounts the list of account objects to persist
     * @throws Exception if an I/O or encryption error occurs
     */
    @Override
    public void save(List<Account> accounts) throws Exception {
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(accounts);
        }
        String encrypted = CryptoUtils.encrypt(baos.toByteArray());

        try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {
            writer.write(encrypted);
        }
    }

    /**
     * Loads and returns the list of accounts from the encrypted file on disk.
     * 
     * <p>The method reads the encrypted file, decrypts it, and deserializes
     * the byte stream into a list of {@code Account} objects.
     * 
     * <p>If no file exists, an empty list is returned.
     *
     * @return list of {@code Account} objects loaded from disk
     * @throws Exception if a decryption, deserialization, or I/O error occurs
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Account> load() throws Exception {
        if (!Files.exists(dataFile)) {
            return new ArrayList<>();
        }

        String encrypted;
        try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
            encrypted = reader.readLine();
        }
        byte[] decrypted = CryptoUtils.decrypt(encrypted);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted))) {
            return (List<Account>) ois.readObject();
        }
    }
}

