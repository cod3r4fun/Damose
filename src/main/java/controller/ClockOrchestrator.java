package controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import controller.connectionMakerAndControlUnit.FetcherGTFS;
import controller.connectionMakerAndControlUnit.FetcherGTFS_RT;
import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import controller.connectionMakerAndControlUnit.OfflineConnectionChecker;
import model.vehiclePositionUpdater.Updater;
import model.vehicles.Manager;
import utility.CityTrack;
import utility.exceptionUtils.DuplicationNotAccepted;
import utility.exceptionUtils.FetcherGTFS_RTNonExistent;




/**
 * {@code ClockOrchestrator} governs the end-to-end initialization and lifecycle
 * management of the data pipeline for GTFS and GTFS-RT feeds.
 * 
 * <p>This class encapsulates connection monitoring, data fetching, parsing,
 * and updater orchestration in a time-scheduled, thread-safe manner. It
 * coordinates fetchers and updaters responsible for downloading and processing
 * transit data streams.</p>
 *
 * <p><b>Key responsibilities:</b></p>
 * <ul>
 *   <li>Initialize GTFS and GTFS-RT fetchers for the currently tracked city</li>
 *   <li>Periodically assess network availability and feed accessibility</li>
 *   <li>Schedule periodic tasks to fetch static (GTFS) and real-time (GTFS-RT) data</li>
 *   <li>Initialize and coordinate updaters for parsing and ingesting data into the system</li>
 *   <li>Track update completion and orchestrate multi-city expansion where applicable</li>
 * </ul>
 *
 * <p>The orchestration is driven by a fixed-rate and fixed-delay thread pool
 * scheduler, ensuring predictable timing behavior across all responsibilities.
 * Network activity and GTFS parsing operations are only executed when
 * the system verifies an active and stable connection.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *     // Launch the orchestrator lifecycle
 *     ClockOrchestrator.start();
 * </pre>
 * 
 * <p>This class is implemented as a singleton, providing a global coordinator
 * for the lifecycle of GTFS-based city data ingestion.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class ClockOrchestrator {
	
    /** Interval in seconds for connection status polling. */
	public static final int CONNECTION_UPDATE_TIME_SECONDS = 5;
	private List<FetcherGTFS> toFetch;
	private List<Updater> toUpdateGTFS;
	private List<Updater> toUpdateGTFS_RT;
	private int initializedCities;
	private String city;
	private boolean updateFinished;
	private ScheduledExecutorService scheduler;
	
    /** Singleton instance. */
	private static ClockOrchestrator instance = new ClockOrchestrator();
	
	
    /**
     * Private constructor initializes tracking structures and loads the city context.
     */
	private ClockOrchestrator() {
		initializedCities = 0;
		toFetch = new ArrayList<>();
		toUpdateGTFS = new ArrayList<>();
		toUpdateGTFS_RT = new ArrayList<>();
		city = CityTrack.getTrackedCity();
		updateFinished = false;
	}
	
	
    /**
     * Returns whether all scheduled GTFS updates have been completed.
     * 
     * @return {@code true} if updates are finished, {@code false} otherwise
     */
	public static boolean isUpdateFinished() {
		return ClockOrchestrator.getInstance().updateFinished;
	}
	
	private static ClockOrchestrator getInstance() {
		return instance;
	}
	
	
	/**
	 * Checks the network and master connection status, updating
	 * internal flags used to determine fetcher activity eligibility.
	 */
	private void checkConnection() {
		OfflineConnectionChecker.getSingleton().update();
		MasterConnectionStatusChecker.getSingleton().update();
			
	}
	
	/**
	 * Initiates GTFS data fetch operations using registered fetchers.
	 * 
	 * <p>Fetchers are removed from the queue once successfully completed.</p>
	 */
	private void fetchGTFSData() {
		if (toFetch.isEmpty()) return;
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return;
		for (var fetcher: List.copyOf(toFetch)) {
			if (fetcher.getHasBeenFetched()) {
				toFetch.remove(fetcher);
				continue;
			}
			fetcher.update();
		}
	}
	
	/**
	 * Initiates GTFS-RT (real-time) data fetch operations from all
	 * GTFS-RT fetchers. Skipped if no active connection is available.
	 */
	private void fetchGTFS_RTData() {
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return;
		for (var fetcher: FetcherGTFS_RT.getAllFetchers()) {
			fetcher.update();
		}
	}
	
	
	/**
	 * Initializes GTFS and GTFS-RT fetchers for the tracked city.
	 * 
	 * <p>Expected to be called once during orchestrator startup.</p>
	 *
	 * @throws IllegalArgumentException if any city URL is malformed
	 */
	private void initializeFetchers() {
			try {
				
				new FetcherGTFS(CityTrack.TRACKEDCITIES.get(city).getX(), city);
				
				
				new FetcherGTFS_RT(CityTrack.TRACKEDCITIES.get(city).getY(), city);
			} catch (MalformedURLException | URISyntaxException e) {
				
				throw new IllegalArgumentException("check the CityTrack data for the GTFS_RT Url for city" + city);
				// shouldn't happen
			}
		toFetch = new ArrayList<FetcherGTFS>(FetcherGTFS.getAllFetchers());
	}
	
	
	/**
	 * Attempts to create and register an {@link Updater} instance for the city,
	 * once GTFS data has been successfully fetched.
	 * 
	 * <p>Handles cases where real-time GTFS fetchers must be created on-demand.</p>
	 */
	private void initializeUpdaters() {
			if ((Manager.searchCityManager(city) != null) || !(FetcherGTFS.getAllFetchers().stream().
					filter((t) -> t.getCity().equals(city)).collect(Collectors.toList()).getFirst().getHasBeenFetched())) {
				return;
			}
			
			try {
				var up = new Updater(city);
				toUpdateGTFS.add(up);
				//System.out.println("up to be initialized");
				toUpdateGTFS_RT.add(up);
				initializedCities++;
			} catch (DuplicationNotAccepted e) {
				// already present, nothing to do (shouln't happen)
			} catch (FetcherGTFS_RTNonExistent e) {
				try {
					new FetcherGTFS_RT(CityTrack.TRACKEDCITIES.get(city).getY(), city);
					try {
						var up = new Updater(city);
						toUpdateGTFS.add(up);
						toUpdateGTFS_RT.add(up);
						initializedCities++;
					} catch (DuplicationNotAccepted | FetcherGTFS_RTNonExistent e1) {
						// doesn't happen
						e1.printStackTrace();
					}
				} catch (MalformedURLException | URISyntaxException e1) {
					
					throw new IllegalArgumentException("check the CityTrack data for the GTFS_RT Url for city" + city);
					
					// shouldn't happen
				}
			}
	}
	
	
	/**
	 * Triggers GTFS data parsing for all registered {@link Updater} instances.
	 * 
	 * <p>Each updater is removed from the processing queue after successful parsing.</p>
	 */
	private void parseGTFSData() {
		if (toUpdateGTFS.isEmpty()) return;
		for (Updater updater: List.copyOf(toUpdateGTFS)) {
			if (!updater.isGTFSParsed())
				try {
					updater.updateGTFS();
					toUpdateGTFS.remove(updater);				
					updateFinished = updater.isGTFSParsed();
					//System.out.println(updateFinished);			// to remove
				} catch (InterruptedException | IOException e) {
					updater.setGTFSparsed(false);
				}
			}
		}
	
	
	/**
	 * Invokes real-time data parsing for each registered {@link Updater}.
	 * 
	 * <p>Errors are logged but do not interrupt the scheduling flow.</p>
	 */
	private void parseGTFS_RTDATA() {
		if (toUpdateGTFS_RT.isEmpty()) return;
		for (Updater updater: toUpdateGTFS_RT) {
			try {
			updater.updateGTFS_RT();
			} catch (Exception e) {
				//System.out.println(e);
			}
			}	
		}
	
	
	
	 /**
     * Starts the orchestrator's full lifecycle. Initializes fetchers,
     * starts the scheduled connection monitoring, data fetching, and parsing logic.
     */
	public static void start() {
		ClockOrchestrator.getInstance().launcher();
	}
	
	
	/**
	 * Bootstraps and configures the thread scheduler to handle all orchestrator
	 * operations using fixed-rate and fixed-delay execution policies.
	 *
	 * <p>This method should only be called once from {@link #start()}.</p>
	 */
	private void launcher() {
		scheduler = java.util.concurrent.Executors.newScheduledThreadPool(6);

		initializeFetchers();
		
		checkConnection();

		scheduler.scheduleAtFixedRate(this::checkConnection, 0, CONNECTION_UPDATE_TIME_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
		
		scheduler.scheduleAtFixedRate(this::fetchGTFSData, 1, 60, java.util.concurrent.TimeUnit.SECONDS);
		
		scheduler.scheduleAtFixedRate(this::fetchGTFS_RTData, 1, 30, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleWithFixedDelay(() -> {
			if (initializedCities < CityTrack.NUMBEROFCITIESTRACKED) {
				initializeUpdaters();
			}
		}, 4, 3, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleWithFixedDelay(this::parseGTFSData, 5, 15, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleWithFixedDelay(this::parseGTFS_RTDATA, 20, 15, java.util.concurrent.TimeUnit.SECONDS);
	}
	
	
	
}
