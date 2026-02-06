import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // --- STEP 1: USER QUERY / SERP LOADER ---
        // Since we can't scrape Google directly in a student project without getting blocked,
        // we simulate the SERP result with a list of specific Wikipedia/Journal URLs.

        List<String> crimeUrls = Arrays.asList(
                "https://en.wikipedia.org/wiki/Crime_statistics",       // Fixed link
                "https://en.wikipedia.org/wiki/Police_procedural",
                "https://en.wikipedia.org/wiki/First_Information_Report",
                "https://en.wikipedia.org/wiki/Forensic_science",
                "https://en.wikipedia.org/wiki/Criminal_investigation",
                "https://en.wikipedia.org/wiki/Uniform_Crime_Reports",  // Added another valid one
                "https://en.wikipedia.org/wiki/CompStat",               // Added another valid one
                "https://en.wikipedia.org/wiki/Criminal_justice",       // Added another valid one
                "https://en.wikipedia.org/wiki/Criminology",            // Added another valid one
                "https://en.wikipedia.org/wiki/Offender_profiling"      // Added another valid one
        );

        List<String> deepLearningUrls = Arrays.asList(
                "https://en.wikipedia.org/wiki/Deep_learning",
                "https://en.wikipedia.org/wiki/Convolutional_neural_network",
                "https://en.wikipedia.org/wiki/Recurrent_neural_network",
                "https://en.wikipedia.org/wiki/Transformer_(machine_learning)"
        );

        // --- STEP 2: THREAD POOL CONFIGURATION ---
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        System.out.println("Starting Thread Pool with " + cores + " threads...");

        // --- STEP 3: EXECUTION & TEXT PROCESSING ---
        // We create lists of Future objects to hold the results once threads finish
        List<Future<PaperAnalysisTask.Result>> crimeFutures = new ArrayList<>();
        List<Future<PaperAnalysisTask.Result>> dlFutures = new ArrayList<>();

        // Submit Crime Tasks
        for (String url : crimeUrls) {
            crimeFutures.add(threadPool.submit(new PaperAnalysisTask(url, "CRIME")));
        }

        // Submit Deep Learning Tasks
        for (String url : deepLearningUrls) {
            dlFutures.add(threadPool.submit(new PaperAnalysisTask(url, "DEEP_LEARNING")));
        }

        // --- STEP 4: COUNTING & CATEGORIZATION ---
        Map<String, Integer> crimeStats = new HashMap<>();
        Map<String, Integer> dlSubHeadingStats = new HashMap<>();

        // Aggregate Crime Results
        for (Future<PaperAnalysisTask.Result> f : crimeFutures) {
            PaperAnalysisTask.Result res = f.get(); // This waits for the thread to finish
            if (res != null) {
                for (String feature : res.itemsFound) {
                    crimeStats.put(feature, crimeStats.getOrDefault(feature, 0) + 1);
                }
            }
        }

        // Aggregate Deep Learning Results
        for (Future<PaperAnalysisTask.Result> f : dlFutures) {
            PaperAnalysisTask.Result res = f.get();
            if (res != null) {
                for (String heading : res.itemsFound) {
                    // Count specific interesting headers or all of them
                    // For visualization clarity, let's group some common ones:
                    String cleanHeading = heading.length() > 15 ? heading.substring(0, 15) + "..." : heading;
                    dlSubHeadingStats.put(cleanHeading, dlSubHeadingStats.getOrDefault(cleanHeading, 0) + 1);
                }
            }
        }

        threadPool.shutdown(); // Always shut down the pool!

        // --- STEP 5: VISUALIZATION ---
        // Run GUI code on the Event Dispatch Thread
//        javax.swing.SwingUtilities.invokeLater(() -> {
//            SimpleBarChart.showChart(crimeStats, "Distinctive Features in Crime Reporting Papers");
//
//            // Note: Journal subheadings might be too many to show all, so we can filter top 5
//            SimpleBarChart.showChart(dlSubHeadingStats, "Sub-headings in Deep Learning Papers");
//        });

        javax.swing.SwingUtilities.invokeLater(() -> {

            // Graph 1: Crime Features (This one is already fine)
            SimpleBarChart.showChart(crimeStats, "Distinctive Features in Crime Papers");

            // Graph 2: Deep Learning Sub-headings (FIXED to show only Top 10)
            // We use a stream to sort by count (highest first) and pick the top 10
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