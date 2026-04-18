package com.framework.utils;

import com.framework.constants.AppConstants;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenshotPdfReport {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotPdfReport.class);
    private static final String REPORT_DIR = AppConstants.REPORTS_PATH;
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final List<ScreenshotEntry> entries = Collections.synchronizedList(
        new ArrayList<>()
    );

    private final String testName;

    // Thread-safe counters for parallel runs
    private final AtomicInteger passed  = new AtomicInteger(0);
    private final AtomicInteger failed  = new AtomicInteger(0);
    private final AtomicInteger skipped = new AtomicInteger(0);

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ScreenshotPdfReport(String testName) {
        this.testName = testName;
    }

    // -------------------------------------------------------------------------
    // Result tracking
    // -------------------------------------------------------------------------

    public void markPassed()  { passed.incrementAndGet();  }
    public void markFailed()  { failed.incrementAndGet();  }
    public void markSkipped() { skipped.incrementAndGet(); }

    // -------------------------------------------------------------------------
    // Entry builders
    // -------------------------------------------------------------------------

    /**
     * Adds a visually distinct title banner for each test case.
     */
    public void addTestCaseTitle(String title) {
        entries.add(new ScreenshotEntry(title, ScreenshotEntry.Type.TITLE));
        log.info("Test case title added: {}", title);
    }

    /**
     * Adds a text-only step — no screenshot.
     */
    public void addStep(String stepDescription) {
        entries.add(new ScreenshotEntry(stepDescription));
        log.info("Step added: {}", stepDescription);
    }

    /**
     * Adds a screenshot with a description label.
     */
    public void addScreenshot(String screenshotPath, String stepDescription) {
        if (screenshotPath != null) {
            entries.add(new ScreenshotEntry(screenshotPath, stepDescription));
            log.info("Screenshot added: [{}] {}", stepDescription, screenshotPath);
        }
    }
    /**
 * Adds a coloured PASSED or FAILED result badge as a step entry.
 * Call this at the end of tearDown after marking the result.
 *
 * @param passed true = green PASSED badge, false = red FAILED badge
 */
public void addResultBadge(boolean passed) {
    String label = passed ? "✔ PASSED" : "✘ FAILED";
    entries.add(new ScreenshotEntry(label, ScreenshotEntry.Type.RESULT));
    log.info("Result badge added: {}", label);
}

    // -------------------------------------------------------------------------
    // Generate PDF
    // -------------------------------------------------------------------------

    public String generate() {
        if (entries.isEmpty()) {
            log.warn("No entries to compile — PDF not generated");
            return null;
        }

        try {
            Files.createDirectories(Paths.get(REPORT_DIR));

            String timestamp  = LocalDateTime.now().format(FORMATTER);
            String outputPath = REPORT_DIR + testName + "_" + timestamp + ".pdf";

            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            addCoverPage(document);
            addSummaryTable(document);

            for (ScreenshotEntry entry : entries) {
                addEntryToDocument(document, entry);
            }

            document.close();
            log.info("PDF report generated: {}", outputPath);
            return outputPath;

        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF: {}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Cover page
    // -------------------------------------------------------------------------

    private void addCoverPage(Document document) throws DocumentException, IOException {
        Font titleFont  = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        document.add(new Paragraph("\n\n"));

        Paragraph title = new Paragraph("Test Execution Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        Paragraph details = new Paragraph(
            "Test Suite  : " + testName + "\n" +
            "Generated   : " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
            ) + "\n" +
            "Environment : " + ConfigReader.get("env")    + "\n" +
            "Browser     : " + ConfigReader.get("browser") + "\n" +
            "System      : " + System.getProperty("os.name") + 
                         " " + System.getProperty("os.version"),
            normalFont
        );
        details.setAlignment(Element.ALIGN_CENTER);
        document.add(details);

        document.add(new Paragraph("\n\n"));

        // Pie chart — pass int values via .get()
        String chartPath = PieChartGenerator.generate(
            passed.get(),
            failed.get(),
            skipped.get()
        );
        if (chartPath != null) {
            Image chart = Image.getInstance(chartPath);
            chart.scaleToFit(400, 300);
            chart.setAlignment(Element.ALIGN_CENTER);
            document.add(chart);
        }

    }

    // -------------------------------------------------------------------------
    // Summary table
    // -------------------------------------------------------------------------

    private void addSummaryTable(Document document) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font cellFont   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        Paragraph heading = new Paragraph("Execution Summary",
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        heading.setSpacingAfter(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f});

        // Header row
        addTableCell(table, "Total Tests", new BaseColor(52,  58,  64), headerFont);
        addTableCell(table, "Passed",      new BaseColor(40,  167, 69), headerFont);
        addTableCell(table, "Failed",      new BaseColor(220, 53,  69), headerFont);
        addTableCell(table, "Skipped",     new BaseColor(255, 193, 7),  headerFont);

        // Value row — .get() for AtomicInteger
        int total = passed.get() + failed.get() + skipped.get();
        addTableCell(table, String.valueOf(total),          BaseColor.LIGHT_GRAY, cellFont);
        addTableCell(table, String.valueOf(passed.get()),   BaseColor.LIGHT_GRAY, cellFont);
        addTableCell(table, String.valueOf(failed.get()),   BaseColor.LIGHT_GRAY, cellFont);
        addTableCell(table, String.valueOf(skipped.get()),  BaseColor.LIGHT_GRAY, cellFont);

        document.add(table);
        document.add(new Paragraph("\n"));
        document.newPage();   // ← summary ends here
    }

    private void addTableCell(PdfPTable table, String text,
                               BaseColor bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        table.addCell(cell);
    }

    // -------------------------------------------------------------------------
    // Entry renderer
    // -------------------------------------------------------------------------

    private void addEntryToDocument(Document document, ScreenshotEntry entry)
            throws DocumentException, IOException {

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   BaseColor.WHITE);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
        Font stepFont  = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        Font pathFont  = new Font(Font.FontFamily.HELVETICA,  8, Font.ITALIC, BaseColor.GRAY);

        switch (entry.type) {

            case TITLE -> {
                Paragraph title = new Paragraph("TEST: " + entry.description, titleFont);
                title.setAlignment(Element.ALIGN_LEFT);
                title.setSpacingBefore(20);
                title.setSpacingAfter(10);

                PdfPTable banner = new PdfPTable(1);
                banner.setWidthPercentage(100);

                PdfPCell cell = new PdfPCell();
                cell.setBackgroundColor(new BaseColor(33, 37, 41));
                cell.setPadding(10);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.addElement(title);
                banner.addCell(cell);

                document.add(banner);
                document.add(new Paragraph("\n"));
            }

            case TEXT -> {
                Paragraph step = new Paragraph("► " + entry.description, stepFont);
                step.setSpacingBefore(4);
                step.setSpacingAfter(4);
                document.add(step);
            }

            case SCREENSHOT -> {
                document.add(new Paragraph("Screenshot: " + entry.description, labelFont));
                document.add(new Paragraph(entry.path, pathFont));
                document.add(new Paragraph("\n"));

                Image image = Image.getInstance(entry.path);
                image.scaleToFit(
                    document.getPageSize().getWidth()  - 40,
                    document.getPageSize().getHeight() - 100
                );
                image.setAlignment(Element.ALIGN_CENTER);
                document.add(image);
                document.add(new Paragraph("\n"));  // ← each screenshot on its own page
            }
            case RESULT -> {
    boolean isPass = entry.description.contains("PASSED");

    BaseColor bgColor   = isPass
        ? new BaseColor(40,  167, 69)    // green
        : new BaseColor(220, 53,  69);   // red

    Font badgeFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);

    PdfPTable badge = new PdfPTable(1);
    badge.setWidthPercentage(30);        // ← badge width, not full line
    badge.setHorizontalAlignment(Element.ALIGN_LEFT);

    PdfPCell cell = new PdfPCell(new Phrase(entry.description, badgeFont));
    cell.setBackgroundColor(bgColor);
    cell.setPadding(6);
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    badge.addCell(cell);

    document.add(new Paragraph("\n"));
    document.add(badge);
    document.add(new Paragraph("\n"));
}
        }
    }

    // -------------------------------------------------------------------------
    // Inner class
    // -------------------------------------------------------------------------

    private static class ScreenshotEntry {

        enum Type { TITLE, SCREENSHOT, TEXT, RESULT }

        final Type   type;
        final String description;
        final String path;

        // Title entry
        ScreenshotEntry(String description, Type type) {
            this.type        = type;
            this.path        = null;
            this.description = description;
        }

        // Screenshot entry
        ScreenshotEntry(String path, String description) {
            this.type        = Type.SCREENSHOT;
            this.path        = path;
            this.description = description;
        }

        // Text-only entry
        ScreenshotEntry(String description) {
            this.type        = Type.TEXT;
            this.path        = null;
            this.description = description;
        }
    }
}