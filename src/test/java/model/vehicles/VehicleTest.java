package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utility.TemporaryDataHolder;
import utility.exceptionUtils.DuplicationNotAccepted;

public class VehicleTest {

    private final String city = "TestCity" + System.nanoTime();

    @BeforeEach
    void setUp() throws DuplicationNotAccepted {
        TemporaryDataHolder tdh = new TemporaryDataHolder();

        var stop1 = new Stop("S1", "Main St", new GeoPosition(40.0, -75.0));
        var route1 = new Route("R1", "Blue Line", "1", "#0000FF");
        var trip1 = new Trip(route1.getRouteId(), "service_1", "T1", null, "1");
        var stopTime1 = new StopTime(trip1.getTrip_id(), LocalTime.of(8,0), LocalTime.of(8,5), stop1.getStopId(), 1);
        var calDate = new CalendarDates("service_1", LocalDate.now(), "1");


        tdh.registerStop(stop1);
        tdh.registerCalendarDate(calDate);
        tdh.registerRoute(route1);
        tdh.registerTrip(trip1);
        tdh.registerStopTime(stopTime1);

        Manager.CityManager cm = new Manager().new CityManager(city);
        cm.setGTFSData(tdh);
    }

    @Test
    void testNullVehicleIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle(null, "bus-123", "STOP1", System.currentTimeMillis(), 1,
                    40.7128f, -74.0060f, city, 180f, 5000, 40,
                    "T1", LocalTime.of(8, 0), LocalDate.now(), "R1", "0");
        });
    }

    @Test
    void testNullNextStopIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("V1", "bus-123", null, System.currentTimeMillis(), 1,
                    40.7128f, -74.0060f, city, 180f, 5000, 40,
                    "T1", LocalTime.of(8, 0), LocalDate.now(), "R1", "0");
        });
    }

    @Test
    void testNullRouteThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("V1", "bus-123", "STOP1", System.currentTimeMillis(), 1,
                    40.7128f, -74.0060f, city, 180f, 5000, 40,
                    "T1", LocalTime.of(8, 0), LocalDate.now(), null, "0");
        });
    }

    @Test
    void testNullDirectionThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("V1", "bus-123", "STOP1", System.currentTimeMillis(), 1,
                    40.7128f, -74.0060f, city, 180f, 5000, 40,
                    "T1", LocalTime.of(8, 0), LocalDate.now(), "R1", null);
        });
    }

    @Test
    void testInvalidRouteThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("V1", "bus-123", "STOP1", System.currentTimeMillis(), 1,
                    40.7128f, -74.0060f, city, 180f, 5000, 40,
                    "T1", LocalTime.of(8, 0), LocalDate.now(), "InvalidRoute", "0");
        });
    }

    @Test
    void testValidVehicleConstruction() {
        Vehicle vehicle = new Vehicle("V1", "bus-123", "STOP1", System.currentTimeMillis(), 1,
                40.7128f, -74.0060f, city, 180f, 5000, 40,
                "T1", LocalTime.of(8, 0), LocalDate.now(), "R1", "1");

        assertEquals("V1", vehicle.getVehicleId());
        assertEquals("bus-123", vehicle.getLabel());
        assertEquals("STOP1", vehicle.getNextStopId());
        assertEquals(1, vehicle.getCurrentStopSequence());
        assertEquals(vehicle.getPosition().getLatitude(), 40.7128f, 0.0001);
        assertEquals(vehicle.getPosition().getLongitude(), -74.0060f, 0.0001);
        assertEquals(city, vehicle.getCity());
        assertEquals(180f, vehicle.getBearing());
        assertEquals(5000, vehicle.getOdometer());
        assertEquals(40, vehicle.getSpeed());
        assertEquals("T1", vehicle.getTripId());
        assertEquals("R1", vehicle.getRouteId());
        assertEquals("1", vehicle.getDirection());
    }

    @Test
    void testMissingTripForDirectionThrowsIndexOutOfBounds() throws DuplicationNotAccepted {
        String city2 = "CityNoDirection" + System.nanoTime();
        TemporaryDataHolder tdh = new TemporaryDataHolder();


        Manager.CityManager cm = new Manager().new CityManager(city2);
        cm.setGTFSData(tdh);

        
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("Vfail", "fail-veh", "STOPX", System.currentTimeMillis(), 2,
                    45.0f, 9.0f, city2, 90f, 100, 30,
                    "TX", LocalTime.of(9, 0), LocalDate.now(), "RX", "0"); // requesting "0"
        });
    }
}
