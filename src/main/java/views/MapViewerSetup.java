package views;

import java.io.File;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import entity.GeoPoint;

final class MapViewerSetup {
    private static final int THREAD_POOL_SIZE = 4;
    private static final int ZOOM_LEVEL = 3;

    private MapViewerSetup() {
    }

    static JXMapViewer create(final GeoPoint origin) {
        final TileFactoryInfo tileInfo = new OSMTileFactoryInfo();
        final File cacheDirectory = new File(System.getProperty("user.home"), ".flushid/map-cache");
        final DefaultTileFactory tileFactory = new DefaultTileFactory(tileInfo);
        tileFactory.setUserAgent("FlushID/1.0 (U of T student map demo)");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDirectory, false));
        tileFactory.setThreadPoolSize(THREAD_POOL_SIZE);
        final JXMapViewer viewer = new JXMapViewer();
        viewer.setTileFactory(tileFactory);
        viewer.setAddressLocation(new org.jxmapviewer.viewer.GeoPosition(origin.latitude(), origin.longitude()));
        viewer.setZoom(ZOOM_LEVEL);
        viewer.setRestrictOutsidePanning(true);
        viewer.setHorizontalWrapped(false);
        final PanMouseInputListener pan = new PanMouseInputListener(viewer);
        viewer.addMouseListener(pan);
        viewer.addMouseMotionListener(pan);
        viewer.addMouseListener(new CenterMapListener(viewer));
        viewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(viewer));
        viewer.addKeyListener(new PanKeyListener(viewer));
        return viewer;
    }
}
