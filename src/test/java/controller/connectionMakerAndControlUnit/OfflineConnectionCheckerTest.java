// Written with chaGPT's help

package controller.connectionMakerAndControlUnit;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfflineConnectionCheckerTest {

    private OfflineConnectionChecker checker;
    

    static class MockObserver implements utility.Observer {
        boolean updated = false;

        @Override
        public void update() {
            updated = true;
        }

        public boolean wasUpdated() {
            return updated;
        }
    }

    @BeforeEach
    void setUp() {
        checker = OfflineConnectionChecker.getSingleton();
        checker.getObservers().clear();
        checker.getUrls().clear();
    }

    // ──────────────────────────────────────────────
    // Singleton
    // ──────────────────────────────────────────────
    @Test
    void testSingletonReturnsSameInstance() {
        assertSame(checker, OfflineConnectionChecker.getSingleton());
    }

    @Test
    void testSingletonNotNull() {
        assertNotNull(OfflineConnectionChecker.getSingleton());
    }

    // ──────────────────────────────────────────────
    // addURL(String)
    // ──────────────────────────────────────────────
    @Test
    void testAddURLFromString_validURL() {
        String testUrl = "https://example.com";
        checker.addURL(testUrl);
        assertFalse(checker.getUrls().isEmpty());
    }

    @Test
    void testAddURLFromString_malformedURL() {
        String malformed = "ht!tp://[invalid]";
        checker.addURL(malformed); // Should fail silently
        assertTrue(checker.getUrls().isEmpty());
    }

    @Test
    void testAddURLFromString_nullInput() {
        checker.addURL((String) null);
        assertTrue(checker.getUrls().isEmpty());
    }

    // ──────────────────────────────────────────────
    // addURL(URI)
    // ──────────────────────────────────────────────
    @Test
    void testAddURLFromURI_valid() throws URISyntaxException {
        URI uri = new URI("https://example.com");
        checker.addURL(uri);
        assertEquals("https://example.com", checker.getUrls().get(0).toString());
    }

    @Test
    void testAddURLFromURI_null() {
        checker.addURL((URI) null);
        assertTrue(checker.getUrls().isEmpty());
    }

    @Test
    void testAddURLFromURI_invalidScheme() throws URISyntaxException {
        URI uri = new URI("ftp://example.com");
        checker.addURL(uri);
        assertEquals("ftp://example.com", checker.getUrls().get(0).toString());
    }

    // ──────────────────────────────────────────────
    // addURL(URL)
    // ──────────────────────────────────────────────
    @Test
    void testAddURLFromURL_valid() throws Exception {
        URL url = new URI("https://example.com").toURL();
        checker.addURL(url);
        assertFalse(checker.getUrls().isEmpty());
    }

    @Test
    void testAddURLFromURL_duplicate() throws Exception {
        URL url = new URI("https://example.com").toURL();
        checker.addURL(url);
        checker.addURL(url);
        assertEquals(2, checker.getUrls().size());
    }

    @Test
    void testAddURLFromURL_null() {
        checker.addURL((URL) null);
        assertTrue(checker.getUrls().isEmpty());
    }

    // ──────────────────────────────────────────────
    // Observers
    // ──────────────────────────────────────────────
    @Test
    void testAttachAndNotifyObserver() {
        MockObserver observer = new MockObserver();
        checker.attach(observer);
        checker.notifyObservers();
        assertTrue(observer.wasUpdated());
    }

    @Test
    void testAttachMultipleObservers() {
        var o1 = new MockObserver();
        var o2 = new MockObserver();
        checker.attach(o1);
        checker.attach(o2);
        checker.notifyObservers();
        assertTrue(o1.wasUpdated() && o2.wasUpdated());
    }

    @Test
    void testDetachObserver() {
        MockObserver observer = new MockObserver();
        checker.attach(observer);
        checker.detach(observer);
        checker.notifyObservers();
        assertFalse(observer.wasUpdated());
    }

}
