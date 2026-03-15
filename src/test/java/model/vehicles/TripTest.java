package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TripTest {

    private static final String VALID_ROUTE_ID = "route_1";
    private static final String VALID_SERVICE_ID = "weekday_service";
    private static final String VALID_TRIP_ID = "trip_101";
    private static final String VALID_HEADSIGN = "Downtown";
    private static final String VALID_DIRECTION = "0";

    @Test
    void testConstructorStoresValuesCorrectly() {
        Trip trip = new Trip(VALID_ROUTE_ID, VALID_SERVICE_ID, VALID_TRIP_ID, VALID_HEADSIGN, VALID_DIRECTION);

        assertEquals(VALID_ROUTE_ID, trip.getRoute_id());
        assertEquals(VALID_SERVICE_ID, trip.getService_id());
        assertEquals(VALID_TRIP_ID, trip.getTrip_id());
        assertEquals(VALID_HEADSIGN, trip.getTrip_headsign());
        assertEquals(VALID_DIRECTION, trip.getDirection_id());
    }

    @Test
    void testFieldsAreFinalAndPrivate() throws NoSuchFieldException {
        Field routeIdField = Trip.class.getDeclaredField("route_id");
        assertTrue(Modifier.isPrivate(routeIdField.getModifiers()), "route_id should be private");
        assertTrue(Modifier.isFinal(routeIdField.getModifiers()), "route_id should be final");

        Field serviceIdField = Trip.class.getDeclaredField("service_id");
        assertTrue(Modifier.isPrivate(serviceIdField.getModifiers()), "service_id should be private");
        assertTrue(Modifier.isFinal(serviceIdField.getModifiers()), "service_id should be final");

        Field tripIdField = Trip.class.getDeclaredField("trip_id");
        assertTrue(Modifier.isPrivate(tripIdField.getModifiers()), "trip_id should be private");
        assertTrue(Modifier.isFinal(tripIdField.getModifiers()), "trip_id should be final");

        Field headsignField = Trip.class.getDeclaredField("trip_headsign");
        assertTrue(Modifier.isPrivate(headsignField.getModifiers()), "trip_headsign should be private");
        assertTrue(Modifier.isFinal(headsignField.getModifiers()), "trip_headsign should be final");

        Field directionIdField = Trip.class.getDeclaredField("direction_id");
        assertTrue(Modifier.isPrivate(directionIdField.getModifiers()), "direction_id should be private");
        assertTrue(Modifier.isFinal(directionIdField.getModifiers()), "direction_id should be final");
    }

    @Test
    void testImmutability_AttemptToModifyFieldsViaReflection() throws Exception {
        Trip trip = new Trip(VALID_ROUTE_ID, VALID_SERVICE_ID, VALID_TRIP_ID, VALID_HEADSIGN, VALID_DIRECTION);

        Field tripIdField = Trip.class.getDeclaredField("trip_id");
        tripIdField.setAccessible(true);
        // Reflection can overwrite final fields - demonstrating why immutability relies on encapsulation and discipline
        tripIdField.set(trip, "changed_trip_id");

        assertEquals("changed_trip_id", trip.getTrip_id());
    }

    @Test
    void testConstructorThrowsOnNullRouteId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Trip(null, VALID_SERVICE_ID, VALID_TRIP_ID, VALID_HEADSIGN, VALID_DIRECTION));
    }

    @Test
    void testConstructorThrowsOnNullServiceId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Trip(VALID_ROUTE_ID, null, VALID_TRIP_ID, VALID_HEADSIGN, VALID_DIRECTION));
    }

    @Test
    void testConstructorThrowsOnNullTripId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Trip(VALID_ROUTE_ID, VALID_SERVICE_ID, null, VALID_HEADSIGN, VALID_DIRECTION));
    }



    @Test
    void testEmptyStringFieldsAllowed() {
        Trip trip = new Trip("", "", "", "", "");

        assertEquals("", trip.getRoute_id());
        assertEquals("", trip.getService_id());
        assertEquals("", trip.getTrip_id());
        assertEquals("", trip.getTrip_headsign());
        assertEquals("", trip.getDirection_id());
    }

    @Test
    void testUsageScenario_UniqueTripsInSet() {
        Trip trip1 = new Trip("routeA", "weekday", "trip1", "Downtown", "0");
        Trip trip2 = new Trip("routeA", "weekday", "trip1", "Downtown", "0");
        Trip trip3 = new Trip("routeB", "weekend", "trip2", "Uptown", "1");

        assertNotEquals(trip1, trip2);
        assertNotEquals(trip1.hashCode(), trip2.hashCode());

        Set<Trip> trips = new HashSet<>();
        trips.add(trip1);
        trips.add(trip2);
        trips.add(trip3);

        assertEquals(3, trips.size());
    }

    @Test
    void testToString_NotImplemented() {
        Trip trip = new Trip("r", "s", "t", "h", "d");
        String toStr = trip.toString();
        assertTrue(toStr.contains("Trip"));
    }
}

