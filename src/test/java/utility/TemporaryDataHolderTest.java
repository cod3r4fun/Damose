package utility;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.CalendarDates;
import model.vehicles.Route;
import model.vehicles.Stop;
import model.vehicles.StopTime;
import model.vehicles.Trip;

public class TemporaryDataHolderTest {

    private TemporaryDataHolder dataHolder;
    private Stop constStop;
	private Trip constTrip;

    @BeforeEach
    void setup() {
        dataHolder = new TemporaryDataHolder();
        constStop = new Stop("stop1", "Main St", new GeoPosition(45.0, 90.0));
        constTrip = new Trip("route_1", "service_1", "trip1", null, null);
    }

    @Test
    void registerStop_addsAndPreventsDuplicates() {
        Stop stop = constStop;
        dataHolder.registerStop(stop);
        dataHolder.registerStop(stop); 

        List<Stop> stops = dataHolder.getAllStops();
        assertEquals(1, stops.size());
        assertTrue(stops.contains(stop));
    }

    @Test
    void registerStop_nullIgnored() {
        dataHolder.registerStop(null);
        assertTrue(dataHolder.getAllStops().isEmpty());
    }

    @Test
    void registerRoute_addsAndPreventsDuplicates() {
        Route route = new Route("route_1", "Blue", "1", "#0000FF");
        dataHolder.registerRoute(route);
        dataHolder.registerRoute(route);

        List<Route> routes = dataHolder.getAllRoutes();
        assertEquals(1, routes.size());
        assertTrue(routes.contains(route));
    }

    @Test
    void registerRoute_nullIgnored() {
        dataHolder.registerRoute(null);
        assertTrue(dataHolder.getAllRoutes().isEmpty());
    }

    @Test
    void registerTrip_addsAndPreventsDuplicates() {
        Route route = new Route("route_1", "Blue", "1", "#0000FF");
        Trip trip = constTrip;
        dataHolder.registerTrip(trip);
        dataHolder.registerTrip(trip);

        List<Trip> trips = dataHolder.getAllTrips();
        assertEquals(1, trips.size());
        assertTrue(trips.contains(trip));
    }

    @Test
    void registerTrip_nullIgnored() {
        dataHolder.registerTrip(null);
        assertTrue(dataHolder.getAllTrips().isEmpty());
    }

    @Test
    void registerCalendarDate_addsAndPreventsDuplicates() {
        CalendarDates cd = new CalendarDates("service_1", LocalDate.of(2024, 12, 25), "2");
        dataHolder.registerCalendarDate(cd);
        dataHolder.registerCalendarDate(cd);

        List<CalendarDates> dates = dataHolder.getAllDates();
        assertEquals(1, dates.size());
        assertTrue(dates.contains(cd));
    }

    @Test
    void registerCalendarDate_nullIgnored() {
        dataHolder.registerCalendarDate(null);
        assertTrue(dataHolder.getAllDates().isEmpty());
    }



    @Test
    void registerStopTime_nullIgnored() {
        dataHolder.registerStopTime(null);
        assertTrue(dataHolder.getAllStopTimes().isEmpty());
    }

    @Test
    void reset_clearsAllData() {
        dataHolder.registerStop(constStop);
        dataHolder.registerRoute(new Route("route_1", "Blue", "1", "#0000FF"));
        dataHolder.registerTrip(constTrip);
        dataHolder.registerCalendarDate(new CalendarDates("service_1", LocalDate.now(), "1"));


        dataHolder.reset();

        assertTrue(dataHolder.getAllStops().isEmpty());
        assertTrue(dataHolder.getAllRoutes().isEmpty());
        assertTrue(dataHolder.getAllTrips().isEmpty());
        assertTrue(dataHolder.getAllDates().isEmpty());
        assertTrue(dataHolder.getAllStopTimes().isEmpty());
    }

    @Test
    void getAllLists_areImmutable() {
        Stop stop = constStop;
        dataHolder.registerStop(stop);
        List<Stop> stops = dataHolder.getAllStops();

        assertThrows(UnsupportedOperationException.class, () -> stops.add(new Stop("stop2", "Second St", new GeoPosition(46.0, 91.0))));
    }

    @Test
    void concurrentRegisterStop_threadSafetyCheck() throws InterruptedException {
        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Stop stop = new Stop("stop" + idx, "Stop " + idx, new GeoPosition(idx * 1.0, idx * 1.0));
                    dataHolder.registerStop(stop);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                doneLatch.countDown();
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertEquals(threads, dataHolder.getAllStops().size());
    }
}

