package utility;



/**
 * A generic container for holding a triplet of objects.
 *
 * <p>This class is immutable and provides type-safe access to three values,
 * each potentially of a different type.</p>
 *
 * @param <X> the type of the first element
 * @param <Y> the type of the second element
 * @param <Z> the type of the third element
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * Triple<String, Integer, Boolean> triple = new Triple<>("Alice", 30, true);
 * String name = triple.getX();  // "Alice"
 * }</pre>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class Triple<X ,Y, Z> {
	private final X x;
	private final Y y;
	private final Z z;
	
	
    /**
     * Constructs a new {@code Triple} with the given values.
     *
     * @param x the first element
     * @param y the second element
     * @param z the third element
     */
	public Triple(X x, Y y, Z z){
		this.x = x;
		this.y = y;
		this.z = z;
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
     * Returns the third element.
     *
     * @return the value of {@code z}
     */
	public Z getZ() {
		return z;
	}
	
	
    /**
     * Returns a string representation of the triple by concatenating the string
     * values of {@code x}, {@code y}, and {@code z} in order.
     *
     * @return a string representation of this {@code Triple}
     */
	@Override
	public String toString() {
		return x.toString() + y.toString() + z.toString();
	}
}
