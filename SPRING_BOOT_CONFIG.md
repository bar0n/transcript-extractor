# Spring Boot Project Configuration - YouTube Transcript Extractor

## 1. Project Overview
This is a Spring Boot application that uses Spring Shell to create a command-line interface (CLI) for extracting YouTube video transcripts. It is built with Java 24 and follows modern, enterprise-grade development practices.

## 2. Technology Stack
- **Java**: 24
- **Spring Boot**: 3.4.1
- **Spring Shell**: 3.2.8 (for the CLI)
- **Build Tool**: Maven 3.9.x
- **HTTP Client**: OkHttp 4.12.0
- **HTML Parsing**: JSoup 1.17.2
- **YouTube API**: Google APIs v3
- **Testing**: JUnit 5, Mockito

## 3. Project Structure
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

## 4. Commands

### Build & Run
- **Build Project**: `mvn clean compile`
- **Run Tests**: `mvn test`
- **Package JAR**: `mvn clean package`
- **Run Application**: `mvn spring-boot:run`

### Application Usage (CLI)
- **Start Shell**: `mvn spring-boot:run`
- **Extract Transcript**: `extract-transcript --video-id "dQw4w9WgXcQ"`
- **Get Help**: `help` or `extract-transcript --help`
- **Exit Shell**: `exit`

## 5. Configuration

### `application.yml`
The primary configuration is located in `src/main/resources/application.yml`.

```yaml
youtube:
  api:
    key: ${YOUTUBE_API_KEY:your-api-key-here} # Fallback for local development

spring:
  main:
    banner-mode: "off"
  
logging:
  level:
    com:
      danvega:
        transcripts: INFO
    org:
      springframework:
        shell: WARN
```

### Environment Variables
- `YOUTUBE_API_KEY`: Your YouTube Data API v3 key (required for API-based extraction).

### Type-Safe Configuration (Recommended Practice)
For more robust validation, use `@ConfigurationProperties`. This approach provides validation, autocompletion in the IDE, and strong typing.

**Example:**
```java
// Add "spring-boot-configuration-processor" to pom.xml for metadata generation
@Configuration
@ConfigurationProperties(prefix = "youtube")
@Validated // Enables validation annotations
public class YouTubeProperties {

    @Valid
    private final Api api = new Api();

    public Api getApi() {
        return api;
    }

    public static class Api {
        @NotBlank // Fails startup if the key is missing
        private String key;

        // Getters and Setters
    }
}
```

## 6. Coding & Testing Standards

### Dependency Injection
- **Constructor Injection**: Used exclusively to ensure beans are created with all required dependencies.

### Layered Architecture
- **Shell**: `TranscriptCommands` (handles user input)
- **Service**: `TranscriptService` (contains business logic)
- **Client**: `YouTubeTranscriptExtractor` (handles external communication)

### Testing
- **Unit Tests**: Located in `src/test/java`, they test components in isolation with mocked dependencies (e.g., `TranscriptServiceUnitTest`).
- **Integration Tests**: Test the Spring context and bean interactions (e.g., `TranscriptServiceTest`).

```java
// Unit Test Example
@ExtendWith(MockitoExtension.class)
class TranscriptServiceUnitTest {
    @Mock
    private YouTubeTranscriptExtractor extractor;
    @InjectMocks
    private SimpleYouTubeTranscriptService service;

    @Test
    void testExtraction() {
        // ... test logic
    }
}

// Integration Test Example
@SpringBootTest
class TranscriptServiceTest {
    @Autowired
    private TranscriptService service;

    @Test
    void contextLoads() {
        // ... test logic
    }
}
```

## 7. Future Enhancements & Best Practices

### Performance: Caching
To avoid re-fetching the same transcript, you can implement caching.

1.  **Add Dependency**: Add `spring-boot-starter-cache` to `pom.xml`.
2.  **Enable Caching**: Add `@EnableCaching` to a `@Configuration` class.
3.  **Use `@Cacheable`**: Annotate the service method.

```java
@Service
public class TranscriptService {
    @Cacheable(value = "transcripts", key = "#videoId")
    public TranscriptResult extractTranscript(String videoId) {
        // ... extraction logic
    }
}
```

### Performance: Virtual Threads
For improved performance on I/O-bound tasks (like API calls), enable virtual threads (requires Java 21+).

**`application.yml`:**
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Containerization: Docker
To package the application as a container image, create a `Dockerfile`.

```dockerfile
# Use a slim, modern Java base image
FROM bellsoft/liberica-openjdk-alpine:24

# Set up a non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# Copy dependency layers first for better Maven caching
COPY --chown=appuser:appgroup target/dependency/ ./
COPY --chown=appuser:appgroup target/classes/ ./

USER appuser
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-cp", ".:lib/*", "com.danvega.transcripts.TranscriptExtractorApplication"]
```

### Code Quality
Consider adding Maven plugins to your `pom.xml` to enforce code quality.
- **Static Analysis**: `spotbugs-maven-plugin`
- **Code Coverage**: `jacoco-maven-plugin`

```