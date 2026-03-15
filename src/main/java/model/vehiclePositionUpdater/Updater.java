package model.vehiclePositionUpdater;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import utility.GTFSHeaders;

import org.jxmapviewer.viewer.GeoPosition;

import com.google.transit.realtime.GtfsRealtime;

import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;
import model.vehicles.Stop;
import model.vehicles.StopTime;
import model.vehicles.Trip;
import model.vehicles.CalendarDates;
import model.vehicles.Manager;
import model.vehicles.Route;
import model.vehicles.Manager.CityManager;
import model.vehicles.Vehicle;
import utility.ReadFile;
import utility.SysConstants;
import utility.TemporaryDataHolder;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;

import java.time.format.DateTimeFormatter;

/**
 * Main class responsible for updating vehicle positions and parsing GTFS data (static and real-time).
 * <p>
 * Holds references to city-specific data managers and parsers.
 * Loads and parses GTFS static files (stops, routes, trips, calendar_dates, stop_times)
 * and GTFS real-time vehicle position feeds.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */


public class Updater{
	
	/**
	 * Manager for the city that stores stops, routes, trips, vehicles, etc.
	 */
	private final CityManager cityManager;
	
	/**
	 * The city name this updater instance works on.
	 */
	private final String city;
	
	/**
	 * Parser for static GTFS data files (stops, routes, trips, calendar_dates, stop_times).
	 */
	private final ParserGTFS parserGTFS;
	
	/**
	 * Flas that indicates whether the first parsing has been done;
	 */
	private boolean GTFSParsed;
	
	/**
	 * Parser for GTFS real-time vehicle position feed.
	 */
	private final ParserGTFS_RT parserGTFS_RT;
	
	/**
	 * Base directory path containing the GTFS static files for the city.
	 */
	private final String BaseDir; 
	
	
	/**
	 * Constructs an Updater for the specified city.
	 * <p>
	 * Initializes base directory path and parser instances.
	 * Also creates a CityManager instance for managing the city's data.
	 * </p>
	 * 
	 * @param city The city name (e.g., "Rome")
	 * @throws DuplicationNotAccepted if a CityManager for this city already exists (to avoid duplicates)
	 * @throws FetcherGTFS_RTNonExistent if no GTFS-RT fetcher exists for this city
	 */
	public Updater(String city) throws DuplicationNotAccepted, FetcherGTFS_RTNonExistent {
		this.city = city;
		BaseDir = SysConstants.CURRENTDIR + File.separator + SysConstants.FOLDERGTFS + File.separator +city+ "/" + "_";
		parserGTFS = this.new ParserGTFS();
		parserGTFS_RT = this.new ParserGTFS_RT();
		cityManager = new Manager().new CityManager(city);
		GTFSParsed = false;
		
		
		/* 
		 * solution to avoid throws, but is not thread-safe
		 * 
		 * CityManager existingManager = Manager.getAllCityManagers()
		 *      .stream()
		 *      .filter(scm -> scm.getCity().equals(city))
		 *      .findFirst()
		 *      .orElse(null);
		 *
		 *  if (existingManager != null) {
		 *      stopList = existingManager;
		 *  } else {
		 *      stopList = new Manager.CityManager(city);
		 *  }
		 * 
		 */

	}
	
	
	
	
	/**
	 * Parses all static GTFS files and registers their data in CityManager.
	 * 
	 * Calls the parsing methods in this order:
	 * stops, routes, trips, calendar_dates, stop_times.
	 * Notifies CityManager before and after the update to prevent concurrency issues.
	 * 
	 * @throws InterruptedException if stop_times parsing is interrupted
	 * @throws IOException          if file reading fails
	 */
	public void updateGTFS() throws InterruptedException, IOException {
		parserGTFS.parse();
		GTFSParsed = true;
	}
	
	
	/**
	 * Parses the GTFS real-time feed and updates the CityManager's vehicle data.
	 * Clears existing vehicles before adding new ones from the feed.
	 */
	public void updateGTFS_RT() {
		parserGTFS_RT.parse();
	}



	
	/**
	 * Returns the city manager holding all vehicle, route, trip, stop data for the city.
	 * 
	 * @return the CityManager instance
	 */
	public Manager.CityManager getCityManager() {
		return cityManager;
	}
	
	
	/**
	 * Returns the flag indicating whether the GTFS data has been parsed
	 * 
	 * @return GTFSParsed flag;
	 */
	public boolean isGTFSParsed() {
		return GTFSParsed;
	}
	
	public void setGTFSparsed(boolean actual) {
		GTFSParsed = actual;
	}
	

	
	/**
	 * Fast parser for time strings formatted as HH:mm:ss.
	 * <p>
	 * GTFS allows hours >= 24 to indicate times after midnight.
	 * This method parses and applies modulo 24 on hours.
	 * </p>
	 * 
	 * @param timeStr time string (e.g., "25:30:00")
	 * @return LocalTime object representing parsed time with hours modulo 24
	 */
	private static LocalTime fastParseTime(String timeStr) {
		if (timeStr.length() < 8) return null;
	    int h = Integer.parseInt(timeStr.substring(0, 2));
	    int m = Integer.parseInt(timeStr.substring(3, 5));
	    int s = Integer.parseInt(timeStr.substring(6, 8));

	    if (h >= 24) h %= 24;

	    return LocalTime.of(h, m, s);
	}
	
	
	

	
	
	
	
	
	
	/**
	 * Inner class responsible for parsing static GTFS data files:
	 * stops.txt, routes.txt, trips.txt, calendar_dates.txt, stop_times.txt.
	 * 
	 * Parses each file and registers corresponding objects into CityManager.
	 * 
	 * <p>Author: Franco Della Negra</p>
	 */
	
	private class ParserGTFS{
		private final TemporaryDataHolder tdh;
		
		/**
		 * Private constructor to restrict instantiation to the outer class only.
		 */
		private ParserGTFS() {
			tdh = new TemporaryDataHolder();
		}
		
		/**
		 * Parses stops.txt file, creates Stop objects, and registers them.
		 */
		private void parseStops(){
			
			String targetFile =  Updater.this.BaseDir + "stops.txt";
			
			ReadFile.readAndDoWithFirstLineMapper(
					targetFile, 
					(line, pos) -> {
				//line = line.replace("\"", "");		// to see for berlin implementation...
				var stopFieldsEx = line.split(","); 
				String[] stopFields = new String[pos.length];
				for (int i = 0 ; i<pos.length; i++) {
					stopFields[i] = stopFieldsEx[pos[i]];
				}
				var stop = new Stop(stopFields[0], stopFields[1], 
						new GeoPosition(Double.parseDouble(stopFields[2]), Double.parseDouble(stopFields[3])));
				
				tdh.registerStop(stop);
			}, ",", GTFSHeaders.STOPS);
			//System.out.println(1);
		}
	
		
		/**
		 * Parses routes.txt file, creates Route objects, and returns them.
		 */
		private void parseRoutes() {
			
			//System.out.println("parsing routes");		// debugging for Berlin
			
			String targetFile = Updater.this.BaseDir + "routes.txt";
			
			ReadFile.readAndDoWithFirstLineMapper(
					targetFile, 
					(line, pos) -> {
				var routeFieldsEx = line.split(","); 
				String[] routeFields = new String[pos.length];
				for (int i = 0 ; i<pos.length; i++) {
					routeFields[i] = routeFieldsEx[pos[i]];
				}
				var route = new Route(routeFields[0], routeFields[1], 
						routeFields[2], routeFields[3]);
				
				tdh.registerRoute(route);
			}, ",", GTFSHeaders.ROUTES);
		}
		
		
		/**
		 * Parses trips.txt file, creates Trip objects, and registers them. 
		 */
		private void parseTrips(){
			
			
			String targetFile = Updater.this.BaseDir + "trips.txt";
			
			ReadFile.readAndDoWithFirstLineMapper(
					targetFile, 
					(line, pos) -> {
							var tripFieldsEx = line.split(","); 
								String[] tripFields = new String[pos.length];
								for (int i = 0 ; i<pos.length; i++) {
									tripFields[i] = tripFieldsEx[pos[i]];
								}
								var trip = new Trip(tripFields[0], tripFields[1], 
									tripFields[2], tripFields[3], tripFields[4]);
				
								tdh.registerTrip(trip);
					}, 
					",", GTFSHeaders.TRIPS);
		}
		
		
		
		/**
		 * Parses calendar_dates.txt file, creates CalendarDates objects, and registers them.
		 */
		private void parseCalendarDates() {
			

			String targetFile = Updater.this.BaseDir+ "calendar_dates.txt";
			

			
			ReadFile.readAndDoWithFirstLineMapper(
					targetFile,
					(line, pos) ->{
						var calFieldsEx = line.split(",");
						String[] calFields = new String[pos.length];
						for (int i = 0; i<pos.length; i++) {
							calFields[i] = calFieldsEx[pos[i]];
						}
						var calendar = new CalendarDates(calFields[0], LocalDate.parse(calFields[1], DateTimeFormatter.ofPattern("yyyyMMdd")), calFields[2]);
												
						tdh.registerCalendarDate(calendar);
					}, 
					",", GTFSHeaders.CALENDARDATES);
						
		}
		
		/**
		 * Parses stop_times.txt file in parallel,
		 * creates StopTime objects, and registers them.
		 * @throws IOException 
		 * @throws InterruptedException 
		 */
		private void parseStopTimes() throws InterruptedException, IOException{

			String targetFile = Updater.this.BaseDir + "stop_times.txt";
			
			ReadFile.readAndDoWithFirstLineMapperParallel(
					targetFile,
					(line, pos) ->{
						var stopTFieldsEx = line.split(",");
						String[] stopTFields = new String[pos.length];
						for (int i = 0; i<pos.length; i++) {
							stopTFields[i] = stopTFieldsEx[pos[i]];
						}
						

						var stopTime = new StopTime(stopTFields[0], fastParseTime(stopTFields[1]), fastParseTime(stopTFields[2]), stopTFields[3], Integer.parseInt(stopTFields[4]));
						
						tdh.registerStopTime(stopTime);
					}, 
					",", GTFSHeaders.STOPTIMES);

			
		}
		


		/**
		 * Parses all static GTFS files and registers their data in CityManager.
		 * Private so it can only be accessed by the outer class
		 * 
		 * Calls the parsing methods in this order:
		 * stops, routes, trips, calendar_dates, stop_times.
		 * Notifies CityManager before and after the update to prevent concurrency issues.
		 * 
		 * @throws InterruptedException if stop_times parsing is interrupted
		 * @throws IOException          if file reading fails
		 */
		private void parse() throws InterruptedException, IOException{
			this.parseStops();
			this.parseRoutes();
			this.parseTrips();
			this.parseCalendarDates();
			this.parseStopTimes();
			Updater.this.cityManager.isBeingUpdated();
			Updater.this.cityManager.setGTFSData(tdh);
			Updater.this.cityManager.terminateUpdate();
			tdh.reset();
		}
		
		
		
	
	}
	
	
	
	
	/**
	 * Inner class responsible for parsing GTFS real-time vehicle position data.
	 * <p>
	 * Uses a FetcherGTFS_RT to obtain the GTFS-RT feed and updates vehicles data.
	 * Clears previously stored vehicles before updating.
	 * </p>
	 * 
	 * <p>Author: Franco Della Negra</p>
	 */
	private class ParserGTFS_RT{
		
		/**
		 * Fetcher object used to retrieve GTFS-RT feed for the city.
		 */
		private final FetcherGTFS_RT fetcherGTFS_RT;
		
		
		/**
		 * Constructs the ParserGTFS_RT and retrieves the GTFS-RT fetcher for the city.
		 * 
		 * @throws FetcherGTFS_RTNonExistent if no GTFS-RT fetcher exists for the city
		 */
		private ParserGTFS_RT() throws FetcherGTFS_RTNonExistent {
			fetcherGTFS_RT = FetcherGTFS_RT.getAllFetchers().stream(
					).filter((t) -> t.getCity().equals(Updater.this.city))
					.findFirst().orElseThrow(FetcherGTFS_RTNonExistent::new);
		}
		
		/**
		 * Parses the GTFS real-time feed and updates the CityManager's vehicle data.
		 * Private so that it can only be accessed by the outer class
		 * Clears existing vehicles before adding new ones from the feed.
		 */
		private void parse() {
			Updater.this.cityManager.isBeingUpdated();
			Updater.this.cityManager.resetVehiclesRT();
			for (var entity: fetcherGTFS_RT.getFeed().getEntityList()) {
				var vehicle = parseVehicle(entity, Updater.this.city);
				Updater.this.cityManager.registerVehicle(vehicle);
			}
			Updater.this.cityManager.terminateUpdate();
		}
		
		/**
		 * Parses a GTFS-RT feed entity into a Vehicle object.
		 * 
		 * Extracts vehicle ID, ...
		 * 
		 * @param entity GTFS-RT FeedEntity containing vehicle data
		 * @param city   city name (for vehicle tracking)
		 * @return Vehicle object created from entity data
		 * @throws IllegalArgumentException if entity has no vehicle data
		 */
		private static Vehicle parseVehicle(GtfsRealtime.FeedEntity entity, String city) {
			if (!entity.hasVehicle()) throw new IllegalArgumentException("FeedEntity does not contain a vehicle");
			var vp = entity.getVehicle();
			if (vp.getTrip().getStartDate().length() < 8) {
				try {   
						return new Vehicle(vp.getVehicle().getId(), 
				
						vp.getVehicle().getLabel(),
						vp.getStopId(),vp.getTimestamp(),
						vp.getCurrentStopSequence(), vp.getPosition().getLatitude(), 
						vp.getPosition().getLongitude(), city, vp.getPosition().getBearing(),
						vp.getPosition().getOdometer(), vp.getPosition().getSpeed(), 
						vp.getTrip().getTripId(), fastParseTime(vp.getTrip().getStartTime()), 
						null,
						vp.getTrip().getRouteId(), ""+ vp.getTrip().getDirectionId()
								);
						
					} catch (IllegalArgumentException e) {
						return null;
					
			}
			}
			
			 try {
				 return new Vehicle(vp.getVehicle().getId(), 
			 
					vp.getVehicle().getLabel(),
					vp.getStopId(),vp.getTimestamp(),
					vp.getCurrentStopSequence(), vp.getPosition().getLatitude(), 
					vp.getPosition().getLongitude(), city, vp.getPosition().getBearing(),
					vp.getPosition().getOdometer(), vp.getPosition().getSpeed(), 
					vp.getTrip().getTripId(), fastParseTime(vp.getTrip().getStartTime()), 
					LocalDate.parse(vp.getTrip().getStartDate(), DateTimeFormatter.ofPattern("yyyyMMdd")),
					vp.getTrip().getRouteId(), ""+ vp.getTrip().getDirectionId()
					
				);
			 } catch (IllegalArgumentException e) {
				 return null;
			 } catch (Exception e) {
				 System.out.print(e + " ");
				 System.out.println(vp.getTrip().getStartDate());
				 return null;
			 }
		}
	}
		
	
	
	
	
	
	
	
	
	/*
	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		try {
			var up = new Updater("Rome");
		} catch (DuplicationNotAccepted e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FetcherGTFS_RTNonExistent e) {
			OfflineConnectionChecker.getSingleton().update();
	    	MasterConnectionStatusChecker.getSingleton().update();
			var fetcher = new FetcherGTFS_RT("https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb", "Rome");
			Updater up;
			fetcher.update();
			try {
				up = new Updater("Rome");
				up.updateGTFS();
				System.out.println(Manager.getAllCityManagers().getFirst().getStopsPerTrip("1#1-6"));
				for (var route: Manager.getAllCityManagers().getFirst().getAllRoutes()) {
					if (Manager.getAllCityManagers().getFirst().getTripsPerRoute(route) == null) {
						System.out.println(route.getRouteId());
					} else if (Manager.searchCityManager("Rome").getTripsPerRoute(route) == null){
						System.out.println("problem with manager search");
						System.out.println(route.getRouteId());
					}
				}
				
				
				
				up.updateGTFS_RT();
				Manager.searchCityManager("Rome").getAllVehiclesRT()
				.stream().filter((vehicle) -> vehicle.getRouteId().contains("n"))
				.forEach((vehicle) -> System.out.println(vehicle.getRouteId() + " curr stop: " + vehicle.getCurrentStopSequence()));
			} catch (DuplicationNotAccepted e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		} catch (FetcherGTFS_RTNonExistent e1) {
				// impossible
			}
		}
	}
	*/

}

