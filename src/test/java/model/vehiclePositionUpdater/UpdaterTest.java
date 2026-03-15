package model.vehiclePositionUpdater;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.Instant;
import java.time.LocalDate;

import model.vehiclePositionUpdater.Updater;
import model.vehicles.*;
import model.vehicles.Manager.CityManager;
import org.jxmapviewer.viewer.GeoPosition;

import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utility.CityTrack;
import utility.TemporaryDataHolder;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UpdaterExParameterResolver.class)
public class UpdaterTest {

	Vehicle vehicle;
    CityManager cityManager;
    Trip trip;
    Stop stopA;
    Stop stopB;
    LocalTime now = LocalTime.now();
    final UpdaterEx updater;
    
    UpdaterTest(UpdaterEx updater){
    	this.updater = updater;

    }
    
    
	
    static class UpdaterEx extends Updater {
    	
    	Manager.CityManager stubCityManager;

		public UpdaterEx(String city) throws DuplicationNotAccepted, FetcherGTFS_RTNonExistent, MalformedURLException, URISyntaxException{
			super("Rome");
			
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
    void testUpdaterDoubleInitialization() throws Exception, DuplicationNotAccepted, FetcherGTFS_RTNonExistent {
		assertThrows(DuplicationNotAccepted.class, () -> new UpdaterEx("Rome"));
    }

    @Test
    void testSetAndCheckGTFSParsedFlag() throws Exception, DuplicationNotAccepted, FetcherGTFS_RTNonExistent {
        updater.setGTFSparsed(true);
        assertTrue(updater.isGTFSParsed(), "GTFSParsed flag should reflect true.");
    }



    @Test
    void testVehicleInCityManagerIsAccessible() {
        var vehicles = cityManager.getAllVehiclesRT();
        assertEquals(1, vehicles.size(), "One vehicle should be registered.");
        assertEquals("v1", vehicles.get(0).getVehicleId(), "Vehicle ID should match the one registered.");
    }

    @Test
    void testFastParseTimeHandlesValidTime() {
        var parsed = invokeFastParseTime("25:30:00");
        assertEquals(LocalTime.of(1, 30), parsed, "Should wrap hour to 1 (25 % 24)");
    }

    @Test
    void testFastParseTimeWithShortStringReturnsNull() {
        var parsed = invokeFastParseTime("12:3");
        assertNull(parsed, "Should return null for malformed time strings.");
    }

    private LocalTime invokeFastParseTime(String timeStr) {
        try {
            var method = Updater.class.getDeclaredMethod("fastParseTime", String.class);
            method.setAccessible(true);
            return (LocalTime) method.invoke(null, timeStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke fastParseTime", e);
        }
    }
}


