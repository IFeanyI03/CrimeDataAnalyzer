import javax.swing.*;
import java.awt.*;
import java.util.Map;

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

        // Setup dimensions
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / (data.size() + 2);
        int maxVal = data.values().stream().max(Integer::compare).orElse(1);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.drawString(title, 20, 20);

        int x = 20;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int value = entry.getValue();
            int barHeight = (int) ((double) value / maxVal * (height - 50));

            // Draw Bar
            g.setColor(new Color(100, 150, 250)); // Light Blue
            g.fillRect(x, height - barHeight - 30, barWidth - 10, barHeight);

            // Draw Border
            g.setColor(Color.BLACK);
            g.drawRect(x, height - barHeight - 30, barWidth - 10, barHeight);

            // Draw Label and Value
            g.drawString(entry.getKey(), x, height - 10);
            g.drawString(String.valueOf(value), x + (barWidth / 4), height - barHeight - 35);

            x += barWidth;
        }
    }

    public static void showChart(Map<String, Integer> data, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new SimpleBarChart(data, title));
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}