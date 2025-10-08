# Kotlin Language Server

## Overview

This directory contains the Kotlin Language Server implementation using LSP4J (Language Server Protocol for Java). The server provides intelligent code features for Kotlin files in Visual Studio Code.

## Architecture

The language server is built with:
- **Kotlin** as the primary programming language
- **LSP4J** for Language Server Protocol implementation
- **Gradle** for build automation and dependency management
- **Java 11+** as the runtime environment

## Features

- **Code Completion**: Context-aware suggestions for Kotlin syntax
- **Diagnostics**: Real-time error detection and reporting
- **Hover Information**: Display documentation and type information
- **Go to Definition**: Navigate to symbol declarations
- **Find References**: Locate all usages of symbols
- **Code Actions**: Quick fixes and refactoring suggestions

## Project Structure

```
server/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/kotlinlspp/
│   │           ├── KotlinLanguageServer.kt
│   │           ├── KotlinTextDocumentService.kt
│   │           └── KotlinWorkspaceService.kt
│   └── test/
│       └── kotlin/
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore
└── README.md
```

## Building

### Prerequisites
- JDK 11 or higher
- Gradle 7.0+ (or use the wrapper)

### Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create a fat JAR with all dependencies
./gradlew shadowJar

# Clean build artifacts
./gradlew clean
```

## Running

### Standalone Mode

```bash
# Run the language server (stdio mode)
java -jar build/libs/kotlin-language-server-all.jar
```

### Integration with VSCode

The VSCode extension (in the `client/` directory) automatically starts and manages the language server process.

## Development

### Adding New Features

1. Implement LSP methods in `KotlinTextDocumentService.kt`
2. Add corresponding message handlers
3. Update protocol handlers in `KotlinLanguageServer.kt`
4. Write unit tests for new functionality

### Debugging

- Set `KOTLINLS_DEBUG=true` environment variable for verbose logging
- Use IntelliJ IDEA or Eclipse for step-by-step debugging
- Check logs in the VSCode Output panel (Kotlin Language Server)

## Configuration

The server accepts configuration through initialization options:

```json
{
  "kotlinls": {
    "compiler": {
      "jvmTarget": "11"
    },
    "completion": {
      "snippets": true
    }
  }
}
```

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## Dependencies

- **org.eclipse.lsp4j**: Language Server Protocol implementation
- **org.jetbrains.kotlin**: Kotlin standard library and compiler
- **com.google.code.gson**: JSON serialization

## Contributing

When contributing to the server:
1. Follow Kotlin coding conventions
2. Add KDoc comments for public APIs
3. Write unit tests for new features
4. Update this README with significant changes

## Troubleshooting

### Server won't start
- Check Java version: `java -version`
- Verify JAR file exists: `ls -la build/libs/`
- Check VSCode output panel for errors

### No code completion
- Ensure Kotlin files have `.kt` extension
- Verify language ID is set to "kotlin"
- Check server logs for initialization errors

## License

MIT License - See root LICENSE file for details.
