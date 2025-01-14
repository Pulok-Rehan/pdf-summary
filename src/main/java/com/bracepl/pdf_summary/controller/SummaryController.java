package com.bracepl.pdf_summary.controller;

import com.bracepl.pdf_summary.service.SummaryService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@Slf4j
public class SummaryController {

    private static final Logger log = LoggerFactory.getLogger(SummaryController.class);
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<String> summarizePdf(@RequestParam("file") MultipartFile file) {
        try {
            log.info("GENERATING SUMMARY...");
            String summary = summaryService.generateSummary(file);
            log.info("SUMMARY: {}", summary);
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing PDF: " + e.getMessage());
        }
    }


}

