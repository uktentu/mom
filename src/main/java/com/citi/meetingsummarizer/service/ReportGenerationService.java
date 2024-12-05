package com.citi.meetingsummarizer.service;

import com.citi.meetingsummarizer.model.Meeting;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReportGenerationService {

    @Value("${app.report.dir:/reports}")
    private String reportDir;

    public String generateEditableReport(Meeting meeting, String summarizedText) throws IOException {
        // Create reports directory if it doesn't exist
        Path reportPath = Paths.get(reportDir).toAbsolutePath().normalize();
        Files.createDirectories(reportPath);

        // Generate unique filename
        String filename = "Meeting_Summary_" + UUID.randomUUID() + ".docx";
        Path targetLocation = reportPath.resolve(filename);

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(targetLocation.toFile())) {

            // Citi Branding Header
            XWPFParagraph headerParagraph = document.createParagraph();
            headerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headerRun = headerParagraph.createRun();
            headerRun.setBold(true);
            headerRun.setFontSize(16);
            headerRun.setText("Citi Meeting Summary");

            // Meeting Details Section
            XWPFParagraph detailsParagraph = document.createParagraph();
            XWPFRun detailsRun = detailsParagraph.createRun();
            detailsRun.setText("Meeting Title: " + meeting.getTitle());
            detailsRun.addBreak();
            detailsRun.setText("Date: " + meeting.getMeetingDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            detailsRun.addBreak();
            detailsRun.setText("Duration: " + meeting.getDurationMinutes() + " minutes");

            // Editable Summary Section
            XWPFParagraph summaryTitleParagraph = document.createParagraph();
            XWPFRun summaryTitleRun = summaryTitleParagraph.createRun();
            summaryTitleRun.setBold(true);
            summaryTitleRun.setText("Meeting Summary");

            XWPFParagraph summaryParagraph = document.createParagraph();
            summaryParagraph.setSpacingBefore(200); // Add spacing for better readability
            XWPFRun summaryRun = summaryParagraph.createRun();

            // Extract and format the summarized text
            String formattedSummary = formatSummaryText(summarizedText);
            summaryRun.setText(formattedSummary);

            // Editable Notes Section
            XWPFParagraph notesTitleParagraph = document.createParagraph();
            XWPFRun notesTitleRun = notesTitleParagraph.createRun();
            notesTitleRun.setBold(true);
            notesTitleRun.setText("Additional Notes");

            XWPFParagraph notesPlaceholderParagraph = document.createParagraph();
            XWPFRun notesPlaceholderRun = notesPlaceholderParagraph.createRun();
            notesPlaceholderRun.setItalic(true);
            notesPlaceholderRun.setColor("CCCCCC");
            notesPlaceholderRun.setText("Click here to add your notes...");

            // Write the document to file
            document.write(out);
        }

        return targetLocation.toString();
    }

    /**
     * Formats the summarized text into a clean bullet-point structure.
     *
     * @param summarizedText The raw summarized text (may include JSON-like syntax or mixed delimiters).
     * @return A formatted summary as bullet points.
     */
    private String formatSummaryText(String summarizedText) {
        // Check if the input is in JSON format and contains a "summary" key
        if (summarizedText.startsWith("{") && summarizedText.contains("\"summary\":")) {
            int startIndex = summarizedText.indexOf("\"summary\":") + 10; // Start after "summary":
            int endIndex = summarizedText.lastIndexOf("}");
            if (startIndex < endIndex) {
                summarizedText = summarizedText.substring(startIndex, endIndex).trim();
            }
        }

        // Clean up JSON-like characters and whitespace
        summarizedText = summarizedText.replaceAll("[{}\"']", "").trim();

        // Replace literal \n characters with actual newlines for formatting
        summarizedText = summarizedText.replace("\\n", "\n");

        // Split the summarized text into individual lines based on common delimiters (newline, hyphen)
        String[] summaryLines = summarizedText.split("\\n|-\\s");

        // Build the bullet-point formatted summary
        StringBuilder formattedSummary = new StringBuilder();
        for (String line : summaryLines) {
            if (!line.isBlank()) { // Ignore blank lines
                formattedSummary.append("• ").append(line.trim()).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }

        return formattedSummary.toString().trim();
    }


}
