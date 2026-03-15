package utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

//documentation written with the help of chatGPT


/**
 * The {@code ReadFile} class provides utility methods for reading delimited text files
 * (such as CSV or TSV) and processing their lines using custom mapping logic.
 * 
 * <p>This class supports both single-threaded and parallel processing of file content,
 * where the first line is assumed to be a header row that maps column names to expected positions.
 * 
 * <p>The methods require a user-defined operation handler implementing {@link OperationOnLineMapped}
 * to apply logic to each line after mapping fields based on headers.
 *
 * <p>Designed for use in data-processing pipelines, ETL tasks, or log analysis systems where
 * header-based field mapping is crucial and performance can be scaled via parallelism.
 * 
 * <p><strong>Usage note:</strong> Files should be well-formed with consistent delimiters and a valid header row.
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 */
public class ReadFile {

    /**
     * Reads a delimited text file and applies an operation to each data line based on
     * column positions inferred from the header row.
     *
     * @param path           the full path to the input file to be read
     * @param op             a functional interface defining the operation to apply to each line;
     *                       receives the raw line and an array of indices corresponding to requested headers
     * @param separator      the delimiter used in the file (e.g., comma for CSV, tab for TSV)
     * @param headersInOrder an array of expected headers, which will determine which fields are extracted from each line
     * 
     * @throws RuntimeException if any expected header is not found in the file's actual header row
     * 
     * <p><strong>Example usage:</strong>
     * <pre>{@code
     * ReadFile.readAndDoWithFirstLineMapper("data.csv", (line, indices) -> {
     *     String[] fields = line.split(",");
     *     String id = fields[indices[0]];
     *     String name = fields[indices[1]];
     *     // Do something with id and name
     * }, ",", new String[]{"ID", "Name"});
     * }</pre>
     */
    public static void readAndDoWithFirstLineMapper(String path, OperationOnLineMapped op,
    		String separator, String[] headersInOrder) {
    	
    	
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            line = br.readLine(); 
            String[] headers = line.split(separator);

            int[] filtered_pos = new int[headersInOrder.length];
            for (int i = 0; i < headersInOrder.length; i++) {
                int pos = -1;
                for (int j = 0; j < headers.length; j++) {
                    if (headersInOrder[i].equals(headers[j].transform((string) -> string.replaceAll("\"", "")))) {
                        pos = j;
                        break;
                    }
                }
                if (pos == -1) {
                	System.out.println(headersInOrder[i]);
                    throw new RuntimeException("Missing expected header: " + headersInOrder[i]);
                }
                filtered_pos[i] = pos;
            }

            while ((line = br.readLine()) != null) {
                op.operation(line, filtered_pos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a delimited text file and processes its content in parallel batches, using a thread pool
     * to speed up operations on large files. The file's first line is treated as a header.
     *
     * @param path           the path to the input file
     * @param op             the operation to apply to each line, receiving the raw line and indices of relevant columns
     * @param separator      the delimiter used to split each line
     * @param headersInOrder the array of expected headers to extract from each line
     * @throws InterruptedException if the thread is interrupted while waiting for batch tasks to complete
     * @throws IOException if the file cannot be opened or read
     * @throws RuntimeException if a header is missing or a parallel task encounters an execution error
     * 
     * <p>This method uses a fixed-size thread pool based on the number of available processors and
     * breaks the file into batches (default 10,000 lines). Each batch is submitted as a separate
     * task for execution.
     * 
     * <p><strong>Performance note:</strong> Ideal for multi-core systems and large datasets.
     * Not recommended for very small files due to thread overhead.
     * 
     * <p><strong>Example usage:</strong>
     * <pre>{@code
     * ReadFile.readAndDoWithFirstLineMapperParallel("data.csv", (line, indices) -> {
     *     String[] parts = line.split(",");
     *     String value = parts[indices[0]];
     *     // Process the value
     * }, ",", new String[]{"Value"});
     * }</pre>
     */
    public static void readAndDoWithFirstLineMapperParallel(
            String path,
            OperationOnLineMapped op,
            String separator,
            String[] headersInOrder) throws InterruptedException, IOException {

        final int batchSize = 10_000;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(availableProcessors);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // read header
            if (line == null) {
                throw new RuntimeException("File is empty");
            }

            String[] headers = line.split(separator);
            int[] filtered_pos = new int[headersInOrder.length];
            for (int i = 0; i < headersInOrder.length; i++) {
                int pos = -1;
                for (int j = 0; j < headers.length; j++) {
                    if (headersInOrder[i].equals(headers[j])) {
                        pos = j;
                        break;
                    }
                }
                if (pos == -1) {
                    throw new RuntimeException("Missing expected header: " + headersInOrder[i]);
                }
                filtered_pos[i] = pos;
            }

            List<String> batch = new ArrayList<>(batchSize);
            List<Future<?>> futures = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                batch.add(line);
                if (batch.size() == batchSize) {
                    List<String> batchCopy = new ArrayList<>(batch);
                    futures.add(executor.submit(() -> {
                        for (String l : batchCopy) {
                            op.operation(l, filtered_pos);
                        }
                    }));
                    batch.clear();
                }
            }

            // Process the final batch
            if (!batch.isEmpty()) {
                List<String> batchCopy = new ArrayList<>(batch);
                futures.add(executor.submit(() -> {
                    for (String l : batchCopy) {
                        op.operation(l, filtered_pos);
                    }
                }));
            }

            // Await completion of all submitted tasks
            for (Future<?> future : futures) {
                future.get();
            }

        } catch (ExecutionException e) {
            throw new RuntimeException("Error during parallel execution", e.getCause());
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }
    }
}

