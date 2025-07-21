# YouTube Transcript Extractor

A Spring Boot application with Spring Shell to extract transcripts from YouTube videos and convert them to PDF format.

## Quick Start

### Prerequisites
- Java 24
- Maven 3.9+
- YouTube Data API v3 key

### Setup

1. **Get YouTube API Key**
   - Go to [Google Cloud Console](https://console.developers.google.com/)
   - Create a new project or select existing one
   - Enable YouTube Data API v3
   - Create credentials (API Key)
   - Copy the API key

2. **Set Environment Variable**
   ```bash
   export YOUTUBE_API_KEY=your_actual_api_key_here
   ```

3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Usage

Once the application starts, you'll see a Spring Shell prompt:

```bash
shell:>
```

#### Extract Single Video Transcript

```bash
shell:> extract-transcript --video-id dQw4w9WgXcQ
```

#### Get Help

```bash
shell:> transcript-help
```

### Example Commands

```bash
# Extract transcript from Rick Astley's "Never Gonna Give You Up"
extract-transcript --video-id dQw4w9WgXcQ

# Extract transcript from a Dan Vega tutorial (example)
extract-transcript --video-id oHg5SJYRHA0

# Show help
transcript-help
```

## Project Structure

```
src/main/java/com/danvega/transcripts/
├── TranscriptExtractorApplication.java    # Main application
├── service/
│   └── TranscriptService.java             # Core transcript extraction logic
├── shell/
│   └── TranscriptCommands.java            # Spring Shell commands
└── config/
    └── YouTubeApiConfig.java              # YouTube API configuration
```

## Features

- ✅ Extract transcripts from single YouTube videos by video ID
- ✅ YouTube Data API v3 integration
- ✅ Spring Shell interactive commands
- ✅ Comprehensive error handling
- ✅ Configuration validation
- 🔄 Channel-wide transcript extraction (coming soon)
- 🔄 PDF generation (coming soon)
- 🔄 Docker support (coming soon)

## Configuration

### Application Properties (application.yml)

```yaml
youtube:
  api:
    key: ${YOUTUBE_API_KEY:your-api-key-here}
```

### Environment Variables

- `YOUTUBE_API_KEY` - Your YouTube Data API v3 key (required)

## Troubleshooting

### Common Issues

1. **"YouTube API key is not configured"**
   - Make sure you've set the `YOUTUBE_API_KEY` environment variable
   - Verify the API key is valid and has YouTube Data API v3 enabled

2. **"No transcript available"**
   - Not all YouTube videos have transcripts/captions
   - Some videos may have transcripts but they're not accessible via API

3. **API Quota Exceeded**
   - YouTube Data API has daily quotas
   - Default quota is usually sufficient for development

### Getting YouTube Video ID

The video ID is the 11-character string in YouTube URLs:
- `https://www.youtube.com/watch?v=dQw4w9WgXcQ` → Video ID: `dQw4w9WgXcQ`
- `https://youtu.be/dQw4w9WgXcQ` → Video ID: `dQw4w9WgXcQ`

## Development

### Build
```bash
mvn clean compile
```

### Test
```bash
mvn test
```

### Package
```bash
mvn clean package
```

### Run with Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Next Steps

See [PLAN.md](PLAN.md) for the complete development roadmap including:
- Channel-wide video processing
- Bulk transcript extraction
- PDF generation
- Docker containerization
- Advanced features

## License

This project is for educational purposes.