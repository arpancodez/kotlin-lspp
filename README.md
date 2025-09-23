# Kotlin Language Server for Visual Studio Code

This project provides a Kotlin Language Server and a Visual Studio Code plugin client for enhanced Kotlin development.  
It implements the Language Server Protocol (LSP) for Kotlin, enabling features like smart code completion, diagnostics, go-to-definition, and hover information inside VSCode.

---

## Features

- Language Server implemented in Kotlin with LSP4J  
- VSCode client extension in TypeScript that starts and communicates with the language server  
- Supports syntax analysis, code completion, error diagnostics, and more  
- Customizable and extendable architecture for advanced Kotlin IDE features  
- Compatible with VSCode's built-in LSP client  

---

## Project Structure

kotlin-lsp-plugin/

├── client/ # VSCode client extension

│ ├── src/main/kotlin/ # Client code (TypeScript in practice, Kotlin example here)

│ ├── package.json # Extension manifest

│ ├── tsconfig.json

│ └── webpack.config.js

├── server/ # Kotlin language server implementation

│ ├── src/main/kotlin/ # Language server Kotlin source files

│ ├── build.gradle.kts # Gradle build config

│ └── settings.gradle.kts

├── README.md # This file

└── .gitignore

text

---

## Getting Started

### Prerequisites

- Java JDK 11 or higher  
- Gradle (or use the Gradle wrapper)  
- Node.js and npm/yarn for client  
- Visual Studio Code  

### Build & Run

1. **Build Language Server (Kotlin):**

cd server
./gradlew build

text

2. **Build VSCode Client Extension:**

cd client
npm install
npm run compile

text

3. **Run/Debug in VSCode:**

- Open VSCode at `client` folder  
- Press F5 to launch the extension host with the language server  

---

## Usage

- Open or create `.kt` Kotlin files in VSCode  
- The language server automatically provides code completion, diagnostics, hover info, and navigation  
- Use VSCode's command palette and code actions for enhanced Kotlin editing experience  

---

## Contributing

Contributions and improvements are welcome! Please fork this repo and submit pull requests.  
Check issues for current tasks and discussion.

---

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

---

## References

- [Language Server Protocol Specification](https://microsoft.github.io/language-server-protocol/)  
- [Kotlin LSP GitHub Repository](https://github.com/Kotlin/kotlin-lsp.git)  
- [VSCode Language Server Extension Guide](https://code.visualstudio.com/api/language-extensions/language-server-extension-guide)  

---

## Author

Arpan - [GitHub](https://github.com/arpancodez)
