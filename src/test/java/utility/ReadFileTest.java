
package utility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ReadFileTest {

    private List<Path> tempFiles = new ArrayList<>();

    @AfterEach
    void cleanup() throws IOException {
        for (Path file : tempFiles) {
            Files.deleteIfExists(file);
        }
        tempFiles.clear();
    }

    /**
     * Helper to create a temp file with given content lines joined by \n
     */
    private Path createTempFile(String... lines) throws IOException {
        Path tempFile = Files.createTempFile("readfiletest-", ".txt");
        Files.write(tempFile, Arrays.asList(lines));
        tempFiles.add(tempFile);
        return tempFile;
    }

    @Test
    void testReadAndDoWithFirstLineMapper_basicCorrectness() throws IOException {
        // Prepare file with header and data
        Path file = createTempFile(
            "ID,Name,Age",
            "1,Alice,30",
            "2,Bob,25",
            "3,Charlie,40"
        );

        // Use a list to capture processed lines and indices passed
        List<String> processedLines = new ArrayList<>();
        List<int[]> capturedIndices = new ArrayList<>();

        ReadFile.readAndDoWithFirstLineMapper(file.toString(), (line, indices) -> {
            processedLines.add(line);
            // clone to avoid later mutation problems
            capturedIndices.add(Arrays.copyOf(indices, indices.length));
        }, ",", new String[]{"ID", "Name"});

        // Should process all data lines (3)
        assertEquals(3, processedLines.size());
        assertEquals("1,Alice,30", processedLines.get(0));
        assertEquals("2,Bob,25", processedLines.get(1));
        assertEquals("3,Charlie,40", processedLines.get(2));

        // Check indices correspond to header positions
        int[] expectedIndices = {0, 1};
        for (int[] idxs : capturedIndices) {
            assertArrayEquals(expectedIndices, idxs);
        }
    }

    @Test
    void testReadAndDoWithFirstLineMapper_missingHeader_throwsRuntimeException() throws IOException {
        Path file = createTempFile(
            "ID,Name,Age",
            "1,Alice,30"
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapper(file.toString(), (line, indices) -> {
                // no-op
            }, ",", new String[]{"ID", "NonExistingHeader"});
        });
        assertTrue(exception.getMessage().contains("Missing expected header"));
    }

    @Test
    void testReadAndDoWithFirstLineMapper_emptyFile() throws IOException {
        Path file = createTempFile(); // empty file

        // It should throw NullPointerException because first line is null and split is called
        // Your code does not explicitly handle empty files for readAndDoWithFirstLineMapper (only parallel)
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapper(file.toString(), (line, indices) -> {}, ",", new String[]{"ID"});
        });

        // Alternative: you could modify ReadFile to check header == null and throw better exception
    }

    @Test
    void testReadAndDoWithFirstLineMapper_operationReceivesCorrectIndicesAndFields() throws IOException {
        Path file = createTempFile(
            "A,B,C",
            "v1,v2,v3",
            "x1,x2,x3"
        );

        List<String> results = new ArrayList<>();

        ReadFile.readAndDoWithFirstLineMapper(file.toString(), (line, indices) -> {
            String[] parts = line.split(",");
            String combined = parts[indices[0]] + "-" + parts[indices[1]];
            results.add(combined);
        }, ",", new String[]{"A", "B"});

        assertEquals(List.of("v1-v2", "x1-x2"), results);
    }

    @Test
    void testReadAndDoWithFirstLineMapper_parallel_basic() throws Exception {
        Path file = createTempFile(
            "ID,Val",
            "1,A",
            "2,B",
            "3,C",
            "4,D"
        );

        // Use thread-safe collection to capture processed lines
        Set<String> processed = Collections.synchronizedSet(new HashSet<>());

        ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {
            String[] parts = line.split(",");
            processed.add(parts[indices[0]] + ":" + parts[indices[1]]);
        }, ",", new String[]{"ID", "Val"});

        // All lines processed, order can vary
        Set<String> expected = Set.of("1:A", "2:B", "3:C", "4:D");
        assertEquals(expected, processed);
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_missingHeader() throws IOException, InterruptedException {
        Path file = createTempFile(
            "A,B,C",
            "1,2,3"
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {}, ",", new String[]{"A", "Z"});
        });
        assertTrue(exception.getMessage().contains("Missing expected header"));
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_emptyFile() throws IOException, InterruptedException {
        Path file = createTempFile();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {}, ",", new String[]{"A"});
        });
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_operationThrows_exceptionIsWrapped() throws IOException, InterruptedException {
        Path file = createTempFile(
            "ID,Val",
            "1,A",
            "2,B"
        );

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {
                if (line.startsWith("2")) {
                    throw new IllegalStateException("Test exception");
                }
            }, ",", new String[]{"ID", "Val"});
        });

        assertTrue(thrown.getMessage().contains("Error during parallel execution"));
        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertEquals("Test exception", thrown.getCause().getMessage());
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_largeBatchSplitting() throws Exception {
        // Create file with 10,001 data lines + header to force two batches
        int linesCount = 10_001;
        List<String> lines = new ArrayList<>(linesCount + 1);
        lines.add("ID,Val");
        for (int i = 1; i <= linesCount; i++) {
            lines.add(i + "," + (char)('A' + (i % 26)));
        }

        Path file = createTempFile(lines.toArray(new String[0]));

        AtomicInteger processedCount = new AtomicInteger(0);

        ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {
            processedCount.incrementAndGet();
        }, ",", new String[]{"ID", "Val"});

        assertEquals(linesCount, processedCount.get());
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_delimiterTab() throws Exception {
        Path file = createTempFile(
            "ID\tVal",
            "1\tA",
            "2\tB"
        );

        Set<String> processed = Collections.synchronizedSet(new HashSet<>());

        ReadFile.readAndDoWithFirstLineMapperParallel(file.toString(), (line, indices) -> {
            String[] parts = line.split("\t");
            processed.add(parts[indices[0]] + ":" + parts[indices[1]]);
        }, "\t", new String[]{"ID", "Val"});

        assertEquals(Set.of("1:A", "2:B"), processed);
    }

    @Test
    void testReadAndDoWithFirstLineMapper_handlesIOExceptionGracefully() throws IOException {
        // Provide a file path that does not exist - should print stacktrace but not throw (as per current impl)
        String nonExistentPath = "/non/existing/file.csv";

        // The method does not throw IOException but catches and prints stacktrace
        // So we just check it completes without throwing
        assertDoesNotThrow(() -> {
            ReadFile.readAndDoWithFirstLineMapper(nonExistentPath, (line, indices) -> {}, ",", new String[]{"ID"});
        });
    }

    @Test
    void testReadAndDoWithFirstLineMapperParallel_handlesIOException() {
        String nonExistentPath = "/non/existing/file.csv";

        assertThrows(IOException.class, () -> {
            ReadFile.readAndDoWithFirstLineMapperParallel(nonExistentPath, (line, indices) -> {}, ",", new String[]{"ID"});
        });
    }
}

