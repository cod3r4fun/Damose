package model.searchEngine;
import model.vehicles.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;


/**
 * Interface defining search operations for transit stop times
 * based on routes and stops.
 */
public interface Searcher {
	
    /**
     * Searches for stop times mapped by stops for a given route.
     *
     * @param routeId the identifier of the route to search for.
     * @return a map where each key is a {@link Stop} on the route, and
     *         the value is a list of {@link LocalTime} objects representing
     *         the stop times for that stop on the route.
     */
	public Map<Stop, List<LocalTime>> searchStopTimesPerRoute(String routeId);
	
	
    /**
     * Searches for stop times mapped by routes for a given stop.
     *
     * @param stopId the identifier of the stop to search for.
     * @return a map where each key is a {@link Route} serving the stop, and
     *         the value is a list of {@link LocalTime} objects representing
     *         the stop times for that stop on each route.
     */
	public Map<Route, List<LocalTime>> searchStopTimesPerStop(String stopId);
	
	
    /**
     * Searches for stop times for a specific stop on a specific route.
     *
     * @param routeId the identifier of the route.
     * @param stopId  the identifier of the stop.
     * @return a list of {@link LocalTime} objects representing the stop times
     *         for the specified stop on the specified route.
     */
	public List<LocalTime> searchStopTimesPerRouteAndStop(String routeId, String stopId);
}
