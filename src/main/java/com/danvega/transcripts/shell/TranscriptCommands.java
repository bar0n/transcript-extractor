package com.danvega.transcripts.shell;

import com.danvega.transcripts.service.TranscriptService;
import com.danvega.transcripts.service.SimpleYouTubeTranscriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class TranscriptCommands {

    private static final Logger log = LoggerFactory.getLogger(TranscriptCommands.class);
    
    private final TranscriptService transcriptService;
    private final SimpleYouTubeTranscriptService simpleTranscriptService;
    
    public TranscriptCommands(TranscriptService transcriptService, SimpleYouTubeTranscriptService simpleTranscriptService) {
        this.transcriptService = transcriptService;
        this.simpleTranscriptService = simpleTranscriptService;
    }

    @ShellMethod(value = "Extract transcript from a single YouTube video by video ID", key = "extract-transcript")
    public String extractTranscript(@ShellOption(help = "YouTube video ID (e.g., dQw4w9WgXcQ)") String videoId) {
        
        try {
            log.info("Processing transcript extraction request for video ID: {}", videoId);
            
            // Validate video ID format (basic validation)
            if (videoId == null || videoId.trim().isEmpty()) {
                return "Error: Video ID cannot be empty";
            }
            
            if (videoId.length() != 11) {
                return "Error: Invalid video ID format. YouTube video IDs should be 11 characters long";
            }
            
            String transcript = transcriptService.extractTranscript(videoId);
            
            return String.format("================================\n" +
                    "Transcript for Video ID: %s\n" +
                    "================================\n\n" +
                    "%s\n\n" +
                    "================================\n" +
                    "Extraction completed successfully\n" +
                    "================================", videoId, transcript);
                    
        } catch (Exception e) {
            log.error("Error extracting transcript for video ID: {}", videoId, e);
            return String.format("Error: %s", e.getMessage());
        }
    }

    @ShellMethod(value = "Show help information for transcript extraction", key = "transcript-help")
    public String showHelp() {
        return "YouTube Transcript Extractor Help\n" +
                "================================\n\n" +
                "Available Commands:\n\n" +
                "extract-transcript --video-id <VIDEO_ID>\n" +
                "    Extract transcript from a single YouTube video\n" +
                "    \n" +
                "    Parameters:\n" +
                "    --video-id: The 11-character YouTube video ID\n" +
                "               (found in URL: https://youtube.com/watch?v=VIDEO_ID)\n" +
                "    \n" +
                "    Examples:\n" +
                "    extract-transcript --video-id dQw4w9WgXcQ\n" +
                "    extract-transcript --video-id oHg5SJYRHA0\n" +
                "\n" +
                "transcript-help\n" +
                "    Show this help information\n" +
                "\n" +
                "Notes:\n" +
                "- You need a valid YouTube API key set in YOUTUBE_API_KEY environment variable\n" +
                "- Not all videos have transcripts available\n" +
                "- The tool will try multiple methods to extract transcripts\n" +
                "\n" +
                "Configuration:\n" +
                "- Set YOUTUBE_API_KEY environment variable before running\n" +
                "- Alternatively, configure youtube.api.key in application.yml";
    }

    @ShellMethod(value = "Test transcript extraction with simpler methods", key = "test-extract")
    public String testExtractTranscript(@ShellOption(help = "YouTube video ID for testing") String videoId) {
        
        try {
            log.info("Testing transcript extraction for video ID: {}", videoId);
            
            // Validate video ID format (basic validation)
            if (videoId == null || videoId.trim().isEmpty()) {
                return "Error: Video ID cannot be empty";
            }
            
            if (videoId.length() != 11) {
                return "Error: Invalid video ID format. YouTube video IDs should be 11 characters long";
            }
            
            String result = simpleTranscriptService.extractTranscriptSimple(videoId);
            
            return String.format("================================\n" +
                    "TEST TRANSCRIPT EXTRACTION\n" +
                    "Video ID: %s\n" +
                    "================================\n\n" +
                    "%s\n\n" +
                    "================================\n" +
                    "Test completed\n" +
                    "================================", videoId, result);
                    
        } catch (Exception e) {
            log.error("Error in test transcript extraction for video ID: {}", videoId, e);
            return String.format("Test Error: %s", e.getMessage());
        }
    }
}