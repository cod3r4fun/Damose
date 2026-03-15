package view;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;


/**
 * Represents a waypoint on the map corresponding to a vehicle's position, 
 * with an associated label for display or identification purposes.
 * <p>
 * Extends {@link DefaultWaypoint} by adding a descriptive label.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class VehicleWaypoint extends DefaultWaypoint {
    private final String label;

    
    /**
     * Constructs a VehicleWaypoint at the specified geographic position with the given label.
     * 
     * @param position the geographic position of the vehicle; must not be null
     * @param label a descriptive label for this vehicle waypoint
     */
    public VehicleWaypoint(GeoPosition position, String label) {
        super(position);
        this.label = label;
    }

    /**
     * Returns the label associated with this vehicle waypoint.
     * 
     * @return the label string
     */
    public String getLabel() {
        return label;
    }
}

