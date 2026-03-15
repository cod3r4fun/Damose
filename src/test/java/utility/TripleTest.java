package utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TripleTest {

    @Test
    void constructorShouldSetAllFieldsCorrectly() {
        Triple<String, Integer, Boolean> triple = new Triple<>("Alice", 30, true);
        assertEquals("Alice", triple.getX());
        assertEquals(30, triple.getY());
        assertTrue(triple.getZ());
    }

    @Test
    void getXShouldReturnCorrectValue() {
        Triple<String, String, String> triple = new Triple<>("X", "Y", "Z");
        assertEquals("X", triple.getX());
    }

    @Test
    void getYShouldReturnCorrectValue() {
        Triple<String, String, String> triple = new Triple<>("X", "Y", "Z");
        assertEquals("Y", triple.getY());
    }

    @Test
    void getZShouldReturnCorrectValue() {
        Triple<String, String, String> triple = new Triple<>("X", "Y", "Z");
        assertEquals("Z", triple.getZ());
    }

    @Test
    void toStringShouldConcatenateAllThreeElements() {
        Triple<String, String, String> triple = new Triple<>("A", "B", "C");
        assertEquals("ABC", triple.toString());
    }

    @Test
    void toStringShouldHandleComplexObjects() {
        Triple<Object, Object, Object> triple = new Triple<>(new StringBuilder("1"), new StringBuilder("2"), new StringBuilder("3"));
        assertEquals("123", triple.toString());
    }

    @Test
    void toStringShouldThrowIfXIsNull() {
        Triple<Object, Object, Object> triple = new Triple<>(null, "B", "C");
        assertThrows(NullPointerException.class, triple::toString);
    }

    @Test
    void toStringShouldThrowIfYIsNull() {
        Triple<Object, Object, Object> triple = new Triple<>("A", null, "C");
        assertThrows(NullPointerException.class, triple::toString);
    }

    @Test
    void toStringShouldThrowIfZIsNull() {
        Triple<Object, Object, Object> triple = new Triple<>("A", "B", null);
        assertThrows(NullPointerException.class, triple::toString);
    }

    @Test
    void toStringShouldThrowIfAllAreNull() {
        Triple<Object, Object, Object> triple = new Triple<>(null, null, null);
        assertThrows(NullPointerException.class, triple::toString);
    }

    @Test
    void supportsNullValuesInFields() {
        Triple<String, Integer, Boolean> triple = new Triple<>(null, null, null);
        assertNull(triple.getX());
        assertNull(triple.getY());
        assertNull(triple.getZ());
    }

    @Test
    void supportsMixedGenericTypes() {
        Triple<Double, String, Character> triple = new Triple<>(3.14, "pi", 'π');
        assertEquals(3.14, triple.getX());
        assertEquals("pi", triple.getY());
        assertEquals('π', triple.getZ());
    }

    @Test
    void shouldRemainImmutableAfterConstruction() {
        StringBuilder x = new StringBuilder("X");
        StringBuilder y = new StringBuilder("Y");
        StringBuilder z = new StringBuilder("Z");

        Triple<StringBuilder, StringBuilder, StringBuilder> triple = new Triple<>(x, y, z);

        x.append("1");
        y.append("2");
        z.append("3");

        assertEquals("X1", triple.getX().toString());
        assertEquals("Y2", triple.getY().toString());
        assertEquals("Z3", triple.getZ().toString());
    }
}
