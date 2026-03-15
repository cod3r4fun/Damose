package utility;

//documentation written with the help of chatGPT

/**
 * The {@code SysConstants} class contains global system-wide constant values 
 * used across the application. These constants are immutable and publicly 
 * accessible, promoting consistency and reducing duplication of string literals 
 * and system property accesses throughout the codebase.
 * 
 * <p>This class is typically used to reference shared configuration values, 
 * such as folder names or system paths, that are not expected to change 
 * during runtime.
 * 
 * <p><strong>Note:</strong> As a utility class, {@code SysConstants} is not 
 * intended to be instantiated.
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class SysConstants {

    /**
     * The name of the directory associated with the General Transit Feed Specification (GTFS) data.
     * <p>This constant can be used to identify the GTFS-specific folder for reading or writing
     * transit feed files.
     * 
     * <p>Example usage:
     * <pre>{@code
     * String path = SysConstants.CURRENTDIR + File.separator + SysConstants.FOLDERGTFS;
     * }</pre>
     */
    public static final String FOLDERGTFS = "GTFS";

    /**
     * The current working directory of the Java process, obtained via 
     * {@link System#getProperty(String)} using the key {@code "user.dir"}.
     * <p>This directory is typically the location from which the JVM was launched, and is 
     * useful for building relative file paths or for diagnostic/logging purposes.
     * 
     * <p>It may vary depending on the environment (IDE, command-line, container, etc.) 
     * from which the application is started.
     * 
     * <p>Example value: {@code "/Users/username/projects/myapp"}
     * 
     * <p>Example usage:
     * <pre>{@code
     * File configFile = new File(SysConstants.CURRENTDIR, "config/settings.json");
     * }</pre>
     */
    public static final String CURRENTDIR = System.getProperty("user.dir");

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>All members are static and should be accessed in a static context.
     */
    private SysConstants() {
        throw new UnsupportedOperationException("SysConstants is a utility class and should not be instantiated.");
    }
}
