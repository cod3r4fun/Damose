package utility;

//documentation written with the help of chatGPT

/**
 * A functional interface representing an operation to be performed on a single line of text
 * from a file, along with the mapped positions of selected fields based on a header row.
 * 
 * <p>This interface is designed to be used with methods that read delimited text files
 * (such as CSV, TSV, or custom formats) and extract specific columns from each line,
 * identified by their header names. The {@code operation} method provides the raw line 
 * and the array of indices indicating which fields to extract and process.
 * 
 * <p>This interface supports usage with lambda expressions or method references in modern
 * Java (8+).
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * OperationOnLineMapped processor = (line, positions) -> {
 *     String[] parts = line.split(",");
 *     String id = parts[positions[0]];
 *     String name = parts[positions[1]];
 *     // Perform some operation on id and name
 * };
 * }</pre>
 *
 * <p>Used primarily by the {@link ReadFile} class methods for line-by-line processing.
 * 
 * @see ReadFile#readAndDoWithFirstLineMapper(String, OperationOnLineMapped, String, String[])
 * @see ReadFile#readAndDoWithFirstLineMapperParallel(String, OperationOnLineMapped, String, String[])
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface OperationOnLineMapped {

    /**
     * Applies a user-defined operation to a single line of text from a file,
     * given the mapped positions of the desired fields.
     *
     * @param line          the full raw line from the file (excluding line break characters)
     * @param filtered_pos  an array of zero-based indices representing the positions of the headers 
     *                      requested by the caller (based on the header row of the file)
     * 
     * <p><strong>Usage pattern:</strong> The implementation typically splits the line using the
     * appropriate delimiter and accesses fields using {@code filtered_pos[i]} to extract specific columns.
     */
    void operation(String line, int[] filtered_pos);
}
