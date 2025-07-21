package com.danvega.transcripts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YouTubeTranscriptExtractor {
    
    private static final Logger log = LoggerFactory.getLogger(YouTubeTranscriptExtractor.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public YouTubeTranscriptExtractor() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public String extractTranscript(String videoId) {
        try {
            log.info("Attempting to extract transcript for video: {}", videoId);
            
            // Get the YouTube watch page
            String watchPageUrl = "https://www.youtube.com/watch?v=" + videoId;
            Request request = new Request.Builder()
                    .url(watchPageUrl)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Failed to fetch YouTube page: {}", response.code());
                    return "Failed to fetch video page";
                }
                
                String html = response.body().string();
                
                // Look for captions in the page
                String transcript = extractFromPlayerResponse(html);
                if (transcript != null && !transcript.trim().isEmpty()) {
                    log.info("Successfully extracted transcript");
                    return transcript;
                }
                
                // Try alternative extraction methods
                transcript = extractFromScriptTags(html);
                if (transcript != null && !transcript.trim().isEmpty()) {
                    log.info("Successfully extracted transcript using alternative method");
                    return transcript;
                }
                
                log.warn("No transcript found for video: {}", videoId);
                return "No transcript available for this video. This could be because:\n" +
                       "1. The video doesn't have captions/subtitles\n" +
                       "2. Captions are auto-generated and not accessible via scraping\n" +
                       "3. The video has restricted access to captions\n" +
                       "4. This is a private or unlisted video";
            }
            
        } catch (Exception e) {
            log.error("Error extracting transcript for video {}: {}", videoId, e.getMessage());
            return "Error extracting transcript: " + e.getMessage();
        }
    }
    
    private String extractFromPlayerResponse(String html) {
        try {
            // Try multiple patterns for finding player response
            String[] patterns = {
                "var ytInitialPlayerResponse = (\\{.*?\\});",
                "ytInitialPlayerResponse\":(\\{.*?\\}),\"",
                "\"ytInitialPlayerResponse\":(\\{.*?\\}),"
            };
            
            JsonNode playerResponse = null;
            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(html);
                
                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    try {
                        playerResponse = objectMapper.readTree(jsonStr);
                        break;
                    } catch (Exception e) {
                        log.debug("Failed to parse JSON with pattern {}: {}", patternStr, e.getMessage());
                        continue;
                    }
                }
            }
            
            if (playerResponse != null) {
                // Navigate through the JSON structure to find captions
                JsonNode captions = playerResponse
                        .path("captions")
                        .path("playerCaptionsTracklistRenderer")
                        .path("captionTracks");
                
                if (captions.isArray() && captions.size() > 0) {
                    log.info("Found {} caption tracks", captions.size());
                    
                    // Get the first available caption track (preferably English)
                    JsonNode captionTrack = null;
                    for (JsonNode track : captions) {
                        String languageCode = track.path("languageCode").asText();
                        String name = track.path("name").path("simpleText").asText();
                        log.info("Available caption track: {} ({})", name, languageCode);
                        
                        if ("en".equals(languageCode) || "en-US".equals(languageCode)) {
                            captionTrack = track;
                            break;
                        }
                    }
                    
                    // If no English track, use the first available
                    if (captionTrack == null && captions.size() > 0) {
                        captionTrack = captions.get(0);
                    }
                    
                    if (captionTrack != null) {
                        String baseUrl = captionTrack.path("baseUrl").asText();
                        if (!baseUrl.isEmpty()) {
                            log.info("Attempting to fetch transcript from: {}", baseUrl.substring(0, Math.min(baseUrl.length(), 100)) + "...");
                            return fetchTranscriptFromUrl(baseUrl);
                        }
                    }
                } else {
                    log.info("No caption tracks found in player response");
                }
            }
        } catch (Exception e) {
            log.error("Error parsing player response: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String extractFromScriptTags(String html) {
        try {
            Document doc = Jsoup.parse(html);
            
            // Look for transcript data in script tags
            doc.select("script").forEach(script -> {
                String scriptContent = script.html();
                if (scriptContent.contains("captionTracks") || scriptContent.contains("transcript")) {
                    log.debug("Found potential transcript data in script tag");
                    // Additional parsing logic could go here
                }
            });
            
        } catch (Exception e) {
            log.debug("Error parsing script tags: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String fetchTranscriptFromUrl(String url) {
        try {
            // Try different format parameters
            String[] formats = {"", "&fmt=srv3", "&fmt=vtt", "&fmt=ttml"};
            
            for (String format : formats) {
                String testUrl = url + format;
                log.debug("Trying URL with format: {}", format);
                
                Request request = new Request.Builder()
                        .url(testUrl)
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/xml,application/xml,*/*")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String content = response.body().string();
                        log.debug("Received content length: {}", content.length());
                        log.debug("Content type: {}", response.header("Content-Type"));
                        
                        if (content != null && !content.trim().isEmpty()) {
                            String result = parseTranscriptContent(content, format);
                            if (result != null && !result.trim().isEmpty()) {
                                return result;
                            }
                        }
                    } else {
                        log.debug("Request failed with status: {} for format: {}", response.code(), format);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching transcript from URL: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String parseTranscriptContent(String content, String format) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            if (format.contains("vtt")) {
                return parseVttContent(content);
            } else if (format.contains("ttml")) {
                return parseTtmlContent(content);
            } else {
                return parseTranscriptXml(content);
            }
        } catch (Exception e) {
            log.error("Error parsing transcript content with format {}: {}", format, e.getMessage());
            return null;
        }
    }
    
    private String parseVttContent(String vttContent) {
        try {
            StringBuilder transcript = new StringBuilder();
            String[] lines = vttContent.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                // Skip timestamp lines and empty lines
                if (!line.isEmpty() && 
                    !line.startsWith("WEBVTT") && 
                    !line.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3} --> \\d{2}:\\d{2}:\\d{2}\\.\\d{3}") &&
                    !line.matches("\\d+")) {
                    
                    // Remove HTML tags and decode entities
                    line = line.replaceAll("<[^>]*>", "");
                    line = org.jsoup.parser.Parser.unescapeEntities(line, false);
                    transcript.append(line).append(" ");
                }
            }
            
            String result = transcript.toString().trim();
            log.info("Parsed VTT content: {} characters", result.length());
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing VTT content: {}", e.getMessage());
            return null;
        }
    }
    
    private String parseTtmlContent(String ttmlContent) {
        try {
            Document doc = Jsoup.parse(ttmlContent, "", org.jsoup.parser.Parser.xmlParser());
            StringBuilder transcript = new StringBuilder();
            
            // TTML uses <p> elements for text
            doc.select("p").forEach(element -> {
                String text = element.text();
                if (!text.trim().isEmpty()) {
                    transcript.append(text).append(" ");
                }
            });
            
            String result = transcript.toString().trim();
            log.info("Parsed TTML content: {} characters", result.length());
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing TTML content: {}", e.getMessage());
            return null;
        }
    }
    
    private String parseTranscriptXml(String xmlContent) {
        try {
            log.debug("Parsing transcript XML content (first 500 chars): {}", 
                     xmlContent.substring(0, Math.min(xmlContent.length(), 500)));
            
            Document doc = Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());
            StringBuilder transcript = new StringBuilder();
            
            doc.select("text").forEach(element -> {
                String text = element.text();
                if (!text.trim().isEmpty()) {
                    try {
                        // Decode HTML entities and URL encoding
                        text = java.net.URLDecoder.decode(text, StandardCharsets.UTF_8);
                        text = org.jsoup.parser.Parser.unescapeEntities(text, false);
                    } catch (Exception e) {
                        // If decoding fails, use original text
                        log.debug("Failed to decode text: {}", text);
                    }
                    transcript.append(text).append(" ");
                }
            });
            
            String result = transcript.toString().trim();
            log.info("Successfully extracted transcript with {} characters", result.length());
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing transcript XML: {}", e.getMessage());
            return null;
        }
    }
}