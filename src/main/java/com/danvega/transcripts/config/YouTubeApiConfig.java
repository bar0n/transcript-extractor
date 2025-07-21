package com.danvega.transcripts.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class YouTubeApiConfig {

    private static final Logger log = LoggerFactory.getLogger(YouTubeApiConfig.class);

    @Value("${youtube.api.key}")
    private String apiKey;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty() || "your-api-key-here".equals(apiKey)) {
            log.warn("⚠️  YouTube API key is not configured!");
            log.warn("⚠️  Please set YOUTUBE_API_KEY environment variable or configure youtube.api.key in application.yml");
            log.warn("⚠️  Get your API key from: https://console.developers.google.com/");
        } else {
            log.info("✅ YouTube API key is configured");
            log.info("✅ API key length: {} characters", apiKey.length());
        }
    }
}