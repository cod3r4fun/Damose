package model.user;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.Route;
import model.vehicles.Stop;
import utility.exceptionUtils.FavoriteAlreadyPresent;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

class FavoritesTest {

    private Favorites favorites;
    private static final String TEST_FILE = "test_favorites.ser";

    // Dummy Route and Stop for testing (replace with your real ones or create minimal stubs)
    private Route tramRoute = new Route("R1", "Blue Line", "0", "#0000FF");
    private Route metroRoute = new Route("R2", "Red Line", "1", "#FF0000");
    private Stop stop1 = new Stop("Main St", "stop-1", new GeoPosition(40.0, -75.0));

    @BeforeEach
    void setup() {
        favorites = new Favorites();
    }

    @AfterEach
    void cleanup() throws Exception {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAddAndGetFavoriteRoutes() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteRoute(tramRoute);
        favorites.saveFavoriteRoute(metroRoute);

        List<Route> tramFavorites = favorites.getFavoriteTramRoutes();
        List<Route> metroFavorites = favorites.getFavoriteMetroRoutes();

        assertEquals(1, tramFavorites.size());
        assertEquals(tramRoute, tramFavorites.get(0));

        assertEquals(1, metroFavorites.size());
        assertEquals(metroRoute, metroFavorites.get(0));
    }

    @Test
    void testAddDuplicateRouteThrows() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteRoute(tramRoute);
        assertThrows(FavoriteAlreadyPresent.class, () -> favorites.saveFavoriteRoute(tramRoute));
    }

    @Test
    void testAddAndGetFavoriteStops() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteStop(stop1);

        List<Stop> stops = favorites.getFavoriteStops();
        assertEquals(1, stops.size());
        assertEquals(stop1, stops.get(0));
    }

    @Test
    void testAddDuplicateStopThrows() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteStop(stop1);
        assertThrows(FavoriteAlreadyPresent.class, () -> favorites.saveFavoriteStop(stop1));
    }

    @Test
    void testRemoveFavoriteRoute() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteRoute(tramRoute);
        assertFalse(favorites.getFavoriteMetroRoutes().size() == 1);
        favorites.removeFavoriteRoute(tramRoute);
        assertTrue(favorites.getFavoriteTramRoutes().isEmpty());
    }

    @Test
    void testRemoveFavoriteStop() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteStop(stop1);
        favorites.removeFavoriteStop(stop1);
        assertTrue(favorites.getFavoriteStops().isEmpty());
    }

    @Test
    void testSerializationAndDeserialization() throws Exception, FavoriteAlreadyPresent {
        favorites.saveFavoriteRoute(tramRoute);
        favorites.saveFavoriteStop(stop1);

        favorites.serializeToFile(TEST_FILE);
        assertTrue(Files.exists(new File(TEST_FILE).toPath()));

        Favorites loaded = Favorites.deserializeFromFile(TEST_FILE);

        assertEquals(1, loaded.getFavoriteTramRoutes().size());
        assertEquals(tramRoute.getRouteId(), loaded.getFavoriteTramRoutes().get(0).getRouteId());

        assertEquals(1, loaded.getFavoriteStops().size());
        assertEquals(stop1.getStopId(), loaded.getFavoriteStops().get(0).getStopId());
    }
}
