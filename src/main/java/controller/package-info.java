/**
 * Provides core orchestration and control functionality for the application runtime.
 *
 * <p>This package contains classes responsible for high-level coordination of data fetching,
 * parsing, connection health monitoring, and updater lifecycle management. These controllers
 * act as the bridge between the external data sources (e.g., GTFS feeds) and the internal
 * application state, ensuring that all tracked cities remain synchronized with live and static
 * transit data feeds.</p>
 *
 * <h2>Key Responsibilities</h2>
 * <ul>
 *   <li>Managing scheduled execution flows via {@link ClockOrchestrator}</li>
 *   <li>Maintaining a consistent time reference across the system via {@link DateController}</li>
 *   <li>Orchestrating city-specific GTFS and GTFS-RT data processing pipelines</li>
 *   <li>Delegating data acquisition to fetchers and state updates to updaters</li>
 *   <li>Integrating with the UI bootstrapper and core city tracking utilities</li>
 * </ul>
 *
 * <h2>Sub-Packages</h2>
 * <ul>
 *   <li>{@code controller.connectionMakerAndControlUnit} – encapsulates GTFS connection handling and connection status checking</li>
 * </ul>
 *
 * <h2>Threading & Scheduling</h2>
 * {@link ClockOrchestrator} leverages a multi-threaded scheduled executor service to manage polling, parsing, and data refresh intervals, ensuring responsiveness and scalability across multiple cities.
 *
 * * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 *
 * @see controller.ClockOrchestrator
 * @see controller.DateController
 */
package controller;
