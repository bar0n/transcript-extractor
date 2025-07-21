# YouTube Transcript Extractor - Spring Boot Application Plan

## Project Overview
Create a Spring Shell application to extract transcripts from all videos in Dan Vega's YouTube channel and convert them to PDF format.

## Technology Stack

### Core Technologies
- **Java 24** (Latest LTS features)
- **Spring Boot 3.4** (Latest stable release)
- **Spring Shell 3.2+**
- **Spring Data JPA**
- **Maven** (Build tool)
- **Docker & Docker Compose** (Containerization)

### Database
- **PostgreSQL** (Production)
- **H2** (Development/Testing)

### YouTube Integration
- **YouTube Data API v3**
- **YouTube Transcript API** (Community libraries)
- **Google API Client Libraries**

### PDF Generation
- **iText 7** (Primary choice for PDF creation)
- **Apache PDFBox** (Alternative option)

### Additional Libraries
- **Jackson** (JSON processing)
- **Lombok** (Reduce boilerplate)
- **Slf4j + Logback** (Logging)
- **Testcontainers** (Integration testing)
- **Micrometer** (Metrics)

## Development Phases

### ✅ Phase 1: Project Analysis & Planning
- [x] Research YouTube API capabilities
- [x] Design application architecture
- [x] Technology stack selection
- [x] Spring Shell commands design
- [x] Docker containerization planning

### 🔄 Phase 2: Project Setup & Configuration
- [ ] Create Spring Boot 3.4 Maven project with Java 24
- [ ] Setup Maven dependencies (Spring Shell, YouTube API)
- [ ] Configure application properties
- [ ] Setup YouTube API credentials management

### 🎯 Phase 3: Single Video Transcript Extraction (FIRST PRIORITY)
- [ ] Create TranscriptService for single video by YouTube ID
- [ ] Implement Shell command: extract-transcript --video-id
- [ ] Test with sample video IDs
- [ ] Handle errors and edge cases

### 📊 Phase 4: Data Models & Repository Layer
- [ ] Create Video entity with JPA annotations
- [ ] Create Transcript entity with relationships
- [ ] Create Channel entity for metadata
- [ ] Setup JPA repositories with custom queries
- [ ] Configure database migrations (Flyway)
- [ ] Create DTO classes for API responses

### 🎬 Phase 5: YouTube API Integration (Channel Processing)
- [ ] Implement YouTubeApiService
- [ ] Channel discovery and validation
- [ ] Video listing with pagination (descending by date)
- [ ] Video metadata extraction
- [ ] Rate limiting and quota management
- [ ] Error handling and retry mechanisms

### 📝 Phase 6: Bulk Transcript Extraction
- [ ] Extend TranscriptService for multiple videos
- [ ] Channel-wide transcript extraction
- [ ] Batch processing capabilities
- [ ] Progress tracking

### 📄 Phase 7: PDF Generation
- [ ] Implement PdfGenerationService
- [ ] PDF template design and styling
- [ ] Transcript formatting for PDF
- [ ] Video metadata inclusion
- [ ] Multi-language support
- [ ] Bulk PDF generation

### 🖥️ Phase 8: Complete Shell Commands
- [ ] **extract-transcript** - Single video (DONE FIRST)
- [ ] **list-videos** - Paginated video listing
- [ ] **fetch-channel** - Download all channel videos
- [ ] **extract-channel-transcripts** - Extract transcripts for channel
- [ ] **generate-pdf** - Create PDF files
- [ ] **export-all** - Complete workflow execution
- [ ] **status** - Show processing progress

### 🐳 Phase 9: Docker & Deployment
- [ ] Create optimized Dockerfile (multi-stage build)
- [ ] Configure Docker Compose with services
- [ ] Setup PostgreSQL container
- [ ] Configure volume mounts for PDF output
- [ ] Environment variables management
- [ ] Health checks and monitoring

### 🔧 Phase 10: Advanced Features
- [ ] Caching mechanisms (Redis/In-memory)
- [ ] Asynchronous processing with @Async
- [ ] Progress tracking and notifications
- [ ] Configuration management
- [ ] Metrics and monitoring (Actuator)
- [ ] Graceful shutdown handling

### 🧪 Phase 11: Testing & Quality
- [ ] Unit tests for all services
- [ ] Integration tests with Testcontainers
- [ ] Shell command testing
- [ ] YouTube API mocking for tests
- [ ] PDF generation validation
- [ ] Docker image testing

### 📦 Phase 12: Documentation & Finalization
- [ ] Comprehensive README with setup instructions
- [ ] API documentation
- [ ] Docker deployment guide
- [ ] Performance optimization
- [ ] Security considerations
- [ ] Production readiness checklist

## FIRST STEP: Single Video Transcript Extraction

### Maven Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Spring Shell -->
    <dependency>
        <groupId>org.springframework.shell</groupId>
        <artifactId>spring-shell-starter</artifactId>
    </dependency>
    
    <!-- YouTube Data API -->
    <dependency>
        <groupId>com.google.apis</groupId>
        <artifactId>google-api-services-youtube</artifactId>
        <version>v3-rev20240814-2.0.0</version>
    </dependency>
    
    <!-- Google API Client -->
    <dependency>
        <groupId>com.google.api-client</groupId>
        <artifactId>google-api-client</artifactId>
        <version>2.6.0</version>
    </dependency>
</dependencies>
```

### TranscriptService Implementation
```java
@Service
public class TranscriptService {
    
    @Value("${youtube.api.key}")
    private String apiKey;
    
    public String extractTranscript(String videoId) {
        try {
            // Method 1: Try automatic captions via API
            String transcript = getAutomaticCaptions(videoId);
            
            if (transcript == null) {
                // Method 2: Try community transcript library
                transcript = getCommunityTranscript(videoId);
            }
            
            return transcript != null ? transcript : "No transcript available";
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract transcript for video: " + videoId, e);
        }
    }
}
```

### Shell Command
```java
@ShellComponent
public class TranscriptCommands {
    
    @Autowired
    private TranscriptService transcriptService;
    
    @ShellMethod("Extract transcript from a single YouTube video")
    public String extractTranscript(@ShellOption String videoId) {
        
        try {
            String transcript = transcriptService.extractTranscript(videoId);
            
            return String.format("Transcript for video %s:\n\n%s", 
                               videoId, 
                               transcript);
                               
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
```

### Usage Example
```bash
# Extract transcript from specific video
extract-transcript --video-id "dQw4w9WgXcQ"
```

## Project Structure
```
transcript-extractor/
├── src/main/java/com/danvega/transcripts/
│   ├── TranscriptExtractorApplication.java
│   ├── service/
│   │   └── TranscriptService.java
│   ├── shell/
│   │   └── TranscriptCommands.java
│   └── config/
│       └── YouTubeApiConfig.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

## Environment Variables
```bash
YOUTUBE_API_KEY=your_api_key_here
```

---
**Status:** 🚀 Ready for Implementation - Starting with Single Video Transcript
**Target:** Extract transcript by video ID first, then expand to full channel processing
**Created:** 2025-07-21
**Last Updated:** 2025-07-21