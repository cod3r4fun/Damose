// written by chatGPT
package view;

import java.io.File;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import controller.connectionMakerAndControlUnit.MasterConnectionStatusChecker;
import utility.SysConstants;


/**
 * Utility class for creating tile factories with caching and prefetching map tiles.
 * <p>
 * This class provides methods to instantiate a {@link DefaultTileFactory} that uses
 * a persistent local cache for storing map tiles and to prefetch map tiles by
 * simulating viewport rendering at specified zoom levels and geographic bounds.
 * </p>
 * 
 * <p>Prefetching tiles can improve map responsiveness by loading tiles in advance.</p>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class TilePrefetcher {

    /**
     * Creates a {@link DefaultTileFactory} configured with a persistent local cache.
     * <p>
     * The cache is stored under the current working directory in the ".jxmapviewer" folder.
     * This allows for tile images to be reused across application runs, reducing network load.
     * </p>
     * 
     * @param info the tile factory information specifying the map tile source and parameters; must not be null
     * @return a {@link DefaultTileFactory} with local caching enabled
     */
    public static DefaultTileFactory createCachedTileFactory(TileFactoryInfo info) {
        FileBasedLocalCache cache = new FileBasedLocalCache(new File(SysConstants.CURRENTDIR + ".jxmapviewer"), false);
        DefaultTileFactory factory = new DefaultTileFactory(info);
        factory.setLocalCache(cache);
        return factory;
    }


    
    /**
     * Prefetches map tiles by simulating viewport rendering between two geographic corners at specified zoom levels.
     * <p>
     * This method creates a dummy {@link JXMapViewer} instance that sets its tile factory,
     * zoom level, and center position repeatedly to trigger the tile loading mechanism.
     * The prefetching only occurs if the connection is active according to
     * {@link MasterConnectionStatusChecker}.
     * </p>
     * <p>
     * The prefetching delays briefly between zoom level changes to allow asynchronous tile loading.
     * </p>
     * 
     * @param tileFactory the tile factory to use for fetching tiles; must not be null
     * @param topLeft the geographic top-left corner of the rectangular area to prefetch; must not be null
     * @param bottomRight the geographic bottom-right corner of the rectangular area to prefetch; must not be null
     * @param minZoom the minimum zoom level (inclusive) to prefetch
     * @param maxZoom the maximum zoom level (inclusive) to prefetch
     */
    public static void prefetchViaDisplay(DefaultTileFactory tileFactory, GeoPosition topLeft, GeoPosition bottomRight, int minZoom, int maxZoom) {
        JXMapViewer dummyViewer = new JXMapViewer();
        dummyViewer.setTileFactory(tileFactory);
        dummyViewer.setSize(800, 600);

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            dummyViewer.setZoom(zoom);

            GeoPosition center = new GeoPosition(
                (topLeft.getLatitude() + bottomRight.getLatitude()) / 2,
                (topLeft.getLongitude() + bottomRight.getLongitude()) / 2
            );

            dummyViewer.setAddressLocation(center);

            // Aspetta che la connessione sia attiva prima di procedere (con timeout)
            final long waitTimeout = 5000;
            long waitStart = System.currentTimeMillis();

            while (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) {
                if (System.currentTimeMillis() - waitStart > waitTimeout) {
                    System.out.println("Connessione non disponibile (timeout prima di repaint), salto zoom " + zoom);
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // Procedi solo se connessione è ancora attiva
            if (MasterConnectionStatusChecker.getSingleton().isConnectionActive()) {
                dummyViewer.repaint();

                // Attesa post-repaint, ma interrompibile se la connessione cade
                final long postRepaintTimeout = 3000;
                long postStart = System.currentTimeMillis();
                while (System.currentTimeMillis() - postStart < postRepaintTimeout) {
                    if (!MasterConnectionStatusChecker.getSingleton().isConnectionActive()) {
                        break;
                    }
                    try {
                        Thread.sleep(200); // breve pausa
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}
