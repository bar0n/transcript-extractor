package com.danvega.transcripts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TranscriptService - lightweight tests without Spring context
 */
@ExtendWith(MockitoExtension.class)
class TranscriptServiceUnitTest {

    @Mock
    private YouTubeTranscriptExtractor transcriptExtractor;

    private TranscriptService transcriptService;

    @BeforeEach
    void setUp() throws Exception {
        transcriptService = new TranscriptService(transcriptExtractor);
    }

    @Test
    void testExtractTranscript_Success() {
        // Given
        String videoId = "dQw4w9WgXcQ";
        String expectedTranscript = "Never gonna give you up, never gonna let you down...";
        
        when(transcriptExtractor.extractTranscript(videoId))
            .thenReturn(expectedTranscript);

        // When
        String result = transcriptService.extractTranscript(videoId);

        // Then
        assertEquals(expectedTranscript, result);
        verify(transcriptExtractor).extractTranscript(videoId);
    }

    @Test
    void testExtractTranscript_Failure() {
        // Given
        String videoId = "invalidVideo";
        String errorMessage = "No transcript available for this video";
        
        when(transcriptExtractor.extractTranscript(videoId))
            .thenReturn(errorMessage);

        // When
        String result = transcriptService.extractTranscript(videoId);

        // Then
        assertTrue(result.contains("No transcript available"));
        verify(transcriptExtractor).extractTranscript(videoId);
    }

    @Test
    void testExtractTranscript_Exception() {
        // Given
        String videoId = "errorVideo";
        
        when(transcriptExtractor.extractTranscript(videoId))
            .thenThrow(new RuntimeException("Network error"));

        // When
        String result = transcriptService.extractTranscript(videoId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Error extracting transcript"));
        assertTrue(result.contains("Network error"));
    }

    @Test
    void testExtractTranscript_EmptyResult() {
        // Given
        String videoId = "emptyVideo";
        
        when(transcriptExtractor.extractTranscript(videoId))
            .thenReturn("");

        // When
        String result = transcriptService.extractTranscript(videoId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("No transcript available"));
    }

    @Test
    void testExtractTranscript_NullResult() {
        // Given
        String videoId = "nullVideo";
        
        when(transcriptExtractor.extractTranscript(videoId))
            .thenReturn(null);

        // When
        String result = transcriptService.extractTranscript(videoId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("No transcript available"));
    }

    @Test
    void testServiceBehavior_MultipleVideos() {
        // Test that service can handle multiple different video scenarios
        
        // Success case
        when(transcriptExtractor.extractTranscript("success"))
            .thenReturn("Valid transcript content");
        
        // Error case
        when(transcriptExtractor.extractTranscript("error"))
            .thenReturn("Error extracting transcript: Access denied");
        
        // Empty case
        when(transcriptExtractor.extractTranscript("empty"))
            .thenReturn("");

        // Test all scenarios
        String successResult = transcriptService.extractTranscript("success");
        String errorResult = transcriptService.extractTranscript("error");
        String emptyResult = transcriptService.extractTranscript("empty");

        // Verify results
        assertEquals("Valid transcript content", successResult);
        assertTrue(errorResult.contains("No transcript available") || 
                  errorResult.contains("Error extracting transcript"));
        assertTrue(emptyResult.contains("No transcript available"));

        // Verify all calls were made
        verify(transcriptExtractor).extractTranscript("success");
        verify(transcriptExtractor).extractTranscript("error");
        verify(transcriptExtractor).extractTranscript("empty");
    }

    @Test
    void testExtractTranscript_DemoWithRealVideoIds() {
        // This test demonstrates the service with real YouTube video IDs
        // but uses mocked responses to show expected behavior
        
        String[] realVideoIds = {
            "dQw4w9WgXcQ", // Rick Astley - Never Gonna Give You Up
            "oHg5SJYRHA0", // Dan Vega video
            "3QDnoVM_PJI"  // Educational content
        };
        
        // Mock different realistic responses
        when(transcriptExtractor.extractTranscript("dQw4w9WgXcQ"))
            .thenReturn("Classic 80s music video transcript...");
        
        when(transcriptExtractor.extractTranscript("oHg5SJYRHA0"))
            .thenReturn("Spring Boot tutorial content...");
            
        when(transcriptExtractor.extractTranscript("3QDnoVM_PJI"))
            .thenReturn("No transcript available for this video");

        // Test each video
        for (String videoId : realVideoIds) {
            String result = transcriptService.extractTranscript(videoId);
            
            assertNotNull(result);
            assertTrue(result.length() > 0);
            
            // Log the result for demo purposes
            System.out.println("Video " + videoId + ": " + 
                             (result.length() > 50 ? result.substring(0, 50) + "..." : result));
        }

        // Verify all extractions were attempted
        for (String videoId : realVideoIds) {
            verify(transcriptExtractor).extractTranscript(videoId);
        }
    }
}