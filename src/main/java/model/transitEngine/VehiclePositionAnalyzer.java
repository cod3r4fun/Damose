package model.transitEngine;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import model.vehicles.Manager;




/**
 * Interface for analyzing and retrieving vehicle positions on transit routes.
 * <p>
 * Implementations may provide vehicle positions based on real-time data or static schedule data.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public interface VehiclePositionAnalyzer {
	
    /**
     * Retrieves a list of vehicle positions for the specified route.
     * <p>
     * The positions may be based on real-time vehicle tracking or static scheduled trip data,
     * depending on the implementation and availability of real-time data.
     * </p>
     * 
     * @param routeId the identifier of the route for which vehicle positions are requested
     * @param cityManager the city manager providing access to transit data
     * @return a list of {@link GeoPosition} objects representing the current locations of vehicles on the route
     */
	public List<GeoPosition> findVehiclePositionsForRoute(String routeId, Manager.CityManager cityManager);
	
	
    /**
     * Retrieves a list of vehicle positions for the specified route and direction.
     * <p>
     * The positions may be based on real-time vehicle tracking or static scheduled trip data,
     * filtered by direction.
     * </p>
     * 
     * @param routeId the identifier of the route for which vehicle positions are requested
     * @param direction the direction identifier within the route
     * @param cityManager the city manager providing access to transit data
     * @return a list of {@link GeoPosition} objects representing the current locations of vehicles on the route and direction
     */
	public List<GeoPosition> findVehiclePositionsForRouteAndDirection(String routeId, String direction, Manager.CityManager cityManager);
}
