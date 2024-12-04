package com.citi.meetingsummarizer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class SummarizationService {

    private final WebClient webClient;

    @Value("${ml.summarization.endpoint}")
    private String summarizationEndpoint;

    public SummarizationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> generateSummary(String transcriptPath) {
        try {
            // Read transcript content
            String transcriptContent = Files.readString(Paths.get(transcriptPath));

            // Prepare request payload
            Map<String, String> payload = new HashMap<>();
            payload.put("transcript", transcriptContent);
//            payload.put("depth","detailed");

            // Send request to ML summarization service
            return webClient.post()
                    .uri(summarizationEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Error reading transcript file", e));
        }
    }
}