package view;

import java.awt.Color;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import model.vehicles.Stop;



/**
 * Represents a waypoint on the map that corresponds to a transit stop and supports user interaction.
 * <p>
 * Extends {@link DefaultWaypoint} by associating a {@link Stop} object and an optional display color.
 * This class enables marking stops with customizable colors on the map.
 * </p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class ClickableWaypoint extends DefaultWaypoint {
    private final Stop stop;
    private final Color color;

    
    /**
     * Constructs a ClickableWaypoint with the specified stop and default color (red).
     * 
     * @param stop the transit stop represented by this waypoint; must not be null
     */
    public ClickableWaypoint(Stop stop) {
        this(stop, Color.RED); // default color
    }

    
    /**
     * Constructs a ClickableWaypoint with the specified stop and color.
     * 
     * @param stop the transit stop represented by this waypoint; must not be null
     * @param color the color to use for rendering this waypoint
     */
    public ClickableWaypoint(Stop stop, Color color) {
        super(stop.getPosition());
        this.stop = stop;
        this.color = color;
    }

    
    /**
     * Returns the associated transit stop.
     * 
     * @return the {@link Stop} object represented by this waypoint
     */
    public Stop getStop() {
        return stop;
    }

    
    /**
     * Returns the display color of this waypoint.
     * 
     * @return the {@link Color} used to render this waypoint
     */
    public Color getColor() {
        return color;
    }
}


