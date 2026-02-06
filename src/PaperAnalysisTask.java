import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * ROLE: The "Worker" (Thread Logic)
 * * This class implements Callable, which means it is designed to be executed
 * by a thread in the ExecutorService. It performs the heavy network lifting.
 */
public class PaperAnalysisTask implements Callable<PaperAnalysisTask.Result> {
    private String url;
    private String type; // Determines if we look for "CRIME" keywords or "DEEP_LEARNING" headers

    public PaperAnalysisTask(String url, String type) {
        this.url = url;
        this.type = type;
    }

    // A helper class to store the results of a single page analysis
    public static class Result {
        String url;
        List<String> itemsFound = new ArrayList<>();
    }

    @Override
    public Result call() {
        Result result = new Result();
        result.url = this.url;

        try {
            System.out.println("Thread-" + Thread.currentThread().getId() + " fetching: " + url);

            // 1. Fetch the HTML document using Jsoup
            // We set a User-Agent to mimic a real browser (Chrome) to avoid being blocked by Wikipedia.
            // We set a 30-second timeout to handle slow internet connections.
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(30000)
                    .get();

            String fullText = doc.body().text().toLowerCase();

            if (type.equals("CRIME")) {
                // TASK A: Feature Extraction
                // We use keyword heuristics to detect if a paper contains specific crime reporting features.
                String[] features = {"location", "time", "suspect", "victim", "weapon", "witness", "evidence", "arrest", "outcome", "police"};
                for (String feature : features) {
                    if (fullText.contains(feature)) {
                        result.itemsFound.add(feature);
                    }
                }
            } else if (type.equals("DEEP_LEARNING")) {
                // TASK B: Sub-heading Extraction
                // We parse the DOM to find <h2> and <h3> tags, which represent section headers.
                Elements headers = doc.select("h2, h3");
                for (Element header : headers) {
                    // Filter out very short text (navigation links) to keep data relevant
                    if (header.text().length() > 5) {
                        result.itemsFound.add(header.text());
                    }
                }
            }

        } catch (Exception e) {
            // If a single page fails (e.g. 404 error), we log it but do NOT crash the whole program.
            System.err.println("Error processing " + url + ": " + e.getMessage());
        }
        return result;
    }
}