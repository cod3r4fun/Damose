/**
 * The {@code model.transitEngine} package contains classes and interfaces
 * responsible for analyzing, estimating, and managing transit vehicle data
 * within a public transportation system.
 * <p>
 * This package provides implementations and abstractions for:
 * <ul>
 *   <li>Estimating expected arrival times of vehicles at stops based on
 *       real-time data and scheduled trips.</li>
 *   <li>Retrieving and analyzing the current positions of vehicles on
 *       routes, including support for both real-time and static data sources.</li>
 *   <li>Defining contracts (interfaces) for vehicle position analysis and
 *       transit engine calculations to enable flexible implementations.</li>
 * </ul>
 * <p>
 * Key classes include:
 * <ul>
 *   <li>{@link StandardVehicleAnalyser} - Provides logic to estimate vehicle arrival times at stops.</li>
 *   <li>{@link SimpleVehiclePositionAnalyzer} - Offers mechanisms to determine vehicle locations on routes.</li>
 * </ul>
 * <p>
 * This package depends on {@code model.vehicles} for domain models such as
 * {@code Vehicle}, {@code Stop}, and {@code Trip}, as well as utility classes
 * for time and location calculations.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */

package model.transitEngine;