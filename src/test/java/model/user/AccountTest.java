// written by chatGPT 

package model.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.Route;
import model.vehicles.Stop;
import utility.exceptionUtils.FavoriteAlreadyPresent;

import java.io.File;
import java.nio.file.Files;

class AccountTest {

    private Account account;

    // Simple stub classes for Route and Stop
    static class TestRoute extends Route {
        private final String id;
        private final String type;

        TestRoute(String id, String type) {
        	super("caso", "caso", "caso", null);
            this.id = id;
            this.type = type;
        }

        @Override public String getRouteId() { return id; }

        @Override public String getType() { return type; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestRoute)) return false;
            TestRoute other = (TestRoute) o;
            return id.equals(other.id) && type.equals(other.type);
        }

        @Override public int hashCode() {return id.hashCode() + type.hashCode();}
    }

    static class TestStop extends Stop {
        private final String id;

        TestStop(String id) { 
        	super("caso", "caso", new GeoPosition(23.11321f, 11.2321f));
        	this.id = id; 
        	}

        @Override public String getStopId() { return id; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestStop)) return false;
            TestStop other = (TestStop) o;
            return id.equals(other.id);
        }

        @Override public int hashCode() {
            return id.hashCode();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        account = new Account("myPassword123", "testUser");
    }

    @Test
    void testPasswordIsHashed() {
        String password = account.getPassword();
        assertNotNull(password);
        assertNotEquals("myPassword123", password);
        assertTrue(password.length() > 0);
    }

    @Test
    void testSaveFavoriteRouteAndPreventDuplicates() throws FavoriteAlreadyPresent {
        Route r1 = new TestRoute("R1", "1"); // Metro route type

        account.saveFavoriteRoute(r1);
        assertTrue(account.getFavorites().getFavoriteMetroRoutes().contains(r1));

        // Trying to add again throws FavoriteAlreadyPresent
        assertThrows(FavoriteAlreadyPresent.class, () -> account.saveFavoriteRoute(r1));
    }

    @Test
    void testSaveFavoriteStopAndPreventDuplicates() throws FavoriteAlreadyPresent {
        Stop s1 = new TestStop("S1");

        account.saveFavoriteStop(s1);
        assertTrue(account.getFavorites().getFavoriteStops().contains(s1));

        // Duplicate stop addition throws exception
        assertThrows(FavoriteAlreadyPresent.class, () -> account.saveFavoriteStop(s1));
    }

    @Test
    void testRemoveFavoriteRoute() throws FavoriteAlreadyPresent {
        Route r1 = new TestRoute("R2", "3"); // Bus route type
        account.saveFavoriteRoute(r1);
        assertTrue(account.getFavorites().getFavoriteBusRoutes().contains(r1));

        account.reomvefavoriteRoute(r1);
        assertFalse(account.getFavorites().getFavoriteBusRoutes().contains(r1));
    }

    @Test
    void testRemoveFavoriteStop() throws FavoriteAlreadyPresent {
        Stop s1 = new TestStop("S2");
        account.saveFavoriteStop(s1);
        assertTrue(account.getFavorites().getFavoriteStops().contains(s1));

        account.reomvefavoriteStop(s1);
        assertFalse(account.getFavorites().getFavoriteStops().contains(s1));
    }

    @Test
    void testSerializationAndDeserialization() throws Exception, FavoriteAlreadyPresent {
        Route r1 = new TestRoute("R3", "0");
        Stop s1 = new TestStop("S3");
        account.saveFavoriteRoute(r1);
        account.saveFavoriteStop(s1);

        String filename = "test_account.ser";
        account.serializeToFile(filename);

        File file = new File(filename);
        assertTrue(file.exists());

        Account loaded = Account.deserializeFromFile(filename);
        assertEquals(account.getUsername(), loaded.getUsername());
        assertEquals(account.getPassword(), loaded.getPassword());

        // Favorites must contain the saved route and stop
        assertTrue(loaded.getFavorites().getFavoriteTramRoutes().contains(r1));
        assertTrue(loaded.getFavorites().getFavoriteStops().contains(s1));

        // Clean up
        Files.deleteIfExists(file.toPath());
    }
}
