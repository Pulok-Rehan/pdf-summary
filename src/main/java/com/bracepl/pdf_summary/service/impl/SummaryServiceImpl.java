package com.bracepl.pdf_summary.service.impl;

import com.bracepl.pdf_summary.entity.Summary;
import com.bracepl.pdf_summary.repository.SummaryRepository;
import com.bracepl.pdf_summary.service.SummaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class SummaryServiceImpl implements SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryServiceImpl.class);
    @Value("${openai.api.key}")
    private String openaiApiKey;
    private final SummaryRepository summaryRepository;
    private final WebClient webClient;

    public SummaryServiceImpl(SummaryRepository summaryRepository, WebClient webClient) {
        this.summaryRepository = summaryRepository;
        this.webClient = webClient;
    }

    @Override
    public String generateSummary(MultipartFile file) throws IOException {
        log.info("EXTRACTING TEXT FROM PDF...");
        String extractedText = this.extractTextFromPdf(file);
        String summary = this.getSummary(extractedText);

        String filePath = "path/to/store/" + file.getOriginalFilename();
        file.transferTo(new File(filePath));
////
//        Summary newSummary = Summary.builder()
//                .summary(summary)
//                .filePath(filePath)
//                .build();
//        summaryRepository.save(newSummary);

        return summary;

    }
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            log.info("TEXT EXTRACTED SUCCESSFULLY.");
            return stripper.getText(document);
        }
    }
    private String getSummary(String text) {
        log.info("CALLING CHAT GPT 4 API...");
        return callChatGptApi(text);
    }

    private String callChatGptApi(String text) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        log.info("API URL: {}", apiUrl);

        String requestPayload = """
            {
              "model": "gpt-4",
              "messages": [
                {"role": "user", "content": "Summarize the following text: %s"}
              ]
            }
            """.formatted(text);

        try {
            return webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        String summary = extractSummaryFromResponse(response);
                        return summary;
                    })
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in summarization: " + e.getMessage();
        }
    }

    private String extractSummaryFromResponse(String response) {
        try {
            log.info("API RESPONSE : {}", response);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to extract summary";
        }
    }
}
