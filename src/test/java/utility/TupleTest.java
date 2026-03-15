package utility;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TupleTest {

    @Test
    void constructorShouldSetFieldsCorrectly() {
        Tuple<String, Integer> tuple = new Tuple<>("Key", 100);
        assertEquals("Key", tuple.getX());
        assertEquals(100, tuple.getY());
    }

    @Test
    void getXShouldReturnCorrectValue() {
        Tuple<String, String> tuple = new Tuple<>("Hello", "World");
        assertEquals("Hello", tuple.getX());
    }

    @Test
    void getYShouldReturnCorrectValue() {
        Tuple<String, String> tuple = new Tuple<>("Hello", "World");
        assertEquals("World", tuple.getY());
    }

    @Test
    void toStringShouldConcatenateStringRepresentations() {
        Tuple<String, Integer> tuple = new Tuple<>("City: ", 42);
        assertEquals("City: 42", tuple.toString());
    }

    @Test
    void toStringShouldHandleComplexTypes() {
        Tuple<Object, Object> tuple = new Tuple<>(new StringBuilder("X"), new StringBuilder("Y"));
        assertEquals("XY", tuple.toString());
    }

    @Test
    void toStringShouldThrowIfXIsNull() {
        Tuple<Object, Object> tuple = new Tuple<>(null, "Test");
        assertThrows(NullPointerException.class, tuple::toString);
    }

    @Test
    void toStringShouldThrowIfYIsNull() {
        Tuple<Object, Object> tuple = new Tuple<>("Test", null);
        assertThrows(NullPointerException.class, tuple::toString);
    }

    @Test
    void toStringShouldThrowIfBothAreNull() {
        Tuple<Object, Object> tuple = new Tuple<>(null, null);
        assertThrows(NullPointerException.class, tuple::toString);
    }

    @Test
    void allowsNullValuesInTuple() {
        Tuple<String, String> tuple = new Tuple<>(null, "value");
        assertNull(tuple.getX());
        assertEquals("value", tuple.getY());

        tuple = new Tuple<>("value", null);
        assertEquals("value", tuple.getX());
        assertNull(tuple.getY());
    }

    @Test
    void allowsDifferentGenericTypes() {
        Tuple<Double, Boolean> tuple = new Tuple<>(3.14, true);
        assertEquals(3.14, tuple.getX());
        assertTrue(tuple.getY());
    }
}
