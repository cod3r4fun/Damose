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

public class ClockOrchestratorServerPrototype {
	public static final int CONNECTION_UPDATE_TIME_SECONDS = 5;
	private int initializedCities;
	private List<Updater> toUpdateGTFS;
	private List<Updater> toUpdateGTFS_RT;
	private List<FetcherGTFS> toFetch;
	private ScheduledExecutorService scheduler;
	private static ClockOrchestratorServerPrototype instance = new ClockOrchestratorServerPrototype();
	
	
	
	private ClockOrchestratorServerPrototype() {
		initializedCities = 0;
		toUpdateGTFS = new ArrayList<>();
		toUpdateGTFS_RT = new ArrayList<>();
	}
	
	private static ClockOrchestratorServerPrototype getInstance() {
		return instance;
	}
	
	private void checkConnection() {
		OfflineConnectionChecker.getSingleton().update();
		MasterConnectionStatusChecker.getSingleton().update();
		
	}
	
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
	
	private void fetchGTFS_RTData() {
		if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) return;
		for (var fetcher: FetcherGTFS_RT.getAllFetchers()) {
			fetcher.update();
		}
	}
	
	private void initializeFetchers() {
		for (var city: CityTrack.TRACKEDCITIES.keySet()) {
			try {
				
						new FetcherGTFS(CityTrack.TRACKEDCITIES.get(city).getX(), city);
				
				
						new FetcherGTFS_RT(CityTrack.TRACKEDCITIES.get(city).getY(), city);
			} catch (MalformedURLException | URISyntaxException e) {
				
				throw new IllegalArgumentException("check the CityTrack data for the GTFS_RT Url for city" + city);
				// shouldn't happen
			}
		}
		toFetch = new ArrayList<FetcherGTFS>(FetcherGTFS.getAllFetchers());
	}
	
	private void initializeUpdaters() {
		for (var city: CityTrack.TRACKEDCITIES.keySet()) {
			if ((Manager.searchCityManager(city) != null) || !(FetcherGTFS.getAllFetchers().stream().
					filter((t) -> t.getCity().equals(city)).collect(Collectors.toList()).getFirst().getHasBeenFetched())) {
				continue;
			}
			
			try {
				var up = new Updater(city);
				toUpdateGTFS.add(up);
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
	}
	
	
	private void parseGTFSData() {
		if (toUpdateGTFS.isEmpty()) return;
		for (Updater updater: List.copyOf(toUpdateGTFS)) {
			if (!updater.isGTFSParsed())
				try {
					updater.updateGTFS();
					toUpdateGTFS.remove(updater);
				} catch (InterruptedException | IOException e) {
					updater.setGTFSparsed(false);
				}
			}
		}
	
	
	private void parseGTFS_RTDATA() {
		if (toUpdateGTFS_RT.isEmpty()) return;
		for (Updater updater: toUpdateGTFS_RT) {
			updater.updateGTFS_RT();
		}
	}
	
	
	public static void start() {
		ClockOrchestratorServerPrototype.getInstance().launcher();
	}
	
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
		}, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleWithFixedDelay(this::parseGTFSData, 12, 15, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleWithFixedDelay(this::parseGTFS_RTDATA, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
	}
	
	
	
}
