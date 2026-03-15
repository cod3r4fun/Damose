package controller;

import java.time.LocalDate;



/**
 * The {@code DateController} class manages a centralized, mutable reference date used
 * across the application to represent the current context date.
 * 
 * <p>This class provides static methods to access and manipulate the reference date,
 * including advancing the date by one day and resetting it to the system's current date.</p>
 * 
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Maintains a single mutable {@link LocalDate} reference date.</li>
 *   <li>Allows controlled date progression via a method to move the reference date forward by one day.</li>
 *   <li>Supports resetting the reference date to the system's current date.</li>
 *   <li>Prevents instantiation by using a private constructor, enforcing static usage.</li>
 * </ul>

 * 
 * <p><b>Usage example:</b></p>
 * <pre>
 *     // Get the current reference date
 *     LocalDate today = DateController.getReferenceDate();
 *     
 *     // Move the reference date one day ahead
 *     DateController.moveReferenceOneDayAhead();
 *     
 *     // Reset to the actual current date
 *     DateController.reset();
 * </pre>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class DateController {
	private static LocalDate referenceDate;
	
	static {
		DateController.reset();
	}
	
	
    /**
     * Returns the current reference date.
     * 
     * @return the reference {@link LocalDate}
     */
	public static LocalDate getReferenceDate() {
		return DateController.referenceDate;
	}
	
    /**
     * Advances the reference date by one day.
     */
	public static void moveReferenceOneDayAhead() {
		setReferenceDate(referenceDate.plusDays(1));
	}
	
	
    /**
     * Resets the reference date to the system's current date.
     */
	public static void reset() {
		setReferenceDate(LocalDate.now());
	}
	
    /**
     * Private constructor to prevent instantiation.
     */
	private DateController() {
	}
	
	
    /**
     * Helper method that updates the reference date to the provided new date.
     * 
     * @param newReferenceDate the new reference date to set
     */

	private static void setReferenceDate(LocalDate newReferenceDate) {
		referenceDate = newReferenceDate;
	}
}
