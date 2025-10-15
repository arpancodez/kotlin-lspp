package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * RenameProvider handles symbol renaming operations across the document.
 * It safely renames identifiers and updates all references to maintain code correctness.
 */
class RenameProvider {

    /**
     * Prepares a rename operation by validating the position and checking if renaming is allowed.
     * @param document The document content
     * @param position The position where rename is requested
     * @return CompletableFuture with PrepareRenameResult or null if renaming is not possible
     */
    fun prepareRename(
        document: String,
        position: Position
    ): CompletableFuture<PrepareRenameResult?> {
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            return CompletableFuture.completedFuture(null)
        }
        
        val line = lines[position.line]
        val word = extractWordAtPosition(line, position.character)
        
        if (word.isEmpty() || !isValidIdentifier(word)) {
            return CompletableFuture.completedFuture(null)
        }
        
        // Check if it's a keyword (keywords cannot be renamed)
        if (isKeyword(word)) {
            return CompletableFuture.completedFuture(null)
        }
        
        val startCol = line.indexOf(word, position.character - word.length)
        val result = PrepareRenameResult(
            Range(
                Position(position.line, startCol),
                Position(position.line, startCol + word.length)
            ),
            word
        )
        
        return CompletableFuture.completedFuture(result)
    }

    /**
     * Performs the rename operation and returns workspace edits.
     * @param document The document content
     * @param position The position of the symbol to rename
     * @param newName The new name for the symbol
     * @param uri The document URI
     * @return CompletableFuture with WorkspaceEdit containing all necessary changes
     */
    fun rename(
        document: String,
        position: Position,
        newName: String,
        uri: String
    ): CompletableFuture<WorkspaceEdit> {
        val workspaceEdit = WorkspaceEdit()
        val changes = mutableListOf<TextEdit>()
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            workspaceEdit.changes = mapOf(uri to changes)
            return CompletableFuture.completedFuture(workspaceEdit)
        }
        
        val line = lines[position.line]
        val word = extractWordAtPosition(line, position.character)
        
        if (word.isEmpty() || !isValidIdentifier(newName)) {
            workspaceEdit.changes = mapOf(uri to changes)
            return CompletableFuture.completedFuture(workspaceEdit)
        }
        
        // Find all occurrences of the symbol
        val occurrences = findAllOccurrences(document, word)
        
        // Create text edits for each occurrence
        occurrences.forEach { range ->
            changes.add(TextEdit(range, newName))
        }
        
        workspaceEdit.changes = mapOf(uri to changes)
        return CompletableFuture.completedFuture(workspaceEdit)
    }

    /**
     * Finds all occurrences of a symbol in the document.
     */
    private fun findAllOccurrences(document: String, symbol: String): List<Range> {
        val occurrences = mutableListOf<Range>()
        val lines = document.split("\n")
        
        lines.forEachIndexed { lineIndex, line ->
            var startIndex = 0
            while (true) {
                val foundIndex = line.indexOf(symbol, startIndex)
                if (foundIndex == -1) break
                
                // Check if it's a whole word match
                val beforeChar = if (foundIndex > 0) line[foundIndex - 1] else ' '
                val afterChar = if (foundIndex + symbol.length < line.length) 
                    line[foundIndex + symbol.length] else ' '
                
                if (!beforeChar.isLetterOrDigit() && beforeChar != '_' && 
                    !afterChar.isLetterOrDigit() && afterChar != '_') {
                    occurrences.add(Range(
                        Position(lineIndex, foundIndex),
                        Position(lineIndex, foundIndex + symbol.length)
                    ))
                }
                
                startIndex = foundIndex + 1
            }
        }
        
        return occurrences
    }

    /**
     * Extracts the word at the specified position in a line.
     */
    private fun extractWordAtPosition(line: String, character: Int): String {
        if (character >= line.length) {
            return ""
        }
        
        var start = character
        var end = character
        
        // Find start of word
        while (start > 0 && (line[start - 1].isLetterOrDigit() || line[start - 1] == '_')) {
            start--
        }
        
        // Find end of word
        while (end < line.length && (line[end].isLetterOrDigit() || line[end] == '_')) {
            end++
        }
        
        return if (start < end) line.substring(start, end) else ""
    }

    /**
     * Checks if a string is a valid Kotlin identifier.
     */
    private fun isValidIdentifier(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter() && name[0] != '_') return false
        return name.all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * Checks if a word is a Kotlin keyword.
     */
    private fun isKeyword(word: String): Boolean {
        val keywords = setOf(
            "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
            "companion", "const", "constructor", "continue", "crossinline", "data",
            "delegate", "do", "dynamic", "else", "enum", "expect", "external",
            "false", "field", "file", "final", "finally", "for", "fun", "get",
            "if", "import", "in", "infix", "init", "inline", "inner", "interface",
            "internal", "is", "lateinit", "noinline", "null", "object", "open",
            "operator", "out", "override", "package", "param", "private", "property",
            "protected", "public", "receiver", "reified", "return", "sealed", "set",
            "setparam", "super", "suspend", "tailrec", "this", "throw", "true",
            "try", "typealias", "typeof", "val", "var", "vararg", "when", "where", "while"
        )
        return keywords.contains(word)
    }

    /**
     * Validates a new name before performing rename.
     * @param newName The proposed new name
     * @return Pair of validation result (true if valid) and error message (if invalid)
     */
    fun validateNewName(newName: String): Pair<Boolean, String?> {
        if (newName.isEmpty()) {
            return Pair(false, "New name cannot be empty")
        }
        
        if (!isValidIdentifier(newName)) {
            return Pair(false, "'$newName' is not a valid Kotlin identifier")
        }
        
        if (isKeyword(newName)) {
            return Pair(false, "'$newName' is a Kotlin keyword and cannot be used as an identifier")
        }
        
        return Pair(true, null)
    }
}
