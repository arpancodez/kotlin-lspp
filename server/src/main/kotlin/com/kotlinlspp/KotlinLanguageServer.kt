package com.kotlinlspp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture

/**
 * Main entry point for the Kotlin Language Server.
 * 
 * This class implements the Language Server Protocol (LSP) for Kotlin,
 * providing IDE features such as code completion, diagnostics, hover information,
 * and navigation capabilities to Visual Studio Code and other LSP clients.
 * 
 * @author Arpan
 * @version 1.0.0
 * @see <a href="https://microsoft.github.io/language-server-protocol/">LSP Specification</a>
 */
class KotlinLanguageServer : LanguageServer {
    
    private val textDocumentService: KotlinTextDocumentService = KotlinTextDocumentService()
    private val workspaceService: KotlinWorkspaceService = KotlinWorkspaceService()
    
    /**
     * Initializes the language server with client capabilities.
     * 
     * This method is called once when the client connects to the server.
     * It returns the server's capabilities, informing the client which
     * LSP features are supported.
     * 
     * @param params Initialization parameters containing client info and capabilities
     * @return A future that completes with the initialization result
     */
    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        println("[KotlinLS] Initializing Kotlin Language Server...")
        println("[KotlinLS] Client: ${params.clientInfo?.name ?: "Unknown"}")
        println("[KotlinLS] Root URI: ${params.rootUri}")
        
        // Define server capabilities
        val capabilities = ServerCapabilities()
        
        // Text document sync: Full synchronization
        capabilities.textDocumentSync = TextDocumentSyncKind.Full
        
        // Code completion support
        capabilities.completionProvider = CompletionOptions(
            /* resolveProvider */ true,
            /* triggerCharacters */ listOf(".", ":")
        )
        
        // Hover information support
        capabilities.hoverProvider = true
        
        // Go to definition support
        capabilities.definitionProvider = true
        
        // Find references support
        capabilities.referencesProvider = true
        
        // Document symbol support
        capabilities.documentSymbolProvider = true
        
        // Workspace symbol support
        capabilities.workspaceSymbolProvider = true
        
        // Code action support (quick fixes, refactoring)
        capabilities.codeActionProvider = true
        
        // Document formatting support
        capabilities.documentFormattingProvider = true
        
        println("[KotlinLS] Server capabilities initialized successfully")
        
        val result = InitializeResult(capabilities)
        return CompletableFuture.completedFuture(result)
    }
    
    /**
     * Called when the server has been initialized.
     * 
     * This notification is sent after the initialize response.
     * The server can start processing requests after this point.
     * 
     * @param params Initialized parameters (currently unused)
     */
    override fun initialized(params: InitializedParams) {
        println("[KotlinLS] Language server initialized and ready to accept requests")
    }
    
    /**
     * Called when the client is shutting down.
     * 
     * The server should prepare for shutdown but not exit yet.
     * Actual exit happens after the exit notification.
     * 
     * @return A future that completes when shutdown preparation is done
     */
    override fun shutdown(): CompletableFuture<Any> {
        println("[KotlinLS] Shutting down Kotlin Language Server...")
        return CompletableFuture.completedFuture(null)
    }
    
    /**
     * Called when the client wants to exit.
     * 
     * The server process should terminate after this notification.
     */
    override fun exit() {
        println("[KotlinLS] Exiting Kotlin Language Server")
        System.exit(0)
    }
    
    /**
     * Returns the text document service that handles document-related operations.
     * 
     * @return The text document service instance
     */
    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }
    
    /**
     * Returns the workspace service that handles workspace-related operations.
     * 
     * @return The workspace service instance
     */
    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }
}

/**
 * Main function to start the Kotlin Language Server.
 * 
 * The server communicates with clients via standard input/output (stdio),
 * which is the standard transport mechanism for LSP.
 */
fun main() {
    println("========================================")
    println("  Kotlin Language Server (LSP)")
    println("  Version: 1.0.0")
    println("  Author: Arpan")
    println("========================================")
    
    try {
        // Create server instance
        val server = KotlinLanguageServer()
        
        // Create launcher to connect server to client via stdio
        val launcher = org.eclipse.lsp4j.launch.LSPLauncher.createServerLauncher(
            server,
            System.`in`,
            System.out
        )
        
        // Get remote proxy to client
        val client = launcher.remoteProxy
        
        // Start listening for client messages
        println("[KotlinLS] Starting language server on stdio...")
        val listening = launcher.startListening()
        
        // Block until client disconnects
        listening.get()
        
        println("[KotlinLS] Language server stopped")
    } catch (e: Exception) {
        System.err.println("[KotlinLS] Fatal error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}
