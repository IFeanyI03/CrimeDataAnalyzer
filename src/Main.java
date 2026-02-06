import java.util.*;
import java.util.concurrent.*;

/**
 * PROJECT: Crime Reporting & Deep Learning Paper Analyzer
 *
 * This class handles the high-level architecture:
 * 1. Simulates the Search Engine Results (SERP) by defining URLs.
 * 2. Manages the Thread Pool to fetch pages in parallel (Multithreading).
 * 3. Aggregates results from all threads.
 * 4. Triggers the Visualization (GUI).
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        // =============================================================
        // STEP 1: USER QUERY / SERP LOADER
        // =============================================================
        // NOTE: We simulate Google SERP results here using a fixed list of URLs.
        // This is done to avoid IP bans and CAPTCHA blocks associated with
        // scraping live search engines during a student demonstration.

        List<String> crimeUrls = Arrays.asList(
                "https://en.wikipedia.org/wiki/Crime_statistics",
                "https://en.wikipedia.org/wiki/Police_procedural",
                "https://en.wikipedia.org/wiki/First_Information_Report",
                "https://en.wikipedia.org/wiki/Forensic_science",
                "https://en.wikipedia.org/wiki/Criminal_investigation",
                "https://en.wikipedia.org/wiki/Uniform_Crime_Reports",
                "https://en.wikipedia.org/wiki/CompStat",
                "https://en.wikipedia.org/wiki/Criminal_justice",
                "https://en.wikipedia.org/wiki/Criminology",
                "https://en.wikipedia.org/wiki/Offender_profiling"
        );

        List<String> deepLearningUrls = Arrays.asList(
                "https://en.wikipedia.org/wiki/Deep_learning",
                "https://en.wikipedia.org/wiki/Convolutional_neural_network",
                "https://en.wikipedia.org/wiki/Recurrent_neural_network",
                "https://en.wikipedia.org/wiki/Transformer_(machine_learning)"
        );

        // =============================================================
        // STEP 2: THREAD POOL CONFIGURATION (Concurrency)
        // =============================================================
        // We calculate the number of available CPU cores to optimize performance.
        int cores = Runtime.getRuntime().availableProcessors();

        // ExecutorService is used to manage a pool of threads.
        // This allows us to fetch multiple websites simultaneously.
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        System.out.println("Starting Thread Pool with " + cores + " threads...");

        // =============================================================
        // STEP 3: EXECUTION & TEXT PROCESSING
        // =============================================================
        // 'Future' objects are placeholders for data that isn't ready yet.
        // They allow the main thread to continue working while the network threads fetch data.
        List<Future<PaperAnalysisTask.Result>> crimeFutures = new ArrayList<>();
        List<Future<PaperAnalysisTask.Result>> dlFutures = new ArrayList<>();

        // Submit tasks to the thread pool
        for (String url : crimeUrls) {
            crimeFutures.add(threadPool.submit(new PaperAnalysisTask(url, "CRIME")));
        }

        for (String url : deepLearningUrls) {
            dlFutures.add(threadPool.submit(new PaperAnalysisTask(url, "DEEP_LEARNING")));
        }

        // =============================================================
        // STEP 4: DATA AGGREGATION
        // =============================================================
        Map<String, Integer> crimeStats = new HashMap<>();
        Map<String, Integer> dlSubHeadingStats = new HashMap<>();

        // Collect results for Crime Papers
        for (Future<PaperAnalysisTask.Result> f : crimeFutures) {
            // f.get() blocks the main thread until the specific download is complete
            PaperAnalysisTask.Result res = f.get();
            if (res != null) {
                for (String feature : res.itemsFound) {
                    crimeStats.put(feature, crimeStats.getOrDefault(feature, 0) + 1);
                }
            }
        }

        // Collect results for Deep Learning Papers
        for (Future<PaperAnalysisTask.Result> f : dlFutures) {
            PaperAnalysisTask.Result res = f.get();
            if (res != null) {
                for (String heading : res.itemsFound) {
                    // Clean up: Truncate very long headers so the chart looks neat
                    String cleanHeading = heading.length() > 15 ? heading.substring(0, 15) + "..." : heading;
                    dlSubHeadingStats.put(cleanHeading, dlSubHeadingStats.getOrDefault(cleanHeading, 0) + 1);
                }
            }
        }

        // Important: Always shut down the thread pool or the program will never exit!
        threadPool.shutdown();

        // =============================================================
        // STEP 5: VISUALIZATION
        // =============================================================
        // We run the GUI updates on the Event Dispatch Thread to ensure thread safety.
        javax.swing.SwingUtilities.invokeLater(() -> {

            // Graph 1: Crime Features
            SimpleBarChart.showChart(crimeStats, "Distinctive Features in Crime Papers");

            // Graph 2: Deep Learning Sub-headings (Top 10 Filter)
            // We use a Stream to sort the data by frequency (highest to lowest) and pick the top 10.
            Map<String, Integer> top10Headings = new LinkedHashMap<>();

            dlSubHeadingStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Sort high to low
                    .limit(10) // Keep only the top 10
                    .forEach(entry -> top10Headings.put(entry.getKey(), entry.getValue()));

            SimpleBarChart.showChart(top10Headings, "Top 10 Sub-headings in Deep Learning Papers");
        });

        System.out.println("Analysis Complete. Charts displayed.");
    }
}