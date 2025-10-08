# VSCode Client Extension

## Overview

This directory contains the Visual Studio Code extension that provides the client-side implementation of the Kotlin Language Server Protocol (LSP) integration.

## Architecture

The client extension is built with:
- **TypeScript** for type-safe client implementation
- **VSCode Extension API** for editor integration
- **LSP Client library** (`vscode-languageclient`) for LSP communication
- **Node.js** runtime environment

## Features

- Automatic language server startup and management
- Stdio-based communication with the Kotlin language server
- Support for all LSP features provided by the server
- Configuration management for Kotlin LSP settings
- Status bar integration for server state
- Error handling and diagnostics reporting

## Project Structure

```
client/
├── src/
│   ├── extension.ts          # Main extension entry point
│   └── languageClient.ts     # Language client configuration
├── package.json              # Extension manifest and dependencies
├── tsconfig.json             # TypeScript compiler configuration
├── .vscodeignore             # Files to exclude from extension package
├── .gitignore                # Git ignore patterns
└── README.md                 # This file
```

## Prerequisites

- Node.js 16.x or higher
- npm or yarn package manager
- Visual Studio Code 1.75.0 or higher
- Java JDK 11+ (for running the language server)

## Installation

### Install Dependencies

```bash
cd client
npm install
```

### Build the Extension

```bash
# Compile TypeScript to JavaScript
npm run compile

# Watch mode for development
npm run watch
```

## Running and Debugging

### Development Mode

1. Open VS Code in the **client** directory
2. Press **F5** to launch the Extension Development Host
3. This will:
   - Compile the extension code
   - Start a new VSCode window with the extension loaded
   - Automatically start the Kotlin language server
4. Open a Kotlin file (`.kt`) to test LSP features

### Debug Configuration

The extension uses the following debug configuration (defined in `.vscode/launch.json`):

```json
{
  "name": "Launch Client",
  "type": "extensionHost",
  "request": "launch",
  "runtimeExecutable": "${execPath}",
  "args": ["--extensionDevelopmentPath=${workspaceFolder}"]
}
```

## Configuration

### Extension Settings

The extension contributes the following settings to VSCode:

- `kotlinLanguageServer.enabled`: Enable/disable the language server (default: `true`)
- `kotlinLanguageServer.serverPath`: Custom path to language server JAR file
- `kotlinLanguageServer.javaHome`: Custom Java home directory
- `kotlinLanguageServer.trace.server`: Trace communication between client and server

### Example settings.json

```json
{
  "kotlinLanguageServer.enabled": true,
  "kotlinLanguageServer.javaHome": "/usr/lib/jvm/java-11-openjdk",
  "kotlinLanguageServer.trace.server": "verbose"
}
```

## Building for Distribution

### Package the Extension

```bash
# Install vsce (VSCode Extension CLI) if not already installed
npm install -g vsce

# Package the extension into a .vsix file
vsce package
```

This creates a `kotlin-lspp-<version>.vsix` file that can be:
- Installed locally in VSCode
- Published to the VSCode Marketplace
- Shared with other developers

### Install VSIX Locally

```bash
code --install-extension kotlin-lspp-<version>.vsix
```

## Development

### Adding New Features

1. **Modify TypeScript source** in `src/`
2. **Update package.json** with new commands, configurations, or capabilities
3. **Rebuild** with `npm run compile`
4. **Test** by pressing F5 in VSCode
5. **Add tests** in the `src/test/` directory (when test framework is set up)

### Code Structure

#### extension.ts

Main entry point for the extension. Contains:
- `activate()`: Called when extension is activated
- `deactivate()`: Called when extension is deactivated
- Language client initialization
- Server process management

#### languageClient.ts

Language client configuration. Contains:
- Server options (command, args, transport)
- Client options (document selector, synchronization)
- Middleware for request/notification handling

## Testing

### Manual Testing

1. Open a Kotlin project in the Extension Development Host
2. Test LSP features:
   - **Code Completion**: Ctrl+Space in a Kotlin file
   - **Hover**: Hover over a symbol
   - **Go to Definition**: F12 on a symbol
   - **Find References**: Shift+F12 on a symbol
   - **Diagnostics**: Check for error/warning underlines

### Automated Testing (Future)

```bash
# Run extension tests
npm run test
```

## Troubleshooting

### Extension Not Activating

- Check that Kotlin files (`.kt`) are opened
- Verify `activationEvents` in `package.json`
- Check Output panel: "Kotlin Language Server"

### Server Not Starting

- Verify Java is installed: `java -version`
- Check server JAR path configuration
- Review server logs in Output panel
- Ensure server JAR is built: `cd ../server && ./gradlew shadowJar`

### No Code Completion

- Verify server is running (check Output panel)
- Ensure file has `.kt` extension
- Check that server capabilities include completion

## Dependencies

### Runtime Dependencies

```json
{
  "vscode-languageclient": "^9.0.1",
  "vscode-languageserver": "^9.0.1"
}
```

### Development Dependencies

```json
{
  "@types/node": "^20.x.x",
  "@types/vscode": "^1.75.0",
  "typescript": "^5.2.2",
  "@typescript-eslint/eslint-plugin": "^6.x.x",
  "@typescript-eslint/parser": "^6.x.x"
}
```

## Publishing to Marketplace

### Prerequisites

1. Create a Personal Access Token (PAT) on Azure DevOps
2. Create a publisher account

### Publish Command

```bash
# Login to publisher
vsce login <publisher-name>

# Publish new version
vsce publish

# Publish specific version
vsce publish minor
vsce publish 1.0.1
```

## Contributing

When contributing to the client:
1. Follow TypeScript and VSCode extension best practices
2. Add JSDoc comments for public APIs
3. Test with multiple Kotlin projects
4. Update this README with significant changes

## Resources

- [VSCode Extension API](https://code.visualstudio.com/api)
- [Language Server Protocol](https://microsoft.github.io/language-server-protocol/)
- [vscode-languageclient Documentation](https://www.npmjs.com/package/vscode-languageclient)
- [Extension Guidelines](https://code.visualstudio.com/api/references/extension-guidelines)

## License

MIT License - See root LICENSE file for details.
