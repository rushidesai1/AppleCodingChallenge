package code.benchmark;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static code.spliterator.FixedBatchSpliterator.withBatchSize;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Rushi Desai on 11/10/2017
 * <p>
 * Micro-benchmark to compare nio vs FixedBatchSizeSpliterator impl
 */
public class FixedBatchSpliteratorBenchmark {

    public static void main(String[] args) throws IOException {
        final Path inputPath = createInput();
        for (int i = 1; i <= 5; i++) {
            System.out.println("Start processing JDK stream");
            measureProcessing(Files.lines(inputPath));
            int batchSize = (i + 3) * 2;
            System.out.println("Start processing fixed-batch stream - Batch Size : " + batchSize);
            measureProcessing(withBatchSize(Files.lines(inputPath), batchSize));
        }
    }

    /**
     * Get statistics regarding processing a file. Since Streams are lazy we can wrap it with any code we want.
     * In this case we are wrapping it some code that can test statistics.
     *
     * @param input
     * @throws IOException
     */
    static void measureProcessing(Stream<String> input) throws IOException {
        final long startTime = System.nanoTime();
        try (Stream<String> lines = input) {
            final long totalTime = lines.parallel()
                    .mapToLong(FixedBatchSpliteratorBenchmark::processLine).sum();
            final double cpuTime = totalTime, realTime = System.nanoTime() - startTime;
            final int virtualCores = Runtime.getRuntime().availableProcessors();
            System.out.println("          Cores: " + virtualCores);
            System.out.format("       CPU time: %.2f s\n", cpuTime / SECONDS.toNanos(1));
            System.out.format("      Real time: %.2f s\n", realTime / SECONDS.toNanos(1));
            System.out.format("CPU utilization: %.2f%%\n\n", 100.0 * cpuTime / realTime / virtualCores);
        }
    }

    /**
     * Just simulate some work
     *
     * @param line
     * @return
     */
    static long processLine(String line) {
        final long localStart = System.nanoTime();
        double d = 0;
        for (int i = 0; i < line.length(); i++)
            for (int j = 0; j < line.length(); j++)
                d += Math.pow(line.charAt(i), line.charAt(j) / 32.0);
        return System.nanoTime() - localStart;
    }

    /**
     * Create a random file to test
     *
     * @return
     * @throws IOException
     */
    static Path createInput() throws IOException {
        final Path inputPath = Paths.get("input.txt");
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(inputPath))) {
            for (int i = 0; i < 6_000; i++) {
                final String textToPutInFile = String.valueOf(System.nanoTime());
                for (int j = 0; j < 15; j++) w.print(textToPutInFile);
                w.println();
            }
        }
        return inputPath;
    }
}