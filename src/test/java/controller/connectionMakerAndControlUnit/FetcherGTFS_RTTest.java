// Written with chaGPT's help
package controller.connectionMakerAndControlUnit;

import com.google.transit.realtime.GtfsRealtime; 
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FetcherGTFS_RTTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void resetStatics() throws Exception {
        // Reset static counter and list
        Field counter = FetcherGTFS_RT.class.getDeclaredField("allObjectsNumber");
        counter.setAccessible(true);
        counter.setInt(null, 0);
        
        Field list = FetcherGTFS_RT.class.getDeclaredField("allFetchers");
        list.setAccessible(true);
        ((CopyOnWriteArrayList<?>) list.get(null)).clear();
    }

    /**
     * Helper: writes a minimal GTFS-Realtime FeedMessage with required header
     * and optional entity, then returns its file:// URL.
     */
    private URL writeProto(Path target, GtfsRealtime.FeedMessage.Builder builder) throws IOException {
        builder.setHeader(GtfsRealtime.FeedHeader.newBuilder()
                .setGtfsRealtimeVersion("2.0")
                .setTimestamp(System.currentTimeMillis() / 1000)
                .build());
        GtfsRealtime.FeedMessage msg = builder.build();
        try (FileOutputStream out = new FileOutputStream(target.toFile())) {
            msg.writeTo(out);
        }
        return target.toUri().toURL();
    }

    @Test
    void urlConstructorStoresReferenceAndAssignsId() throws Exception {
        URL fileUrl = writeProto(tempDir.resolve("feed0.pb"), GtfsRealtime.FeedMessage.newBuilder());
        String city = "city";
        FetcherGTFS_RT f = new FetcherGTFS_RT(fileUrl, city);

        // Verify stored URL via reflection
        Field urlField = FetcherGTFS_RT.class.getDeclaredField("referenceGTFS_RT");
        urlField.setAccessible(true);
        assertEquals(fileUrl, urlField.get(f));

        // Verify ID assignment
        assertEquals(0, f.getThisIdObject());
    }

    @Test
    void uriConstructorDelegatesToUrlConstructor() throws Exception {
        URL u = writeProto(tempDir.resolve("feed1.pb"), GtfsRealtime.FeedMessage.newBuilder());
        String city = "city";
        FetcherGTFS_RT f = new FetcherGTFS_RT(u.toURI(), city);
        assertEquals(0, f.getThisIdObject());
        assertEquals(u, getReferenceURL(f));
    }

    @Test
    void stringConstructorDelegatesCorrectly() throws Exception {
        URL u = writeProto(tempDir.resolve("feed2.pb"), GtfsRealtime.FeedMessage.newBuilder());
        String city = "city";
        FetcherGTFS_RT f = new FetcherGTFS_RT(u.toString(), city);
        assertEquals(0, f.getThisIdObject());
        assertEquals(u, getReferenceURL(f));
    }

    @Test
    void invalidStringConstructorThrows() {
    	String city = "city";
        assertThrows(URISyntaxException.class, () -> new FetcherGTFS_RT("not a uri", city));
    }


    @Test
    void networkErrorIsCaughtAndFeedRemainsUnchanged() throws Exception {
        // create a bogus URL with custom handler to throw IOException
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                throw new IOException("simulated failure");
            }
        };
        URL bad = new URL(null, "http://bad/test", handler);
        String city = "city";

        FetcherGTFS_RT f = new FetcherGTFS_RT(bad, city);
        // set a dummy feed first
        Field feedField = FetcherGTFS_RT.class.getDeclaredField("feed");
        feedField.setAccessible(true);
        GtfsRealtime.FeedMessage dummy = GtfsRealtime.FeedMessage.newBuilder()
            .setHeader(GtfsRealtime.FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0").setTimestamp(0).build())
            .build();
        feedField.set(f, dummy);

        f.update();
        assertSame(dummy, f.getFeed(), "feed should remain unchanged on IOException");
    }

    @Test
    void getAllFetchersTracksInstancesAndIsUnmodifiable() throws Exception {
        URL u = writeProto(tempDir.resolve("a.pb"), GtfsRealtime.FeedMessage.newBuilder());
        String city = "city";
        FetcherGTFS_RT f1 = new FetcherGTFS_RT(u, city);
        FetcherGTFS_RT f2 = new FetcherGTFS_RT(u, city);

        List<FetcherGTFS_RT> snapshot = f1.getAllFetchers();
        assertEquals(2, snapshot.size());
        assertTrue(snapshot.containsAll(List.of(f1, f2)));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(f1));
    }

    @Test
    void multipleInstancesIncreaseIdMonotonically() throws Exception {
    	String city = "city";
        URL u = writeProto(tempDir.resolve("b.pb"), GtfsRealtime.FeedMessage.newBuilder());
        FetcherGTFS_RT f1 = new FetcherGTFS_RT(u, city);
        FetcherGTFS_RT f2 = new FetcherGTFS_RT(u, city);
        FetcherGTFS_RT f3 = new FetcherGTFS_RT(u, city);
        assertEquals(0, f1.getThisIdObject());
        assertEquals(1, f2.getThisIdObject());
        assertEquals(2, f3.getThisIdObject());
    }

    /** Reflectively retrieve the private referenceGTFS_RT URL */
    private URL getReferenceURL(FetcherGTFS_RT f) throws Exception {
        Field urlField = FetcherGTFS_RT.class.getDeclaredField("referenceGTFS_RT");
        urlField.setAccessible(true);
        return (URL) urlField.get(f);
    }
}

