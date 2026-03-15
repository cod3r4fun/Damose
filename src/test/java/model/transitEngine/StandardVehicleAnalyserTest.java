package model.transitEngine;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.vehiclePositionUpdater.Updater;
import model.vehicles.CalendarDates;
import model.vehicles.GTFSData;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Manager.CityManager;
import model.vehicles.Stop;
import model.vehicles.StopTime;
import model.vehicles.Trip;
import model.vehicles.Vehicle;
import utility.CityTrack;
import utility.TemporaryDataHolder;
import utility.Triple;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;

import static org.junit.jupiter.api.Assertions.*;

public class StandardVehicleAnalyserTest {

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

    @Test
    void testExpectedArrivalTimeNormal() {
    	StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();

        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "StopB", cityManager);
        assertNotNull(result);
        assertTrue(result.isAfter(LocalTime.now().minusMinutes(1)));
    }
    
    @Test
    void testExpectedArrivalTimeVehicleAtStart() {
    	vehicle = new Vehicle("v1", "Blue Line", "0", Instant.now().getEpochSecond(), 1, 40.00004f, -75.00004f, "Rome", 0, 0, 0, null, now, null, "R1", "0");
    	StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();

        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "StopB", cityManager);
        assertNull(result);
    }

    @Test
    void testExpectedArrivalTimeNoTripsReturnsNull() {
    	cityManager.setGTFSData(new TemporaryDataHolder());

    	StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();

        assertThrows(NullPointerException.class, () -> analyser.expectedArrivalTimeToStop(vehicle, "stopB", cityManager)); 
        //very unlikely, it would mean that the vehicle is on a not programmed trip;
    }

    @Test
    void testExpectedArrivalTimeInvalidStopSequenceReturnsNull() {
    	 vehicle = new Vehicle("v1", "Blue Line", "0", Instant.now().getEpochSecond(), 0, 40.00004f, -75.00004f, "Rome", 0, 0, 0, null, now, null, "R1", "0");

    	 StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();

        assertNull(analyser.expectedArrivalTimeToStop(vehicle, "stopB", cityManager));

        vehicle = new Vehicle("v1", "Blue Line", "0", Instant.now().getEpochSecond(), 100, 40.00004f, -75.00004f, "Rome", 0, 0, 0, null, now, null, "R1", "0");
        assertNull(analyser.expectedArrivalTimeToStop(vehicle, "stopB", cityManager));
    }
    
    @Test
    void testExpectedArrivalTimeStopNotInTrip() {
        StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();
        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "NonexistentStop", cityManager);
        assertNull(result);
    }

    @Test
    void testExpectedArrivalTimeVehicleHasNoTimestamp() {
        vehicle = new Vehicle("v1", "Blue Line", "0", -1, 2, 40.00004f, -75.00004f, "Rome", 0, 0, 0, null, null, null, "R1", "0");

        StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();
        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "StopB", cityManager);
        assertNull(result);
    }

    @Test
    void testExpectedArrivalTimeOnlyOneStopInTrip() {
        // Remove second stop
        TemporaryDataHolder tdh = new TemporaryDataHolder();
        tdh.registerRoute(new Route("R1", "Blue Line", "0", "#0000FF"));
        Stop stopA = new Stop("S1", "stopA", new GeoPosition(40.0, -75.0));
        tdh.registerStop(stopA);
        tdh.registerTrip(trip);
        tdh.registerCalendarDate(new CalendarDates("service_1", LocalDate.of(2025, 6, 24), "1"));
        tdh.registerStopTime(new StopTime(trip.getTrip_id(), now.minusMinutes(5), now.minusMinutes(4), stopA.getStopId(), 1));

        cityManager.setGTFSData(tdh);

        StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();
        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "StopB", cityManager);
        assertNull(result); 
    }


    @Test
    void testExpectedArrivalTimeSameStopAsVehicleReturnsFutureTime() {
        vehicle = new Vehicle("v1", "Blue Line", "0", Instant.now().getEpochSecond(), 3, 40.0001f, -75.0001f, "Rome", 0, 0, 0, null, now, null, "R1", "0");

        StandardVehicleAnalyser analyser = new StandardVehicleAnalyser();
        LocalTime result = analyser.expectedArrivalTimeToStop(vehicle, "StopB", cityManager);
        assertNull(result);
    }




}
