package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RouteTest {

    @Test
    public void constructor_AllFieldsValid_ShouldCreateObject() {
        Route route = new Route("route_1", "Blue Line", "1", "#0000FF");
        assertEquals("route_1", route.getRouteId());
        assertEquals("Blue Line", route.getRouteName());
        assertEquals("1", route.getType());
        assertEquals("#0000FF", route.getMapVisualiser());
    }

    @Test
    public void constructor_NullRouteId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Route(null, "Blue Line", "1", "#0000FF");
        });
    }

    @Test
    public void constructor_NullRouteShortName_ShouldAllowNull() {
        Route route = new Route("route_1", null, "1", "#0000FF");
        assertNull(route.getRouteName());
    }

    @Test
    public void constructor_NullType_ShouldAllowNull() {
        Route route = new Route("route_1", "Blue Line", null, "#0000FF");
        assertNull(route.getType());
    }

    @Test
    public void constructor_NullMapVisualiser_ShouldAllowNull() {
        Route route = new Route("route_1", "Blue Line", "1", null);
        assertNull(route.getMapVisualiser());
    }
}
