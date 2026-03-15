package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;




/**
 * A painter that draws a route on a {@link JXMapViewer} as a connected line between a sequence of geographic positions.
 * <p>
 * This class converts a list of {@link GeoPosition} coordinates into pixel points on the map,
 * then draws lines connecting these points in the specified color and stroke width.
 * </p>
 * 
 * <p>This painter is suitable for visualizing routes or tracks on a map.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class RoutePainter implements Painter<JXMapViewer> {
    private final List<GeoPosition> track;
    private final Color color;
    private final float strokeWidth;

    
    
    /**
     * Creates a new RoutePainter.
     * 
     * @param track the list of {@link GeoPosition}s representing the route to draw; must not be null or empty
     * @param color the {@link Color} to draw the route line
     * @param strokeWidth the width of the route line stroke in pixels
     */
    public RoutePainter(List<GeoPosition> track, Color color, float strokeWidth) {
        this.track = track;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    
    
    /**
     * Paints the route on the given map viewer.
     * <p>
     * The geographic positions are projected to pixel coordinates relative to the map's viewport,
     * then connected with straight lines using the specified color and stroke width.
     * </p>
     *
     * @param g the {@link Graphics2D} context to paint on
     * @param map the {@link JXMapViewer} instance on which to paint
     * @param w the width of the painting area (ignored)
     * @param h the height of the painting area (ignored)
     */
    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();
        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth));

        List<Point> points = track.stream()
            .map(pos -> map.getTileFactory().geoToPixel(pos, map.getZoom()))
            .map(p -> new Point((int) p.getX() - map.getViewportBounds().x,
                                (int) p.getY() - map.getViewportBounds().y))
            .toList();

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        g.dispose();
    }
}
