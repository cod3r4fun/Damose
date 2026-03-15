package model.vehicles;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import utility.TemporaryDataHolder;
import utility.Triple;
import model.vehicles.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

class GTFSDataTest {

    private TemporaryDataHolder tdh;

    private Route route1;
    private Route route2;
    private Stop stop1;
    private Stop stop2;
    private Trip trip1;
    private Trip trip2;
    private CalendarDates calDate1;
    private CalendarDates calDate2;
    private StopTime stopTime1;
    private StopTime stopTime2;
    private StopTime stopTime3;

    @BeforeEach
    void setup() {
        tdh = new TemporaryDataHolder();

        route1 = new Route("R1", "Blue Line", "1", "#0000FF");
        route2 = new Route("R2", "Red Line", "2", "#FF0000");
        tdh.registerRoute(route1);
        tdh.registerRoute(route2);

        stop1 = new Stop("S1", "Main St", new GeoPosition(40.0, -75.0));
        stop2 = new Stop("S2", "Broadway", new GeoPosition(41.0, -74.0));
        tdh.registerStop(stop1);
        tdh.registerStop(stop2);

        trip1 = new Trip(route1.getRouteId(), "service_1", "T1", null, null);
        trip2 = new Trip(route2.getRouteId(), "service_2", "T2", null, null);
        tdh.registerTrip(trip1);
        tdh.registerTrip(trip2);

        calDate1 = new CalendarDates("service_1", LocalDate.of(2025, 6, 24), "1");
        calDate2 = new CalendarDates("service_2", LocalDate.of(2025, 6, 25), "2"); // Exception type = 2 should be ignored as of now
        tdh.registerCalendarDate(calDate1);
        tdh.registerCalendarDate(calDate2);

        stopTime1 = new StopTime(trip1.getTrip_id(), LocalTime.of(8,0), LocalTime.of(8,5), stop1.getStopId(), 1);
        stopTime2 = new StopTime(trip1.getTrip_id(), LocalTime.of(8,15), LocalTime.of(8,20), stop2.getStopId(), 2);
        stopTime3 = new StopTime(trip2.getTrip_id(), LocalTime.of(9,0), LocalTime.of(9,5), stop2.getStopId(), 1);
        tdh.registerStopTime(stopTime1);
        tdh.registerStopTime(stopTime2);
        tdh.registerStopTime(stopTime3);
    }

    @Test
    void testBasicConstructionAndGetters() {
        GTFSData gtfs = new GTFSData(tdh);

        List<Route> allRoutes = gtfs.getAllRoutes();
        assertEquals(2, allRoutes.size());
        assertTrue(allRoutes.contains(route1));
        assertTrue(allRoutes.contains(route2));

        List<Stop> allStops = gtfs.getAllStops();
        assertEquals(2, allStops.size());
        assertTrue(allStops.contains(stop1));
        assertTrue(allStops.contains(stop2));

        assertEquals(route1, gtfs.searchRoute("R1"));
        assertNull(gtfs.searchRoute("NON_EXISTENT"));

        assertEquals(1, gtfs.getRouteType("R1"));
        assertEquals(2, gtfs.getRouteType("R2"));
        assertEquals(-1, gtfs.getRouteType("UNKNOWN"));
    }

    @Test
    void testRoutesPerStopMapping() {
        GTFSData gtfs = new GTFSData(tdh);

        Set<Route> routesForStop1 = gtfs.getRoutesPerStop("Main St");
        assertNotNull(routesForStop1);
        assertEquals(1, routesForStop1.size());
        assertTrue(routesForStop1.contains(route1));

        Set<Route> routesForStop2 = gtfs.getRoutesPerStop("Broadway");
        assertNotNull(routesForStop2);
        assertEquals(2, routesForStop2.size());
        assertTrue(routesForStop2.contains(route1));
        assertTrue(routesForStop2.contains(route2));

        assertNull(gtfs.getRoutesPerStop("UNKNOWN_STOP"));
    }

    @Test
    void testTripsPerRouteMapping() {
        GTFSData gtfs = new GTFSData(tdh);

        List<Trip> tripsForR1 = gtfs.getTripsPerRoute("R1");
        assertNotNull(tripsForR1);
        assertEquals(1, tripsForR1.size());
        assertTrue(tripsForR1.contains(trip1));

        List<Trip> tripsForR2 = gtfs.getTripsPerRoute("R2");
        assertNotNull(tripsForR2);
        assertEquals(1, tripsForR2.size());
        assertTrue(tripsForR2.contains(trip2));

        assertNull(gtfs.getTripsPerRoute("UNKNOWN_ROUTE"));
    }

    @Test
    void testStopsPerTripMapping() {
        GTFSData gtfs = new GTFSData(tdh);

        List<Triple<Stop, LocalTime, LocalTime>> stopsForTrip1 = gtfs.getStopsPerTrip("T1");
        assertNotNull(stopsForTrip1);
        assertEquals(2, stopsForTrip1.size());

        assertEquals(stop1, stopsForTrip1.get(0).getX());
        assertEquals(LocalTime.of(8,0), stopsForTrip1.get(0).getY());
        assertEquals(LocalTime.of(8,5), stopsForTrip1.get(0).getZ());

        assertEquals(stop2, stopsForTrip1.get(1).getX());

        assertNull(gtfs.getStopsPerTrip("UNKNOWN_TRIP"));
    }

    @Test
    void testTripsPerStopMapping() {
        GTFSData gtfs = new GTFSData(tdh);

        Set<Triple<Trip, LocalTime, LocalTime>> tripsForStop1 = gtfs.getTripsPerStop("Main St");
        assertNotNull(tripsForStop1);
        assertEquals(1, tripsForStop1.size());
        assertTrue(tripsForStop1.stream().anyMatch(t -> t.getX().equals(trip1)));

        Set<Triple<Trip, LocalTime, LocalTime>> tripsForStop2 = gtfs.getTripsPerStop("Broadway");
        assertNotNull(tripsForStop2);
        assertEquals(2, tripsForStop2.size());
        assertTrue(tripsForStop2.stream().anyMatch(t -> t.getX().equals(trip1)));
        assertTrue(tripsForStop2.stream().anyMatch(t -> t.getX().equals(trip2)));

        assertNull(gtfs.getTripsPerStop("UNKNOWN_STOP"));
    }

    @Test
    void testServiceIdToDateMappingAndTripDate() {
        GTFSData gtfs = new GTFSData(tdh);

        assertEquals(LocalDate.of(2025, 6, 24), gtfs.getServiceIdDate("service_1"));
        assertNull(gtfs.getServiceIdDate("service_2"));

        assertEquals(LocalDate.of(2025, 6, 24), gtfs.getTripDate(trip1));
        assertNull(gtfs.getTripDate(trip2));
    }

    @Test
    void testEmptyTemporaryDataHolder() {
        TemporaryDataHolder emptyTdh = new TemporaryDataHolder();
        GTFSData gtfs = new GTFSData(emptyTdh);

        assertTrue(gtfs.getAllStops().isEmpty());
        assertTrue(gtfs.getAllRoutes().isEmpty());
        assertNull(gtfs.getRoutesPerStop("ANY"));
        assertNull(gtfs.getTripsPerRoute("ANY"));
        assertNull(gtfs.getStopsPerTrip("ANY"));
        assertNull(gtfs.getTripsPerStop("ANY"));
        assertNull(gtfs.getServiceIdDate("ANY"));
    }

    @Test
    void testNullEntriesIgnoredInTemporaryDataHolder() {
        TemporaryDataHolder tdhWithNulls = new TemporaryDataHolder();
        tdhWithNulls.registerStop(null);
        tdhWithNulls.registerRoute(null);
        tdhWithNulls.registerTrip(null);
        tdhWithNulls.registerCalendarDate(null);
        tdhWithNulls.registerStopTime(null);

        GTFSData gtfs = new GTFSData(tdhWithNulls);

        assertTrue(gtfs.getAllStops().isEmpty());
        assertTrue(gtfs.getAllRoutes().isEmpty());
        assertTrue(gtfs.getTripsPerRoute("NON_EXISTENT") == null);
    }

    @Test
    void testDuplicateStopsRoutesTripsIgnored() {
        TemporaryDataHolder dupTdh = new TemporaryDataHolder();

        Stop s = stop1;
        dupTdh.registerStop(s);
        dupTdh.registerStop(s);

        Route r = new Route("R1", "Route1", "1", "#FFF");
        dupTdh.registerRoute(r);
        dupTdh.registerRoute(r);

        Trip tr = trip1;
        dupTdh.registerTrip(tr);
        dupTdh.registerTrip(tr);

        GTFSData gtfs = new GTFSData(dupTdh);

        assertEquals(1, gtfs.getAllStops().size());
        assertEquals(1, gtfs.getAllRoutes().size());
        assertEquals(1, gtfs.getTripsPerRoute("R1").size());
    }
}
