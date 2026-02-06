import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * ROLE: The "Visualizer" (Custom GUI)
 * * This is a custom Swing component that manually draws a bar chart.
 * We extend JPanel and override paintComponent() to render graphics.
 */
public class SimpleBarChart extends JPanel {
    private Map<String, Integer> data;
    private String title;

    public SimpleBarChart(Map<String, Integer> data, String title) {
        this.data = data;
        this.title = title;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        // 1. Calculate Dimensions
        int width = getWidth();
        int height = getHeight();
        // Dynamically calculate bar width based on how many items we have
        int barWidth = width / (data.size() + 2);
        // Find the maximum value to scale the bars vertically
        int maxVal = data.values().stream().max(Integer::compare).orElse(1);

        // 2. Clear background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 3. Draw Title
        g.setColor(Color.BLACK);
        g.drawString(title, 20, 20);

        // 4. Draw Bars Loop
        int x = 20;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int value = entry.getValue();

            // Calculate height relative to the window size
            int barHeight = (int) ((double) value / maxVal * (height - 50));

            // Draw Bar (Light Blue)
            g.setColor(new Color(100, 150, 250));
            g.fillRect(x, height - barHeight - 30, barWidth - 10, barHeight);

            // Draw Border (Black)
            g.setColor(Color.BLACK);
            g.drawRect(x, height - barHeight - 30, barWidth - 10, barHeight);

            // Draw Labels (Axis text and Value)
            g.drawString(entry.getKey(), x, height - 10);
            g.drawString(String.valueOf(value), x + (barWidth / 4), height - barHeight - 35);

            x += barWidth;
        }
    }

    // Helper method to create the JFrame window
    public static void showChart(Map<String, Integer> data, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new SimpleBarChart(data, title));
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
}