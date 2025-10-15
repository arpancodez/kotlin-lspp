package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture
import java.util.Stack

/**
 * FoldingRangeProvider identifies regions in code that can be folded/collapsed.
 * This enables users to hide blocks of code like functions, classes, comments, and imports
 * to improve code navigation and readability.
 */
class FoldingRangeProvider {

    /**
     * Provides folding ranges for the entire document.
     * @param document The document content
     * @return CompletableFuture with list of folding ranges
     */
    fun provideFoldingRanges(document: String): CompletableFuture<List<FoldingRange>> {
        val ranges = mutableListOf<FoldingRange>()
        val lines = document.split("\n")
        
        // Find class/interface/object blocks
        ranges.addAll(findBlockFolding(lines))
        
        // Find function blocks
        ranges.addAll(findFunctionFolding(lines))
        
        // Find multi-line comments
        ranges.addAll(findCommentFolding(lines))
        
        // Find import blocks
        ranges.addAll(findImportFolding(lines))
        
        return CompletableFuture.completedFuture(ranges.sortedBy { it.startLine })
    }

    /**
     * Finds folding ranges for blocks defined by braces (classes, objects, etc.).
     */
    private fun findBlockFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        val braceStack = Stack<Pair<Int, String>>() // Stack of (line number, block type)
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Check for class, interface, object, or enum declarations
            val blockType = when {
                trimmed.contains(Regex("^(data\\s+)?(class|interface|object|enum class)\\s+")) -> "class"
                trimmed.contains(Regex("^fun\\s+")) -> "function"
                trimmed.contains(Regex("^(if|when|for|while)\\s*\\(")) -> "control"
                else -> null
            }
            
            // Count opening and closing braces
            val openBraces = line.count { it == '{' }
            val closeBraces = line.count { it == '}' }
            
            // Handle opening braces
            repeat(openBraces) {
                braceStack.push(index to (blockType ?: "block"))
            }
            
            // Handle closing braces
            repeat(closeBraces) {
                if (braceStack.isNotEmpty()) {
                    val (startLine, type) = braceStack.pop()
                    if (index > startLine) {
                        val range = FoldingRange(startLine, index)
                        range.kind = when (type) {
                            "class" -> FoldingRangeKind.Region
                            "function" -> FoldingRangeKind.Region
                            else -> FoldingRangeKind.Region
                        }
                        ranges.add(range)
                    }
                }
            }
        }
        
        return ranges
    }

    /**
     * Finds folding ranges for function blocks.
     */
    private fun findFunctionFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        val functionStack = Stack<Int>()
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Check for function declaration
            if (trimmed.startsWith("fun ") && trimmed.contains("{")) {
                functionStack.push(index)
            } else if (trimmed.startsWith("}") && functionStack.isNotEmpty()) {
                val startLine = functionStack.pop()
                if (index > startLine + 1) {
                    val range = FoldingRange(startLine, index)
                    range.kind = FoldingRangeKind.Region
                    ranges.add(range)
                }
            }
        }
        
        return ranges
    }

    /**
     * Finds folding ranges for multi-line comments.
     */
    private fun findCommentFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        var commentStart: Int? = null
        var inBlockComment = false
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Check for block comment start
            if (trimmed.contains("/*") && !inBlockComment) {
                commentStart = index
                inBlockComment = true
            }
            
            // Check for block comment end
            if (trimmed.contains("*/") && inBlockComment) {
                commentStart?.let { start ->
                    if (index > start) {
                        val range = FoldingRange(start, index)
                        range.kind = FoldingRangeKind.Comment
                        ranges.add(range)
                    }
                }
                commentStart = null
                inBlockComment = false
            }
            
            // Check for KDoc comments (/** ... */)
            if (trimmed.startsWith("/**")) {
                commentStart = index
            } else if (trimmed == "*/" && commentStart != null && !inBlockComment) {
                if (index > commentStart!!) {
                    val range = FoldingRange(commentStart!!, index)
                    range.kind = FoldingRangeKind.Comment
                    ranges.add(range)
                }
                commentStart = null
            }
        }
        
        // Find consecutive single-line comments
        var singleLineCommentStart: Int? = null
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("//")) {
                if (singleLineCommentStart == null) {
                    singleLineCommentStart = index
                }
            } else {
                singleLineCommentStart?.let { start ->
                    if (index - start > 2) { // Only fold if there are 3+ consecutive comment lines
                        val range = FoldingRange(start, index - 1)
                        range.kind = FoldingRangeKind.Comment
                        ranges.add(range)
                    }
                }
                singleLineCommentStart = null
            }
        }
        
        return ranges
    }

    /**
     * Finds folding ranges for import statements.
     */
    private fun findImportFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        var importStart: Int? = null
        var lastImportLine: Int? = null
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("import ")) {
                if (importStart == null) {
                    importStart = index
                }
                lastImportLine = index
            } else if (importStart != null && lastImportLine != null) {
                // End of import block
                if (lastImportLine!! - importStart!! >= 2) { // Only fold if there are 3+ imports
                    val range = FoldingRange(importStart!!, lastImportLine!!)
                    range.kind = FoldingRangeKind.Imports
                    ranges.add(range)
                }
                importStart = null
                lastImportLine = null
            }
        }
        
        // Handle case where imports are at the end of the file
        if (importStart != null && lastImportLine != null && lastImportLine!! - importStart!! >= 2) {
            val range = FoldingRange(importStart!!, lastImportLine!!)
            range.kind = FoldingRangeKind.Imports
            ranges.add(range)
        }
        
        return ranges
    }

    /**
     * Finds folding ranges for when expressions.
     */
    fun findWhenExpressionFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        var whenStart: Int? = null
        var braceCount = 0
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("when") && trimmed.contains("{")) {
                whenStart = index
                braceCount = 1
            } else if (whenStart != null) {
                braceCount += line.count { it == '{' }
                braceCount -= line.count { it == '}' }
                
                if (braceCount == 0) {
                    val range = FoldingRange(whenStart!!, index)
                    range.kind = FoldingRangeKind.Region
                    ranges.add(range)
                    whenStart = null
                }
            }
        }
        
        return ranges
    }

    /**
     * Finds folding ranges for data class parameters.
     */
    fun findDataClassParameterFolding(lines: List<String>): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        var classStart: Int? = null
        var inParameters = false
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("data class") && trimmed.contains("(")) {
                classStart = index
                inParameters = true
            } else if (inParameters && classStart != null && trimmed.contains(")")) {
                if (index > classStart!!) {
                    val range = FoldingRange(classStart!!, index)
                    range.kind = FoldingRangeKind.Region
                    ranges.add(range)
                }
                classStart = null
                inParameters = false
            }
        }
        
        return ranges
    }
}
