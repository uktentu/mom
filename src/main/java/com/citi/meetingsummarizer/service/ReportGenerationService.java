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
        // Create reports directory if not exists
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

            // Meeting Details
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
            XWPFRun summaryRun = summaryParagraph.createRun();
            summaryRun.setText(summarizedText);

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

            // Write the document
            document.write(out);
        }

        return targetLocation.toString();
    }
}