package com.kotlinlspp

import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * CodeFormatter provides code formatting functionality for Kotlin source files.
 * It handles indentation, spacing, and code style enforcement according to Kotlin conventions.
 */
class CodeFormatter {

    /**
     * Formats the entire document according to Kotlin style guidelines.
     * @param content The source code content to format
     * @param params Formatting parameters from the LSP client
     * @return List of text edits to apply
     */
    fun formatDocument(content: String, params: DocumentFormattingParams): List<TextEdit> {
        val edits = mutableListOf<TextEdit>()
        val lines = content.split("\n")
        val formattedLines = mutableListOf<String>()
        
        var indentLevel = 0
        val indentSize = params.options.tabSize
        
        for (line in lines) {
            val trimmed = line.trim()
            
            // Decrease indent for closing braces
            if (trimmed.startsWith("}")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            // Format the line with proper indentation
            val formattedLine = " ".repeat(indentLevel * indentSize) + trimmed
            formattedLines.add(formattedLine)
            
            // Increase indent for opening braces
            if (trimmed.endsWith("{")) {
                indentLevel++
            }
        }
        
        val formattedContent = formattedLines.joinToString("\n")
        if (formattedContent != content) {
            val startPos = Position(0, 0)
            val endPos = Position(lines.size, 0)
            edits.add(TextEdit(Range(startPos, endPos), formattedContent))
        }
        
        return edits
    }
    
    /**
     * Formats a specific range within a document.
     * @param content The full document content
     * @param startLine Starting line number
     * @param endLine Ending line number
     * @return Formatted content for the specified range
     */
    fun formatRange(content: String, startLine: Int, endLine: Int): String {
        val lines = content.split("\n")
        val targetLines = lines.subList(startLine, minOf(endLine + 1, lines.size))
        return targetLines.joinToString("\n") { it.trim() }
    }
    
    /**
     * Removes trailing whitespace from all lines.
     * @param content The source code content
     * @return Content with trailing whitespace removed
     */
    fun removeTrailingWhitespace(content: String): String {
        return content.split("\n").joinToString("\n") { it.trimEnd() }
    }
}
