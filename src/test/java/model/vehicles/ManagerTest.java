package model.vehicles;


import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import utility.TemporaryDataHolder;
import utility.Triple;
import utility.exceptionUtils.DuplicationNotAccepted;
public class ManagerTest {

    Manager.CityManager cm;

    @BeforeEach
    void setup() throws DuplicationNotAccepted {
        cm = new Manager().new CityManager("TestCity" + System.nanoTime());
        TemporaryDataHolder tdh = new TemporaryDataHolder();
        
        var stop1 = new Stop("S1", "Main St", new GeoPosition(40.0, -75.0));
        var route1 = new Route("R1", "Blue Line", "1", "#0000FF");
        var trip1 = new Trip(route1.getRouteId(), "service_1", "T1", null, "1");
        var stopTime1 = new StopTime(trip1.getTrip_id(), LocalTime.of(8,0), LocalTime.of(8,5), stop1.getStopId(), 1);
        var calDate = new CalendarDates("service_1", LocalDate.now(), "1");


        // Minimal GTFS test data
        tdh.registerStop(stop1);
        tdh.registerCalendarDate(calDate);
        tdh.registerRoute(route1);
        tdh.registerTrip(trip1);
        tdh.registerStopTime(stopTime1);

        cm.setGTFSData(tdh);
    }

    @Test
    void testDuplicateCityManagerThrows() {
        assertThrows(DuplicationNotAccepted.class, () -> {
            new Manager().new CityManager(cm.getCity());
        });
    }

    @Test
    void testRegisterNullVehicleDoesNotThrow() {
        assertDoesNotThrow(() -> cm.registerVehicle(null));
        assertEquals(0, cm.getAllVehiclesRT().size());
    }

    @Test
    void testServiceIdAndTripDateRetrieval() {
        Trip trip = cm.getTripsPerRoute("R1").get(0);
        assertEquals(LocalDate.now(), cm.getTripDate(trip));
        assertEquals(LocalDate.now(), cm.getServiceIdDate("service_1"));
    }

    @Test
    void testStopsPerRouteWithSingleTripAndNoDirection() {
        List<Stop> stops = cm.getStopsPerRoute("R1");
        assertEquals(1, stops.size());
        assertEquals("Main St", stops.get(0).getStopId());
    }

    @Test
    void testStopsPerRouteAndDirectionWithMissingDirection() {
        List<Stop> dirStops = cm.getStopsPerRouteAndDirection("R1", "1");
        assertEquals(1, dirStops.size());
    }

    @Test
    void testGetRoutesPerStopAndTripsPerStop() {
        Set<Route> routes = cm.getRoutesPerStop("Main St");
        assertEquals(1, routes.size());
        assertEquals("R1", routes.iterator().next().getRouteId());

        Set<Triple<Trip, LocalTime, LocalTime>> trips = cm.getTripsPerStop("Main St");
        assertEquals(1, trips.size());
        Triple<Trip, LocalTime, LocalTime> entry = trips.iterator().next();
        assertEquals("T1", entry.getX().getTrip_id());
    }

    @Test
    void testUpdateFlagsCorrectly() {
        assertFalse(cm.isInUpdate());
        cm.isBeingUpdated();
        assertTrue(cm.isInUpdate());
        cm.terminateUpdate();
        assertFalse(cm.isInUpdate());
    }

    @Test
    void testSearchRouteAndStop() {
        Route route = cm.searchRoute("R1");
        assertNotNull(route);
        assertEquals("R1", route.getRouteId());

        Stop stop = cm.getStopByStopId("Main St");
        assertNotNull(stop);
        assertEquals("Main St", stop.getStopId());
    }

    @Test
    void testSearchCityManagerFromGlobalList() throws DuplicationNotAccepted {
        Manager.CityManager found = Manager.searchCityManager(cm.getCity());
        assertNotNull(found);
        assertEquals(cm.getCity(), found.getCity());

        assertNull(Manager.searchCityManager("NonExistingCity"));
    }

    @Test
    void testRouteHasDirectionReturnsFalseForSingleDirectionTrip() {
        assertFalse(cm.routeHasDirection("R1"));
    }

    @Test
    void testCityManagerGTFSNotInitializedEdgeCase() throws DuplicationNotAccepted {
        Manager.CityManager temp = new Manager().new CityManager("EmptyCity" + System.nanoTime());

        assertThrows(NullPointerException.class, () -> temp.searchRoute("R1"));
        assertThrows(NullPointerException.class, () -> temp.getAllStops());
        assertThrows(NullPointerException.class, () -> temp.getStopsPerRoute("R1"));
    }
}
