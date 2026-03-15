package model.vehicles;

import java.time.LocalDate;

//documentation written with the help of chatGPT


/**
 * Represents an exception date for a transit service, as defined in the GTFS {@code calendar_dates.txt} file.
 * 
 * <p>This class models modifications to the regular service calendar, indicating dates when service is added or removed.
 * It captures:
 * <ul>
 *   <li>{@code service_id} – Identifier linking to a particular service schedule</li>
 *   <li>{@code date} – The specific date of the exception</li>
 *   <li>{@code exception_type} – Indicator of whether the service is added or removed on this date</li>
 * </ul>
 * 
 * <p>The {@code exception_type} usually uses the following GTFS standard codes:
 * <ul>
 *   <li>{@code 1} – Service added for the specified date</li>
 *   <li>{@code 2} – Service removed for the specified date</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * CalendarDates exception = new CalendarDates("weekday_service", LocalDate.of(2024, 12, 25), "2");
 * if ("2".equals(exception.getException_type())) {
 *     System.out.println("Service is removed on " + exception.getDate());
 * }
 * }</pre>
 * 
 * @see <a href="https://gtfs.org/schedule/reference/#calendardatestxt">GTFS Reference: calendar_dates.txt</a>
 * 
 * @author	Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class CalendarDates {

    /** Identifier for the service this exception applies to. */
    public final String service_id;

    /** Date for which the service exception applies. */
    public final LocalDate date;

    /** Exception type: '1' for added service, '2' for removed service. */
    public final String exception_type;

    /**
     * Constructs a new {@code CalendarDates} object with the specified service ID, date, and exception type.
     *
     * @param service_id      the service identifier linked to this exception
     * @param date            the date the exception applies to
     * @param exception_type  the exception type, typically "1" (added) or "2" (removed)
     */
    public CalendarDates(String service_id, LocalDate date, String exception_type) {
    	
    	if (service_id == null) throw new IllegalArgumentException("service_id cannot be null");
    	if(date == null) throw new IllegalArgumentException("date cannot be null");
    	if(exception_type == null) throw new IllegalArgumentException("exception_type cannot be null");
        this.service_id = service_id;
        this.date = date;
        this.exception_type = exception_type;
    }

    /**
     * @return the service identifier for this exception
     */
    public String getService_id() {
        return service_id;
    }

    /**
     * @return the date the service exception applies to
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the type of exception, typically "1" (service added) or "2" (service removed)
     */
    public String getException_type() {
        return exception_type;
    }
}
