
package model.user;

import utility.PasswordUtils;
import utility.exceptionUtils.FavoriteAlreadyPresent;
import utility.exceptionUtils.PasswordNotEqual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.vehicles.Route;
import model.vehicles.Stop;



/**
 * Singleton class responsible for managing user accounts, authentication, and
 * persistence of user data including favorites. It serves as the centralized
 * service for account lifecycle operations including creation, login, and favorite management.
 * 
 * <p>This class is thread-safe and supports lazy initialization of its singleton instance.
 * It utilizes an {@link AccountRepository} for durable storage, supporting pluggable backends.</p>
 * 
 * <p>The currently authenticated account is maintained internally, enabling user-specific
 * data access and manipulation.</p>
 * 
 * <p>Account data is automatically persisted on JVM shutdown via a shutdown hook.</p>
 * 
 * @see Account
 * @see AccountRepository
 * @see FileAccountRepository
 * @see utility.PasswordUtils
 * @since 1.0
 * @version 1.0
 */
public class AccountManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 
     * Singleton instance of AccountManager.
     * Uses volatile keyword to ensure visibility and safe publication.
     */
    private static volatile AccountManager instance;

    /** Repository for persistent storage of accounts. */
    private final AccountRepository repository;
    
    /** In-memory list of all loaded accounts. */
    private final List<Account> accounts;
    
    /** The currently logged-in account. */
    private Account currentAccount;

    
    // constructor and instance-getters written by chatGPT
    
    
    /**
     * Private constructor. Initializes the manager by loading accounts from the repository.
     * If no accounts exist, creates a default "Guest" account.
     * Registers a shutdown hook to persist accounts on JVM termination.
     * 
     * @param repository the {@link AccountRepository} instance to use for persistence
     */
    private AccountManager(AccountRepository repository) {
        this.repository = repository;
        List<Account> loaded;
        try {
            loaded = repository.load();
        } catch (Exception e) {
            e.printStackTrace();
            loaded = new ArrayList<>();
        }
        this.accounts = loaded;
        if (accounts.isEmpty()) {
            try {
                createAnAccount("Guest", "?", "?");
            } catch (Exception | PasswordNotEqual e) {
                e.printStackTrace();
            }
        }
        this.currentAccount = accounts.getFirst();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                repository.save(accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    
    /**
     * Returns the singleton instance of {@code AccountManager} using the default repository.
     * Initializes the instance if it does not already exist.
     * 
     * @return the singleton {@code AccountManager} instance
     */
    public static AccountManager getInstance() {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager(new FileAccountRepository());
                }
            }
        }
        return instance;
    }

    /**
     * Returns the singleton instance of {@code AccountManager} using a custom repository.
     * Intended primarily for testing or alternative persistence mechanisms.
     * Initializes the instance if it does not already exist.
     * 
     * @param repository the custom {@link AccountRepository} implementation to use
     * @return the singleton {@code AccountManager} instance
     */
    public static AccountManager getInstance(AccountRepository repository) {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager(repository);
                }
            }
        }
        return instance;
    }

    
    /**
     * Creates a new account with the specified username and password.
     * Password confirmation is required to prevent accidental mismatch.
     * 
     * <p>On successful creation, the account is added to the internal list and
     * the repository is updated.</p>
     * 
     * @param username the username for the new account; should be unique
     * @param password the plaintext password for the new account
     * @param password1 password confirmation; must be equal to {@code password}
     * @throws Exception if persistence fails during save
     * @throws PasswordNotEqual if {@code password} and {@code password1} do not match
     */
    public void createAnAccount(String username, String password, String password1) throws Exception, PasswordNotEqual {
        if (password.equals(password1)) {
            var acc = new Account(password, username);
            accounts.add(acc);
            repository.save(accounts);
        } else throw new PasswordNotEqual();
    }

    /**
     * Attempts to log in an existing user by validating credentials.
     * Sets the currently authenticated account on success.
     * 
     * @param username the username of the account to log in
     * @param password the plaintext password to verify
     * @throws Exception if the username does not exist or password verification fails
     */
    public void logIn(String username, String password) throws Exception {
        for (var account : accounts) {
            if (username.equals(account.getUsername()) && PasswordUtils.verifyPassword(password, account.getPassword())) {
                currentAccount = account;
                return;
            }
        }
        throw new Exception();
    }

    /**
     * Returns the username of the currently authenticated account.
     * 
     * @return the username of the current account
     */
    public String getCurrentAccountUsername() {
        return currentAccount.getUsername();
    }
    
    /**
     * Returns the {@link Favorites} of the currently authenticated account.
     * 
     * @return the favorites object containing saved stops and routes
     */
    public Favorites getCurrentFavorites() {
    	 return currentAccount.getFavorites();
    }
    
    /**
     * Adds a route to the current account's favorites by route ID and persists changes.
     * 
     * @param routeId the ID of the route to add as favorite
     * @throws Exception if persistence fails during save
     * @throws FavoriteAlreadyPresent if the route is already favorited
     */
    public void saveFavoriteRoute(String routeId) throws Exception, FavoriteAlreadyPresent {
    	currentAccount.saveFavoriteRoute(routeId);
    	repository.save(accounts);
    }
    
    /**
     * Adds a route to the current account's favorites by route object and persists changes.
     * 
     * @param route the {@link Route} to add as favorite
     * @throws Exception if persistence fails during save
     * @throws FavoriteAlreadyPresent if the route is already favorited
     */
    public void saveFavoriteRoute(Route route) throws Exception, FavoriteAlreadyPresent {
    	currentAccount.saveFavoriteRoute(route);
    	repository.save(accounts);
    }
    
    /**
     * Adds a stop to the current account's favorites by stop ID and persists changes.
     * 
     * @param stopId the ID of the stop to add as favorite
     * @throws Exception if persistence fails during save
     * @throws FavoriteAlreadyPresent if the stop is already favorited
     */
    public void saveFavoriteStop(String stopId) throws Exception, FavoriteAlreadyPresent {
    	currentAccount.saveFavoriteStop(stopId);
    	repository.save(accounts);
    }
    
    /**
     * Adds a stop to the current account's favorites by stop object and persists changes.
     * 
     * @param stop the {@link Stop} to add as favorite
     * @throws Exception if persistence fails during save
     * @throws FavoriteAlreadyPresent if the stop is already favorited
     */
    public void saveFavoriteStop(Stop stop) throws Exception, FavoriteAlreadyPresent {
    	currentAccount.saveFavoriteStop(stop);
    	repository.save(accounts);
    }
    
    /**
     * Removes a stop from the current account's favorites by stop object and persists changes.
     * 
     * @param stop the {@link Stop} to remove from favorites
     * @throws Exception if persistence fails during save
     */
    public void removeFavoriteStop(Stop stop) throws Exception {
    	currentAccount.reomvefavoriteStop(stop);
    	repository.save(accounts);
    }
    
    /**
     * Removes a stop from the current account's favorites by stop ID and persists changes.
     * 
     * @param stopId the ID of the stop to remove from favorites
     * @throws Exception if persistence fails during save
     */
    public void removeFavoriteStop(String stopId) throws Exception {
    	currentAccount.reomvefavoriteStop(stopId);
    	repository.save(accounts);
    }
    
    /**
     * Removes a route from the current account's favorites by route object and persists changes.
     * 
     * @param route the {@link Route} to remove from favorites
     * @throws Exception if persistence fails during save
     */
    public void removeFavoriteRoute(Route route) throws Exception {
    	currentAccount.reomvefavoriteRoute(route);
    	repository.save(accounts);
    }

}

