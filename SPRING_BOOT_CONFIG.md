# Spring Boot Project Configuration - YouTube Transcript Extractor

## Project Overview
This is a Spring Boot application for extracting YouTube video transcripts, following modern Java development practices and enterprise-grade standards.

## Technology Stack
- **Java**: 24 (Latest)
- **Spring Boot**: 3.4.1
- **Spring Framework**: 6.2.x
- **Spring Shell**: 3.2.8 (CLI interface instead of REST)
- **Build Tool**: Maven 3.9.x
- **Database**: H2 (for development/testing) - PostgreSQL for production
- **HTTP Client**: OkHttp 4.12.0
- **HTML Parsing**: JSoup 1.17.2
- **YouTube API**: Google APIs v3-rev20240814
- **Testing**: JUnit 5, Mockito
- **Documentation**: Built-in help system via Spring Shell

## Current Project Structure
```
src/main/java/com/danvega/transcripts/
├── TranscriptExtractorApplication.java    # Main Spring Boot application
├── config/
│   └── YouTubeApiConfig.java              # YouTube API configuration
├── service/
│   ├── TranscriptService.java             # Core transcript extraction logic
│   ├── YouTubeTranscriptExtractor.java    # Web scraping implementation
│   └── SimpleYouTubeTranscriptService.java # Alternative service
└── shell/
    └── TranscriptCommands.java            # Spring Shell commands (@ShellComponent)
```

## Essential Commands for This Project

### Build & Run
- Build project: `mvn clean compile`
- Run tests: `mvn test`
- Run application: `mvn spring-boot:run`
- Package JAR: `mvn clean package`

### Application Usage (Spring Shell)
- Start application: `mvn spring-boot:run`
- Extract transcript: `extract-transcript --video-id dQw4w9WgXcQ`
- Get help: `transcript-help`
- Exit shell: `exit`

### Code Quality (To be implemented)
- Run tests with coverage: `mvn test jacoco:report`
- Static analysis: `mvn spotbugs:check`
- Dependency check: `mvn dependency:analyze`

## Current Configuration

### Application Configuration (application.yml)
```yaml
youtube:
  api:
    key: ${YOUTUBE_API_KEY:your-api-key-here}

spring:
  profiles:
    active: dev
  
logging:
  level:
    com.danvega.transcripts: INFO
    org.springframework.shell: WARN
```

### Environment Variables
- `YOUTUBE_API_KEY` - Your YouTube Data API v3 key (required)

## Coding Standards Applied

### Spring Boot Best Practices ✅
- **Constructor Injection**: Used in `TranscriptService` and `TranscriptCommands`
- **Layered Architecture**: Shell Commands → Service → External APIs
- **Externalized Configuration**: YouTube API key via environment variables
- **Proper Exception Handling**: Comprehensive try-catch blocks with logging

### Current Annotations Usage
- **Shell Commands**: `@ShellComponent` + `@ShellMethod`
- **Services**: `@Service` for business logic
- **Configuration**: `@Configuration` + `@PostConstruct` for validation
- **Dependency Injection**: Constructor-based injection throughout

### Spring Shell Specific Patterns
- **Command Methods**: Use `@ShellMethod` with clear descriptions
- **Option Parameters**: Use `@ShellOption` for command parameters
- **Help Integration**: Built-in help system with descriptive method names
- **Input Validation**: Parameter validation in shell commands

## Security Guidelines Applied

### Current Security Measures ✅
- **API Key Protection**: Environment variable usage, not hardcoded
- **Input Validation**: Video ID format validation
- **HTTP Security**: Proper headers and user agents for web scraping
- **Error Handling**: No sensitive data exposure in error messages

### Additional Security Considerations
- **Rate Limiting**: Implement for YouTube API calls
- **Request Throttling**: Add delays between multiple requests
- **SSL/TLS**: Enforce HTTPS for all external API calls

## Testing Standards Implemented

### Current Test Structure ✅
- **Unit Tests**: `TranscriptServiceUnitTest` - Mocked dependencies
- **Integration Tests**: `TranscriptServiceTest` - Spring Boot context
- **Service Layer Tests**: Comprehensive coverage of transcript extraction
- **Mock External Services**: YouTube API calls properly mocked

### Test Patterns Used
```java
@ExtendWith(MockitoExtension.class)  // Unit tests
@SpringBootTest                       // Integration tests
@Mock private YouTubeTranscriptExtractor  // Mocking dependencies
```

## API Integration Guidelines

### YouTube API Best Practices ✅
- **API Key Management**: Secure environment variable storage
- **Error Handling**: Graceful degradation when API fails
- **Fallback Strategy**: Multiple extraction methods implemented
- **Rate Limiting**: Conscious of API quotas

### HTTP Client Patterns ✅
- **OkHttp Usage**: Modern HTTP client with proper configuration
- **Request Headers**: Appropriate user agents and headers
- **Response Parsing**: Multiple format support (XML, VTT, TTML)
- **Connection Management**: Proper resource cleanup

## Performance Considerations

### Current Optimizations ✅
- **Fallback Strategy**: Primary method doesn't consume API quota
- **Multiple Format Support**: Try different caption formats
- **Efficient Parsing**: JSoup for HTML/XML parsing
- **Resource Management**: Proper InputStream handling

### Future Performance Improvements
- **Caching**: Implement transcript caching with `@Cacheable`
- **Async Processing**: Use `@Async` for bulk operations
- **Connection Pooling**: Configure OkHttp connection pooling
- **Batch Processing**: Process multiple videos efficiently

## Development Workflow for This Project

### Current Status ✅
1. ✅ Basic project structure established
2. ✅ Single video transcript extraction working
3. ✅ Comprehensive test suite implemented
4. ✅ Git repository initialized and pushed to GitHub
5. ✅ Documentation and configuration completed

### Next Development Steps (From PLAN.md)
1. **Channel Processing**: Implement Dan Vega channel video extraction
2. **PDF Generation**: Add transcript to PDF conversion
3. **Docker Support**: Create Dockerfile and docker-compose.yml
4. **Batch Processing**: Multiple video processing capabilities
5. **Enhanced Error Handling**: More sophisticated retry mechanisms

## Spring Shell Specific Best Practices

### Command Design ✅
- **Clear Command Names**: `extract-transcript`, `transcript-help`
- **Descriptive Help Text**: Each command has helpful descriptions
- **Parameter Validation**: Input validation for video IDs
- **User Feedback**: Informative success/error messages

### Shell Integration Patterns
```java
@ShellMethod(value = "Extract transcript from YouTube video", key = "extract-transcript")
public String extractTranscript(@ShellOption(help = "YouTube video ID") String videoId) {
    // Implementation with proper error handling
}
```

## Configuration Management

### Current Configuration Strategy ✅
- **Environment-based**: Different configs for dev/prod
- **API Key Security**: External environment variable
- **Validation**: `@PostConstruct` validation for required configs
- **Logging**: Structured logging with appropriate levels

### Configuration Classes
```java
@Configuration
public class YouTubeApiConfig {
    @Value("${youtube.api.key}")
    private String apiKey;
    
    @PostConstruct
    public void validateConfiguration() {
        // Validation logic
    }
}
```

## Important Notes for This Project

### Current Limitations & Considerations
- **YouTube Protection**: Some videos may block transcript extraction
- **API Quotas**: YouTube Data API has daily limits
- **Format Variations**: Different caption formats require different parsing
- **Rate Limiting**: Be respectful to YouTube's servers

### Architecture Decisions Made
- **Spring Shell over REST**: CLI interface for better user interaction
- **Dual Strategy**: Web scraping + API fallback for better success rate
- **Java 24**: Latest Java features and performance improvements
- **OkHttp**: Modern HTTP client for reliable web requests

## Monitoring & Observability

### Current Logging ✅
- **SLF4J**: Structured logging throughout application
- **Log Levels**: Appropriate INFO/WARN/ERROR levels
- **Contextual Information**: Video IDs and error details in logs

### Future Monitoring Enhancements
- **Spring Boot Actuator**: Health checks and metrics
- **Custom Metrics**: Success/failure rates for extractions
- **Performance Metrics**: Response times and API usage

## Common Patterns Applied

### Design Patterns Used ✅
- **Strategy Pattern**: Multiple transcript extraction strategies
- **Template Method**: Consistent error handling across services
- **Dependency Injection**: Spring's IoC container throughout
- **Command Pattern**: Spring Shell command structure

### Anti-patterns Avoided ✅
- **No Field Injection**: Constructor injection used consistently
- **No Hardcoded Values**: All configuration externalized
- **No Static Methods**: Proper Spring bean management
- **No Fat Services**: Single responsibility principle maintained

This configuration guide reflects the current state of your YouTube Transcript Extractor project while providing a roadmap for future enhancements following Spring Boot best practices.