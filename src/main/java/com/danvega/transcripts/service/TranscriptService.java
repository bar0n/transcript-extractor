package com.danvega.transcripts.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

@Service
public class TranscriptService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptService.class);

    @Value("${youtube.api.key}")
    private String apiKey;

    private final YouTube youtube;
    private final YouTubeTranscriptExtractor transcriptExtractor;

    public TranscriptService(YouTubeTranscriptExtractor transcriptExtractor) throws GeneralSecurityException, IOException {
        this.transcriptExtractor = transcriptExtractor;
        this.youtube = new YouTube.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            null)
            .setApplicationName("transcript-extractor")
            .build();
    }

    public String extractTranscript(String videoId) {
        try {
            log.info("Extracting transcript for video ID: {}", videoId);

            // First try the direct YouTube transcript extractor (no API key needed)
            String transcript = transcriptExtractor.extractTranscript(videoId);

            if (transcript != null && !transcript.trim().isEmpty() &&
                !transcript.contains("No transcript available") &&
                !transcript.contains("Error extracting transcript")) {
                log.info("Successfully extracted transcript using direct method for video: {}", videoId);
                return transcript;
            }

            // Fallback: Try YouTube API method (requires API key but often fails for captions)
            log.info("Direct method failed, trying YouTube API method");
            String apiTranscript = getVideoCaptions(videoId);

            if (apiTranscript != null && !apiTranscript.trim().isEmpty()) {
                log.info("Successfully extracted transcript using API method for video: {}", videoId);
                return apiTranscript;
            }

            // Return the result from direct method even if it contains error message
            log.warn("Both methods failed for video: {}", videoId);
            return transcript != null ? transcript : "No transcript available for this video";

        } catch (Exception e) {
            log.error("Failed to extract transcript for video: {}", videoId, e);
            return "Error extracting transcript: " + e.getMessage();
        }
    }

    private String getVideoCaptions(String videoId) {
        try {
            // List available captions for the video
            YouTube.Captions.List captionsList = youtube.captions()
                .list(java.util.Arrays.asList("snippet"), videoId)
                .setKey(apiKey);

            CaptionListResponse captionsListResponse = captionsList.execute();

            if (captionsListResponse.getItems().isEmpty()) {
                log.info("No captions available for video: {}", videoId);
                return null;
            }

            // Find the first available caption (preferably English)
            Caption selectedCaption = null;
            for (Caption caption : captionsListResponse.getItems()) {
                String language = caption.getSnippet().getLanguage();
                log.info("Found caption in language: {}", language);

                if ("en".equals(language) || "en-US".equals(language)) {
                    selectedCaption = caption;
                    break;
                }

                // If no English caption found, use the first available
                if (selectedCaption == null) {
                    selectedCaption = caption;
                }
            }

            if (selectedCaption == null) {
                return null;
            }

            // Download the caption content
            return downloadCaptionContent(selectedCaption.getId());

        } catch (Exception e) {
            log.error("Error getting captions for video: {}", videoId, e);

            // Fallback: Try alternative method using direct URL access
            return tryAlternativeTranscriptMethod(videoId);
        }
    }

    private String downloadCaptionContent(String captionId) {
        try {
            YouTube.Captions.Download captionDownload = youtube.captions()
                .download(captionId)
                .setKey(apiKey)
                .setTfmt("srt"); // Request SRT format

            InputStream inputStream = captionDownload.executeMediaAsInputStream();
            return convertInputStreamToString(inputStream);

        } catch (Exception e) {
            log.error("Error downloading caption content for caption ID: {}", captionId, e);
            return null;
        }
    }

    private String tryAlternativeTranscriptMethod(String videoId) {
        try {
            log.info("Trying alternative transcript extraction method for video: {}", videoId);

            // This is a simplified approach - in a real implementation,
            // you might use a library like youtube-transcript-api port for Java
            // or implement a custom scraper for YouTube's transcript endpoint

            String url = String.format("https://www.youtube.com/watch?v=%s", videoId);
            log.info("Alternative method would access: {}", url);

            // For now, return a placeholder message
            return "Alternative transcript extraction method not yet implemented. " +
                   "Video URL: " + url + "\n" +
                   "Note: This would require implementing a custom transcript scraper.";

        } catch (Exception e) {
            log.error("Alternative transcript method failed for video: {}", videoId, e);
            return null;
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }
}
