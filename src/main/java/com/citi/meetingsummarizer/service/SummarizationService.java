package com.citi.meetingsummarizer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SummarizationService {
    private static final Logger logger = LoggerFactory.getLogger(SummarizationService.class);

    private final WebClient webClient;

    @Value("${ml.summarization.endpoint}")
    private String summarizationEndpoint;

    @Value("${ml.summarization.timeout:45}")
    private long timeoutSeconds;

    public SummarizationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Custom exception for summarization failures
     */
    public static class SummarizationException extends RuntimeException {
        public SummarizationException(String message) {
            super(message);
        }

        public SummarizationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Generate summary with enhanced error handling and resilience
     *
     * @param transcriptPath Path to the transcript file
     * @return Mono of summary string
     */
    @CircuitBreaker(name = "summarizationService", fallbackMethod = "fallbackSummarization")
    public Mono<String> generateSummary(String transcriptPath) {
        // Prepare request payload
        Map<String, String> payload = new HashMap<>();
        payload.put("transcript", transcriptPath);

        logger.info("Attempting to generate summary for transcript: {}", transcriptPath);

        return webClient.post()
                .uri(summarizationEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(summary -> logger.info("Successfully generated summary"))
                .doOnError(error -> logger.error("Error generating summary", error))
                .onErrorMap(this::mapToSummarizationException);
    }

    /**
     * Fallback method when summarization fails
     *
     * @param transcriptPath Original transcript path
     * @param throwable Original error
     * @return Mono with a fallback summary
     */
    public Mono<String> fallbackSummarization(String transcriptPath, Throwable throwable) {
        logger.warn("Fallback method invoked for transcript: {}", transcriptPath, throwable);

        // Provide a generic fallback summary or rethrow the original exception
        return Mono.just("Unable to generate summary at this time. Please try again later.");
    }

    /**
     * Map various errors to a standardized SummarizationException
     *
     * @param throwable Original error
     * @return Mapped exception
     */
    private Throwable mapToSummarizationException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException webClientError = (WebClientResponseException) throwable;
            return new SummarizationException("ML Service returned an error: " +
                    webClientError.getStatusCode() + " - " + webClientError.getResponseBodyAsString(), throwable);
        } else if (throwable instanceof java.util.concurrent.TimeoutException) {
            return new SummarizationException("Summarization request timed out after " + timeoutSeconds + " seconds", throwable);
        }

        return new SummarizationException("Unexpected error during summarization", throwable);
    }
}