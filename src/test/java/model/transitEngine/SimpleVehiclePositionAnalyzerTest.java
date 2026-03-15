package model.transitEngine;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;
import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import controller.connectionMakerAndControlUnit.OfflineConnectionChecker;
import model.transitEngine.StandardVehicleAnalyserTest.UpdaterEx;
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

class SimpleVehiclePositionAnalyzerTest {

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
        
        
    }
    
    @AfterEach
    void tearDown() {
        MasterConnectionStatusChecker.getSingleton().update();
    }
    
    
    
    
    @Test
    void testFindVehiclePositionsForRoute_StaticData_ReturnsOnePosition() {

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRoute("R1", cityManager);

        assertEquals(1, positions.size(), "Should return one estimated position from static trip data");
        GeoPosition pos = positions.get(0);
        assertEquals(40.0, pos.getLatitude(), 0.0001);
        assertEquals(-75.0, pos.getLongitude(), 0.0001);
    }

    @Test
    void testFindVehiclePositionsForRouteAndDirection_StaticData_ReturnsOnePosition() {

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRouteAndDirection("R1", "0", cityManager);

        assertEquals(1, positions.size(), "Should return one estimated position from static trip data");
        GeoPosition pos = positions.get(0);
        assertEquals(40.0, pos.getLatitude(), 0.0001);
        assertEquals(-75.0, pos.getLongitude(), 0.0001);
    }

    @Test
    void testFindVehiclePositionsForRouteAndDirection_StaticData_NoMatchOnDirection() {

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRouteAndDirection("R1", "1", cityManager); // Wrong direction

        assertEquals(0, positions.size(), "Should return no positions if direction doesn't match");
    }

    // to continue the next tests being online is required
    
    @Test
    void testFindVehiclePositionsForRoute_RTConnection_ReturnsRealTimeVehicle() {
    	
    	OfflineConnectionChecker.getSingleton().update();;
        MasterConnectionStatusChecker.getSingleton().update();

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRoute("R1", cityManager);

        assertEquals(1, positions.size(), "Should return one real-time vehicle position");
        GeoPosition pos = positions.get(0);
        assertEquals(40.00004f, pos.getLatitude(), 0.0001);
        assertEquals(-75.00004f, pos.getLongitude(), 0.0001);
    }

    @Test
    void testFindVehiclePositionsForRouteAndDirection_RTConnection_FilteredCorrectly() {

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRouteAndDirection("R1", "0", cityManager);

        assertEquals(1, positions.size(), "Should return one real-time vehicle with correct direction");
    }

    @Test
    void testFindVehiclePositionsForRouteAndDirection_RTConnection_NoMatchOnDirection() {

        var analyzer = new SimpleVehiclePositionAnalyzer();
        var positions = analyzer.findVehiclePositionsForRouteAndDirection("R1", "1", cityManager);

        assertEquals(0, positions.size(), "Should return zero vehicles for unmatched direction in RT");
    }


    
    
    
}
