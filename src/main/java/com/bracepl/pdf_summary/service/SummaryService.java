package com.bracepl.pdf_summary.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SummaryService {
    String generateSummary(MultipartFile file) throws IOException;
}
