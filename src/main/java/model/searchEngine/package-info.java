/**
 * This package contains classes and interfaces responsible for searching
 * and retrieving transit stop times and schedules.
 * 
 * It provides implementations and abstractions to query stop times
 * based on routes, stops, or both, supporting both real-time and static data sources.
 * 
 * Key interfaces:
 * - {@link model.searchEngine.Searcher} - defines the contract for searching stop times.
 * 
 * Key classes:
 * - {@link model.searchEngine.simpleSearch} - a concrete implementation of the Searcher interface
 *   that fetches stop times with support for real-time data when available.
 *
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * 
 * 
 */

package model.searchEngine;