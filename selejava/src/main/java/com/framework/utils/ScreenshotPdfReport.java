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
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotPdfReport {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotPdfReport.class);
    private static final String REPORT_DIR = AppConstants.REPORTS_PATH;
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Stores screenshot paths + labels for current test
    private final List<ScreenshotEntry> entries = new ArrayList<>();
    private final String testName;

    public ScreenshotPdfReport(String testName) {
        this.testName = testName;
    }

    /**
     * Adds a screenshot to the report.
     *
     * @param screenshotPath  File path returned by ScreenshotUtil.capture()
     * @param stepDescription Human-readable label shown above the screenshot in PDF
     */
    public void addScreenshot(String screenshotPath, String stepDescription) {
        if (screenshotPath != null) {
            entries.add(new ScreenshotEntry(screenshotPath, stepDescription));
            log.info("Added screenshot to report: [{}] {}", stepDescription, screenshotPath);
        }
    }

/**
 * Adds a text-only step description to the report.
 * No screenshot — just a labeled step entry in the PDF.
 *
 * @param stepDescription Human-readable step label
 */
public void addStep(String stepDescription) {
    entries.add(new ScreenshotEntry(stepDescription));  // ← add to list, not toString
    log.info("Added step to report: {}", stepDescription);
}


/**
 * Adds a visually distinct title block for each test case.
 * Call this at the start of each test before adding steps or screenshots.
 *
 * @param testName The test method name or description
 */
public void addTestCaseTitle(String testName) {
    entries.add(new ScreenshotEntry(testName, ScreenshotEntry.Type.TITLE));
    log.info("Added test case title: {}", testName);
}

    /**
     * Compiles all added screenshots into a single PDF report.
     * One screenshot per page, with step label as header.
     *
     * @return Path to generated PDF, or null on failure
     */
    public String generate() {
        if (entries.isEmpty()) {
            log.warn("No screenshots to compile for test: {}", testName);
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

            for (ScreenshotEntry entry : entries) {
                addScreenshotPage(document, entry);
            }

            document.close();
            log.info("PDF report generated: {}", outputPath);
            return outputPath;

        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF report: {}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void addCoverPage(Document document) throws DocumentException {
        Font titleFont  = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        document.add(new Paragraph("\n\n\n"));
        Paragraph title = new Paragraph("Test Execution Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        Paragraph details = new Paragraph(
            "Test: " + testName + "\n" +
            "Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
            ) + "\n" +
            "Total Screenshots: " + entries.size(),
            normalFont
        );
        details.setAlignment(Element.ALIGN_CENTER);
        document.add(details);
        document.newPage();
    }

private void addScreenshotPage(Document document, ScreenshotEntry entry)
        throws DocumentException, IOException {

    Font titleFont     = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,  BaseColor.WHITE);
    Font labelFont     = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    Font stepFont      = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
    Font pathFont      = new Font(Font.FontFamily.HELVETICA,  8, Font.ITALIC, BaseColor.GRAY);

    switch (entry.type) {

        case TITLE -> {
            // Full-width dark banner with test name
            Paragraph title = new Paragraph("TEST: " + entry.description, titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingBefore(20);
            title.setSpacingAfter(10);

            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(new BaseColor(33, 37, 41));   // dark grey
            cell.setPadding(10);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(title);
            banner.addCell(cell);

            document.add(banner);
            document.add(new Paragraph("\n"));
        }

        case TEXT -> {
            Paragraph step = new Paragraph("► " + entry.description, stepFont);
            step.setSpacingBefore(6);
            step.setSpacingAfter(6);
            document.add(step);
        }

        case SCREENSHOT -> {
            document.add(new Paragraph(entry.description, labelFont));
            document.add(new Paragraph(entry.path, pathFont));
            document.add(new Paragraph("\n"));

            Image image = Image.getInstance(entry.path);
            image.scaleToFit(
                document.getPageSize().getWidth()  - 40,
                document.getPageSize().getHeight() - 100
            );
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);
            document.newPage();
        }
    }
}
    // -------------------------------------------------------------------------
    // Inner record
    // -------------------------------------------------------------------------
private static class ScreenshotEntry {

    enum Type { TITLE, SCREENSHOT, TEXT }

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