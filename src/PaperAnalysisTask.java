import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// This "Task" will be submitted to the Thread Pool
public class PaperAnalysisTask implements Callable<PaperAnalysisTask.Result> {
    private String url;
    private String type; // "CRIME" or "DEEP_LEARNING"

    public PaperAnalysisTask(String url, String type) {
        this.url = url;
        this.type = type;
    }

    // This is the object we return after the thread finishes
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
            // 1. Fetch the HTML document
//            Document doc = Jsoup.connect(url).timeout(5000).get();

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Pretend to be a real browser
                    .timeout(30000) // Increase wait time to 30 seconds
                    .get();

            String fullText = doc.body().text().toLowerCase();

            if (type.equals("CRIME")) {
                // FEATURE EXTRACTION: Check if keywords exist in the text
                String[] features = {"location", "time", "suspect", "victim", "weapon", "witness", "evidence", "arrest", "outcome", "police"};
                for (String feature : features) {
                    if (fullText.contains(feature)) {
                        result.itemsFound.add(feature);
                    }
                }
            } else if (type.equals("DEEP_LEARNING")) {
                // SUB-HEADING EXTRACTION: Get text inside <h2> and <h3> tags
                Elements headers = doc.select("h2, h3");
                for (Element header : headers) {
                    // Filter out short/navigation headers to keep it relevant
                    if (header.text().length() > 5) {
                        result.itemsFound.add(header.text());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error processing " + url + ": " + e.getMessage());
        }
        return result;
    }
}