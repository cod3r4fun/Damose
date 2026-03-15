package model.searchEngine;

import controller.DateController;
import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;
import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import controller.connectionMakerAndControlUnit.OfflineConnectionChecker;
import model.vehiclePositionUpdater.Updater;
import model.vehicles.CalendarDates;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Stop;
import model.vehicles.StopTime;
import model.vehicles.Trip;
import model.vehicles.Vehicle;
import model.vehicles.Manager.CityManager;
import utility.CityTrack;
import utility.TemporaryDataHolder;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class simpleSearchTest {

    private simpleSearch search;

    
    
    Vehicle vehicle;
    CityManager cityManager;
    Trip trip;
    Stop stopA;
    Stop stopB;
    LocalTime now = LocalTime.now();
    
    static class UpdaterEx extends Updater {
    	
    	Manager.CityManager stubCityManager;

		public UpdaterEx(String city) throws DuplicationNotAccepted, FetcherGTFS_RTNonExistent, MalformedURLException, URISyntaxException{
			super("Rome");

			stubCityManager = new Manager().new CityManager("Rome");
			
		}
    	
    }

    static void initialSetup() throws MalformedURLException, URISyntaxException, DuplicationNotAccepted, FetcherGTFS_RTNonExistent {
    	var fetcherGTFS_RT = new FetcherGTFS_RT(CityTrack.TRACKEDCITIES.get("Rome").getY(), "Rome");
    	var updater = new UpdaterEx("Rome");
    }
    
    static {
    	try {
			initialSetup();
		} catch (MalformedURLException | URISyntaxException | DuplicationNotAccepted | FetcherGTFS_RTNonExistent e) {
			// TODO Auto-generated catch block
		}
    }
    
    @BeforeEach
    void setup() throws DuplicationNotAccepted, FetcherGTFS_RTNonExistent, MalformedURLException, URISyntaxException {
  
        MasterConnectionStatusChecker.getSingleton().update();
        DateController.reset();
    	
    	TemporaryDataHolder tdh = new TemporaryDataHolder();
        cityManager = Manager.searchCityManager("Rome");
        
        
        tdh = new TemporaryDataHolder();

        var route = new Route("R1", "Blue Line", "0", "#0000FF");
        tdh.registerRoute(route);

        var stopA = new Stop("S1", "stopA", new GeoPosition(40.0, -75.0));
        var stopB = new Stop("S2", "StopB", new GeoPosition(40.0001, -75.0001));
        tdh.registerStop(stopA);
        tdh.registerStop(stopB);

        trip = new Trip(route.getRouteId(), "service_1", "T1", null, "0");
        tdh.registerTrip(trip);

        var calDate1 = new CalendarDates("service_1", LocalDate.of(2025, 6, 24), "1");
        tdh.registerCalendarDate(calDate1);

        var stopTime1 = new StopTime(trip.getTrip_id(), now.minusMinutes(5),now.minusMinutes(4), stopA.getStopId(), 2);
        //var stopTime2 = new StopTime(trip.getTrip_id(), now.minusMinutes(4),now.minusMinutes(3), stopB.getStopId(), 3);
        var stopTime3 = new StopTime(trip.getTrip_id(), now.plusMinutes(10), now.plusMinutes(11), stopB.getStopId(), 3);
        tdh.registerStopTime(stopTime1);
        //tdh.registerStopTime(stopTime2);
        tdh.registerStopTime(stopTime3);


        cityManager.setGTFSData(tdh);
        vehicle = new Vehicle("v1", "Blue Line", "0", Instant.now().getEpochSecond(), 2, 40.00004f, -75.00004f, "Rome", 0, 0, 0, null, now, null, "R1", "0");
        cityManager.resetVehiclesRT();
        cityManager.registerVehicle(vehicle);
        
        search = new simpleSearch("Rome");
        
        
    }

    
    @Test
    void testSearchStopTimesPerRoute_StaticFallback() {
        Map<Stop, List<LocalTime>> result = search.searchStopTimesPerRoute("R1");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSearchStopTimesPerStop_StaticFallback() {
        Map<Route, List<LocalTime>> result = search.searchStopTimesPerStop("StopB");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSearchStopTimesPerRouteAndStop_StaticFallback() {
        List<LocalTime> times = search.searchStopTimesPerRouteAndStop("R1", "StopB");
        OfflineConnectionChecker.getSingleton().update();
        assertNotNull(times);
        assertFalse(times.isEmpty());
    }
    
    
    // to continue the next tests being online is required

    @Test
    void testNullInputs_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> search.searchStopTimesPerRoute(null));
        assertThrows(IllegalArgumentException.class, () -> search.searchStopTimesPerStop(null));
        assertThrows(IllegalArgumentException.class, () -> search.searchStopTimesPerRouteAndStop(null, "S1"));
        assertThrows(IllegalArgumentException.class, () -> search.searchStopTimesPerRouteAndStop("R1", null));
    }
    
    @Test
    void testConstructor_withNullCity_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new simpleSearch(null));
    }

    @Test
    void testGetId_incrementsCorrectly() {
        simpleSearch s1 = new simpleSearch("Rome");
        simpleSearch s2 = new simpleSearch("Rome");
        assertTrue(s2.getId() > s1.getId());
    }

    @Test
    void testSearchStopTimesPerRoute_RTData() {
        Map<Stop, List<LocalTime>> result = search.searchStopTimesPerRoute("R1");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.forEach((stop, times) -> {
            assertNotNull(stop);
            assertNotNull(times);
            assertFalse(times.isEmpty());
        });
    }

    @Test
    void testSearchStopTimesPerStop_RTData() {
        Map<Route, List<LocalTime>> result = search.searchStopTimesPerStop("StopB");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.forEach((route, times) -> {
            assertNotNull(route);
            assertNotNull(times);
            assertFalse(times.isEmpty());
        });
    }

    @Test
    void testSearchStopTimesPerRouteAndStop_RTData() {
        List<LocalTime> times = search.searchStopTimesPerRouteAndStop("R1", "StopB");
        assertNotNull(times);
        assertFalse(times.isEmpty());
    }


}
