package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;


/**
 * Renders {@link VehicleWaypoint} instances on a {@link JXMapViewer}.
 * <p>
 * Displays each vehicle waypoint as a blue filled circle with a label drawn adjacent to it.
 * </p>
 * 
 * <p>This implementation customizes the visual representation of vehicle waypoints,
 * ensuring clear visibility on the map overlay.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class VehicleWaypointRenderer implements WaypointRenderer<VehicleWaypoint> {

	
    /**
     * Paints the specified vehicle waypoint on the provided map viewer.
     * <p>
     * The waypoint is rendered as a blue circle with its label drawn in black next to it.
     * Coordinates are transformed from geographic positions to screen pixel positions
     * using the map's tile factory and zoom level.
     * </p>
     *
     * @param g the graphics context used for painting; must not be null
     * @param map the map viewer on which the waypoint is painted; must not be null
     * @param wp the vehicle waypoint to paint; must not be null
     */
    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, VehicleWaypoint wp) {
        int radius = 5;

        Point2D screenPoint = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

        
        g.setColor(Color.BLUE);
        g.fillOval((int) screenPoint.getX() - radius, (int) screenPoint.getY() - radius, radius * 2, radius * 2);

        g.setColor(Color.BLACK);
        g.drawString(wp.getLabel(), (int) screenPoint.getX() + radius + 2, (int) screenPoint.getY());
    }
}
