// Written with chaGPT's help

package controller.connectionMakerAndControlUnit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FetcherGTFSTest {

    private URL dummyURL;
    private String city;

    @TempDir
    Path tempDir;

    
	@BeforeEach
    void setUp() throws MalformedURLException, URISyntaxException {
        dummyURL = new URI("https://example.com/fakeGTFS.zip").toURL();
        city = "city";

        // Reset static fields before each test
        resetStaticState();
    }

    @AfterEach
    void tearDown() {
        resetStaticState();
    }

    private void resetStaticState() {
        try {
            Field allFetchersField = FetcherGTFS.class.getDeclaredField("allFetchers");
            allFetchersField.setAccessible(true);
            ((List<?>) allFetchersField.get(null)).clear();

            Field counter = FetcherGTFS.class.getDeclaredField("allObjectsNumber");
            counter.setAccessible(true);
            counter.setInt(null, 0);
        } catch (Exception e) {
            throw new RuntimeException("Unable to reset static fields", e);
        }
    }

    @Test
    void testConstructorFromURL() {
        FetcherGTFS fetcher = new FetcherGTFS(dummyURL, city);
        assertNotNull(fetcher);
    }

    @Test
    void testConstructorFromURI() throws URISyntaxException, MalformedURLException {
        URI uri = new URI("https://example.com/fakeGTFS.zip");
        FetcherGTFS fetcher = new FetcherGTFS(uri, city);
        assertNotNull(fetcher);
    }

    @Test
    void testConstructorFromString() throws Exception {
        String url = "https://example.com/fakeGTFS.zip";
        FetcherGTFS fetcher = new FetcherGTFS(url, city);
        assertNotNull(fetcher);
    }

    @Test
    void testAllFetchersTracking() throws Exception {
        new FetcherGTFS("https://example.com/1", city);
        new FetcherGTFS("https://example.com/2", city);
        assertEquals(2, FetcherGTFS.getAllFetchers().size());
    }



    @Test
    void testStringGeneratorFormat() throws Exception {
        FetcherGTFS fetcher = new FetcherGTFS(dummyURL, city);
        Field field = FetcherGTFS.class.getDeclaredField("thisIdObject");
        field.setAccessible(true);
        int id = field.getInt(fetcher);
        String expectedPath = "GTFS" + File.separator + city + "/" + "_";

        Field pathField = FetcherGTFS.class.getDeclaredField("locationOfFile");
        pathField.setAccessible(true);
        assertEquals(expectedPath, pathField.get(fetcher));
    }


    @Test
    void testGTFSDirectoryCreatedOnlyOnce() throws Exception {
        FetcherGTFS f1 = new FetcherGTFS(dummyURL, city);
        FetcherGTFS f2 = new FetcherGTFS(dummyURL, city);

        File dir = new File(System.getProperty("user.dir") + "/GTFS");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }


}

