package model.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.Route;
import model.vehicles.Stop;
import utility.exceptionUtils.FavoriteAlreadyPresent;
import utility.exceptionUtils.PasswordNotEqual;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory AccountRepository stub for testing persistence calls.
 */
class InMemoryAccountRepository implements AccountRepository {
    List<Account> storedAccounts = new ArrayList<>();
    boolean saveCalled = false;

    @Override
    public List<Account> load() {
        return new ArrayList<>(storedAccounts);
    }

    @Override
    public void save(List<Account> accounts) {
        storedAccounts = new ArrayList<>(accounts);
        saveCalled = true;
    }
}

public class AccountManagerTest {

    private AccountManager manager;
    private InMemoryAccountRepository repository;

    static class TestRoute extends Route {
        private final String id;
        private final String type;
        TestRoute(String id, String type) {
        	super("caso", "caso", "caso", null);
            this.id = id; this.type = type;
        }
        @Override 
        public String getRouteId() { 
        	return id; 
        	}
        @Override 
        public String getType() {
        	return type; 
        	}
        @Override 
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestRoute)) return false;
            TestRoute other = (TestRoute) o;
            return id.equals(other.id) && type.equals(other.type);
        }
        @Override 
        public int hashCode() {
        	return id.hashCode() + type.hashCode(); 
        	}
    }

    static class TestStop extends Stop {
        private final String id;
        TestStop(String id) { 
        	super("caso", "caso", new GeoPosition(23.11321f, 11.2321f));
        	this.id = id; 
        	}
        @Override 
        public String getStopId() {
        	return id; 
        	}
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestStop)) return false;
            TestStop other = (TestStop) o;
            return id.equals(other.id);
        }
        @Override 
        public int hashCode() {
        	return id.hashCode(); 
        	}
    }

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
        resetSingleton();
        manager = AccountManager.getInstance(repository);
    }

    private void resetSingleton() {
        try {
            java.lang.reflect.Field instanceField = AccountManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingletonInstanceDefaultAndCustomRepository() {
        AccountManager m1 = AccountManager.getInstance(repository);
        AccountManager m2 = AccountManager.getInstance();
        assertSame(m1, m2, "Singleton instances should be same");

        resetSingleton();
        AccountManager m3 = AccountManager.getInstance();
        assertNotNull(m3);
    }

    @Test
    void testCreateAccountSuccessAndFailPasswordMismatch() throws Exception, PasswordNotEqual {
        manager.createAnAccount("user1", "pass123", "pass123");
        assertTrue(repository.saveCalled, "Repository save should be called after account creation");

        assertThrows(PasswordNotEqual.class, () -> manager.createAnAccount("user2", "pass", "passDiff"));
    }

    @Test
    void testLoginSuccessAndFail() throws Exception, PasswordNotEqual {
        manager.createAnAccount("userX", "myPass", "myPass");

        manager.logIn("userX", "myPass");
        assertEquals("userX", manager.getCurrentAccountUsername());

        Exception ex1 = assertThrows(Exception.class, () -> manager.logIn("wrongUser", "myPass"));

        Exception ex2 = assertThrows(Exception.class, () -> manager.logIn("userX", "wrongPass"));
    }

    @Test
    void testFavoritesAddAndRemoveAndPersistence() throws Exception, PasswordNotEqual, FavoriteAlreadyPresent {
        manager.createAnAccount("favUser", "pw", "pw");
        manager.logIn("favUser", "pw");

        Route r = new TestRoute("route1", "1");
        Stop s = new TestStop("stop1");

 
        manager.saveFavoriteRoute(r);
        assertTrue(manager.getCurrentFavorites().getFavoriteMetroRoutes().contains(r));
        assertTrue(repository.saveCalled);


        manager.saveFavoriteStop(s);
        assertTrue(manager.getCurrentFavorites().getFavoriteStops().contains(s));

        
        manager.removeFavoriteRoute(r);
        assertFalse(manager.getCurrentFavorites().getFavoriteMetroRoutes().contains(r));

        
        manager.removeFavoriteStop(s);
        assertFalse(manager.getCurrentFavorites().getFavoriteStops().contains(s));
    }

    @Test
    void testSaveDuplicateFavoriteThrows() throws Exception, PasswordNotEqual, FavoriteAlreadyPresent {
        manager.createAnAccount("dupUser", "pw", "pw");
        manager.logIn("dupUser", "pw");

        Route r = new TestRoute("rDup", "1");
        manager.saveFavoriteRoute(r);

        assertThrows(FavoriteAlreadyPresent.class, () -> manager.saveFavoriteRoute(r));
    }
}
