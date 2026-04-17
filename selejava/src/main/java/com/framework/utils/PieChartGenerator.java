package com.framework.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.constants.AppConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PieChartGenerator {

    private static final Logger log = LoggerFactory.getLogger(PieChartGenerator.class);

    private PieChartGenerator() {}

    /**
     * Generates a pie chart image from passed/failed/skipped counts.
     * Saves it as a PNG and returns the file path.
     *
     * @param passed  Number of passed tests
     * @param failed  Number of failed tests
     * @param skipped Number of skipped tests
     * @return Absolute path to generated chart image, or null on failure
     */
    public static String generate(int passed, int failed, int skipped) {
        try {
            Files.createDirectories(Paths.get(AppConstants.REPORTS_PATH));

            // Build dataset
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            if (passed  > 0) dataset.setValue("Passed  (" + passed  + ")", passed);
            if (failed  > 0) dataset.setValue("Failed  (" + failed  + ")", failed);
            if (skipped > 0) dataset.setValue("Skipped (" + skipped + ")", skipped);

            // Create chart
            JFreeChart chart = ChartFactory.createPieChart(
                "Test Execution Summary",
                dataset,
                true,   // legend
                true,   // tooltips
                false   // urls
            );

            // Style chart
            chart.setBackgroundPaint(Color.WHITE);
            chart.getTitle().setFont(new Font("Helvetica", Font.BOLD, 16));

            @SuppressWarnings("unchecked")
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelFont(new Font("Helvetica", Font.PLAIN, 12));

            // Slice colours
            plot.setSectionPaint("Passed  (" + passed  + ")", new Color(40, 167, 69));   // green
            plot.setSectionPaint("Failed  (" + failed  + ")", new Color(220, 53, 69));   // red
            plot.setSectionPaint("Skipped (" + skipped + ")", new Color(255, 193, 7));   // yellow

            // Save as PNG
            String chartPath = AppConstants.REPORTS_PATH + "summary_chart.png";
            BufferedImage image = chart.createBufferedImage(500, 350);
            ImageIO.write(image, "PNG", new File(chartPath));

            log.info("Pie chart generated: {}", chartPath);
            return chartPath;

        } catch (IOException e) {
            log.error("Failed to generate pie chart: {}", e.getMessage());
            return null;
        }
    }
}