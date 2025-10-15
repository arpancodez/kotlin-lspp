package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * SymbolProvider discovers and provides document and workspace symbols.
 * It enables features like "Go to Symbol" and outline views in the editor.
 */
class SymbolProvider {

    /**
     * Provides all symbols found in the given document.
     * @param uri The document URI
     * @param content The document content
     * @return CompletableFuture with list of document symbols
     */
    fun provideDocumentSymbols(
        uri: String,
        content: String
    ): CompletableFuture<List<DocumentSymbol>> {
        val symbols = mutableListOf<DocumentSymbol>()
        val lines = content.split("\n")
        
        lines.forEachIndexed { index, line ->
            // Find class declarations
            findClassDeclaration(line, index)?.let { symbols.add(it) }
            
            // Find function declarations
            findFunctionDeclaration(line, index)?.let { symbols.add(it) }
            
            // Find property declarations
            findPropertyDeclaration(line, index)?.let { symbols.add(it) }
        }
        
        return CompletableFuture.completedFuture(symbols)
    }

    /**
     * Finds class declarations in a line of code.
     */
    private fun findClassDeclaration(line: String, lineNumber: Int): DocumentSymbol? {
        val classRegex = Regex("(data\\s+|sealed\\s+|abstract\\s+|open\\s+)?(class|interface|object|enum class)\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
        val match = classRegex.find(line) ?: return null
        
        val modifier = match.groupValues[1].trim()
        val kind = match.groupValues[2]
        val name = match.groupValues[3]
        
        val symbolKind = when (kind) {
            "interface" -> SymbolKind.Interface
            "object" -> SymbolKind.Object
            "enum class" -> SymbolKind.Enum
            else -> SymbolKind.Class
        }
        
        return DocumentSymbol().apply {
            this.name = name
            this.kind = symbolKind
            this.detail = if (modifier.isNotEmpty()) "$modifier $kind" else kind
            this.range = Range(Position(lineNumber, 0), Position(lineNumber, line.length))
            this.selectionRange = Range(
                Position(lineNumber, line.indexOf(name)),
                Position(lineNumber, line.indexOf(name) + name.length)
            )
        }
    }

    /**
     * Finds function declarations in a line of code.
     */
    private fun findFunctionDeclaration(line: String, lineNumber: Int): DocumentSymbol? {
        val functionRegex = Regex("fun\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\)")
        val match = functionRegex.find(line) ?: return null
        
        val name = match.groupValues[1]
        val parameters = match.groupValues[2]
        
        return DocumentSymbol().apply {
            this.name = name
            this.kind = SymbolKind.Function
            this.detail = "fun $name($parameters)"
            this.range = Range(Position(lineNumber, 0), Position(lineNumber, line.length))
            this.selectionRange = Range(
                Position(lineNumber, line.indexOf(name)),
                Position(lineNumber, line.indexOf(name) + name.length)
            )
        }
    }

    /**
     * Finds property declarations in a line of code.
     */
    private fun findPropertyDeclaration(line: String, lineNumber: Int): DocumentSymbol? {
        val propertyRegex = Regex("(val|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:\\s*([^=\\n]+)?")
        val match = propertyRegex.find(line) ?: return null
        
        val kind = match.groupValues[1]
        val name = match.groupValues[2]
        val type = match.groupValues.getOrNull(3)?.trim() ?: "Unknown"
        
        return DocumentSymbol().apply {
            this.name = name
            this.kind = if (kind == "val") SymbolKind.Constant else SymbolKind.Variable
            this.detail = "$kind $name: $type"
            this.range = Range(Position(lineNumber, 0), Position(lineNumber, line.length))
            this.selectionRange = Range(
                Position(lineNumber, line.indexOf(name)),
                Position(lineNumber, line.indexOf(name) + name.length)
            )
        }
    }

    /**
     * Provides workspace-wide symbols matching the query.
     * @param query The search query
     * @return CompletableFuture with list of workspace symbols
     */
    fun provideWorkspaceSymbols(query: String): CompletableFuture<List<SymbolInformation>> {
        // This would typically search across multiple files in the workspace
        // For now, we'll return an empty list as a placeholder
        return CompletableFuture.completedFuture(emptyList())
    }

    /**
     * Provides references to a symbol at the given position.
     * @param document The document content
     * @param position The position of the symbol
     * @return List of locations where the symbol is referenced
     */
    fun findReferences(document: String, position: Position): List<Location> {
        val references = mutableListOf<Location>()
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            return references
        }
        
        val line = lines[position.line]
        val word = extractWordAtPosition(line, position.character)
        
        if (word.isEmpty()) {
            return references
        }
        
        // Find all occurrences of the word in the document
        lines.forEachIndexed { index, currentLine ->
            var startIndex = 0
            while (true) {
                val foundIndex = currentLine.indexOf(word, startIndex)
                if (foundIndex == -1) break
                
                // Check if it's a whole word match
                val beforeChar = if (foundIndex > 0) currentLine[foundIndex - 1] else ' '
                val afterChar = if (foundIndex + word.length < currentLine.length) 
                    currentLine[foundIndex + word.length] else ' '
                
                if (!beforeChar.isLetterOrDigit() && beforeChar != '_' && 
                    !afterChar.isLetterOrDigit() && afterChar != '_') {
                    references.add(Location(
                        "file://current",
                        Range(
                            Position(index, foundIndex),
                            Position(index, foundIndex + word.length)
                        )
                    ))
                }
                
                startIndex = foundIndex + 1
            }
        }
        
        return references
    }

    /**
     * Extracts the word at the specified position.
     */
    private fun extractWordAtPosition(line: String, character: Int): String {
        if (character >= line.length) {
            return ""
        }
        
        var start = character
        var end = character
        
        while (start > 0 && (line[start - 1].isLetterOrDigit() || line[start - 1] == '_')) {
            start--
        }
        
        while (end < line.length && (line[end].isLetterOrDigit() || line[end] == '_')) {
            end++
        }
        
        return if (start < end) line.substring(start, end) else ""
    }

    /**
     * Provides definition location for a symbol at the given position.
     * @param document The document content
     * @param position The position where definition is requested
     * @return Location of the symbol definition, if found
     */
    fun findDefinition(document: String, position: Position): Location? {
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            return null
        }
        
        val line = lines[position.line]
        val word = extractWordAtPosition(line, position.character)
        
        if (word.isEmpty()) {
            return null
        }
        
        // Search for the definition (class, function, or variable declaration)
        lines.forEachIndexed { index, currentLine ->
            val patterns = listOf(
                Regex("(class|interface|object)\\s+$word"),
                Regex("fun\\s+$word\\s*\\("),
                Regex("(val|var)\\s+$word\\s*[:\\s=]")
            )
            
            patterns.forEach { pattern ->
                val match = pattern.find(currentLine)
                if (match != null) {
                    return Location(
                        "file://current",
                        Range(
                            Position(index, currentLine.indexOf(word)),
                            Position(index, currentLine.indexOf(word) + word.length)
                        )
                    )
                }
            }
        }
        
        return null
    }
}
