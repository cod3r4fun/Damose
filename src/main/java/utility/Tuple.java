package utility;


/**
 * A generic immutable container for holding a pair of objects.
 *
 * <p>This class is intended for lightweight structures where pairing of two 
 * related objects is required, such as map entries, coordinate pairs, or
 * composite return types.</p>
 *
 * @param <X> the type of the first element
 * @param <Y> the type of the second element
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Tuple<String, Integer> pair = new Tuple<>("City", 42);
 * String name = pair.getX();     // "City"
 * Integer value = pair.getY();   // 42
 * }</pre>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Tuple<X, Y> {
		private final X x;
		private final Y y;
		
		
	    /**
	     * Constructs a {@code Tuple} with the given values.
	     *
	     * @param x the first element
	     * @param y the second element
	     */
		public Tuple(X x, Y y){
			this.x = x;
			this.y = y;
		}
		
		
	    /**
	     * Returns the first element.
	     *
	     * @return the value of {@code x}
	     */

		public X getX() {
			return x;
		}
		
		
	    /**
	     * Returns the second element.
	     *
	     * @return the value of {@code y}
	     */
		public Y getY() {
			return y;
		}

	    /**
	     * Returns a string representation of the tuple by concatenating the
	     * string values of {@code x} and {@code y} in order.
	     * <p><strong>Note:</strong> This implementation assumes {@code x} and {@code y}
	     * are non-null. If nulls are possible, consider overriding this method safely.</p>
	     *
	     * @return a string representation of this {@code Tuple}
	     */
		@Override
		public String toString() {
			return x.toString() + y.toString();
	}

}
