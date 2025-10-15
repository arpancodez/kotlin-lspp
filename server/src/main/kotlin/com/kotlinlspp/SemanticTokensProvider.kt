package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * SemanticTokensProvider provides semantic token information for enhanced syntax highlighting.
 * It enables editors to apply sophisticated, context-aware syntax coloring based on semantic analysis.
 */
class SemanticTokensProvider {

    // Token types as defined in the LSP specification
    private val tokenTypes = listOf(
        SemanticTokenTypes.Namespace,
        SemanticTokenTypes.Type,
        SemanticTokenTypes.Class,
        SemanticTokenTypes.Enum,
        SemanticTokenTypes.Interface,
        SemanticTokenTypes.Struct,
        SemanticTokenTypes.TypeParameter,
        SemanticTokenTypes.Parameter,
        SemanticTokenTypes.Variable,
        SemanticTokenTypes.Property,
        SemanticTokenTypes.EnumMember,
        SemanticTokenTypes.Event,
        SemanticTokenTypes.Function,
        SemanticTokenTypes.Method,
        SemanticTokenTypes.Macro,
        SemanticTokenTypes.Keyword,
        SemanticTokenTypes.Modifier,
        SemanticTokenTypes.Comment,
        SemanticTokenTypes.String,
        SemanticTokenTypes.Number,
        SemanticTokenTypes.Regexp,
        SemanticTokenTypes.Operator
    )

    // Token modifiers
    private val tokenModifiers = listOf(
        SemanticTokenModifiers.Declaration,
        SemanticTokenModifiers.Definition,
        SemanticTokenModifiers.Readonly,
        SemanticTokenModifiers.Static,
        SemanticTokenModifiers.Deprecated,
        SemanticTokenModifiers.Abstract,
        SemanticTokenModifiers.Async,
        SemanticTokenModifiers.Modification,
        SemanticTokenModifiers.Documentation,
        SemanticTokenModifiers.DefaultLibrary
    )

    /**
     * Returns the legend describing token types and modifiers supported by this provider.
     */
    fun getSemanticTokensLegend(): SemanticTokensLegend {
        return SemanticTokensLegend(tokenTypes, tokenModifiers)
    }

    /**
     * Provides semantic tokens for the entire document.
     * @param document The document content
     * @return CompletableFuture with semantic tokens
     */
    fun provideSemanticTokens(document: String): CompletableFuture<SemanticTokens> {
        val tokens = mutableListOf<Int>()
        val lines = document.split("\n")
        
        var previousLine = 0
        var previousStartChar = 0
        
        lines.forEachIndexed { lineIndex, line ->
            val lineTokens = analyzeLineTokens(line, lineIndex)
            
            lineTokens.forEach { token ->
                // Calculate delta line and delta start
                val deltaLine = token.line - previousLine
                val deltaStartChar = if (deltaLine == 0) {
                    token.startChar - previousStartChar
                } else {
                    token.startChar
                }
                
                // Add token data: [deltaLine, deltaStartChar, length, tokenType, tokenModifiers]
                tokens.add(deltaLine)
                tokens.add(deltaStartChar)
                tokens.add(token.length)
                tokens.add(token.tokenType)
                tokens.add(token.tokenModifiers)
                
                previousLine = token.line
                previousStartChar = token.startChar
            }
        }
        
        return CompletableFuture.completedFuture(SemanticTokens(tokens))
    }

    /**
     * Data class representing a semantic token.
     */
    private data class Token(
        val line: Int,
        val startChar: Int,
        val length: Int,
        val tokenType: Int,
        val tokenModifiers: Int
    )

    /**
     * Analyzes a line and extracts semantic tokens.
     */
    private fun analyzeLineTokens(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        
        // Find keywords
        tokens.addAll(findKeywords(line, lineIndex))
        
        // Find class/interface declarations
        tokens.addAll(findClassDeclarations(line, lineIndex))
        
        // Find function declarations
        tokens.addAll(findFunctionDeclarations(line, lineIndex))
        
        // Find variables
        tokens.addAll(findVariables(line, lineIndex))
        
        // Find strings
        tokens.addAll(findStrings(line, lineIndex))
        
        // Find numbers
        tokens.addAll(findNumbers(line, lineIndex))
        
        // Find comments
        tokens.addAll(findComments(line, lineIndex))
        
        return tokens.sortedBy { it.startChar }
    }

    /**
     * Finds keyword tokens in a line.
     */
    private fun findKeywords(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val keywords = listOf(
            "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
            "companion", "const", "constructor", "continue", "data", "do", "else",
            "enum", "expect", "external", "false", "final", "finally", "for", "fun",
            "if", "import", "in", "infix", "init", "inline", "inner", "interface",
            "internal", "is", "lateinit", "noinline", "null", "object", "open",
            "operator", "out", "override", "package", "private", "protected", "public",
            "return", "sealed", "super", "suspend", "this", "throw", "true",
            "try", "typealias", "val", "var", "vararg", "when", "where", "while"
        )
        
        keywords.forEach { keyword ->
            var startIndex = 0
            while (true) {
                val index = line.indexOf(keyword, startIndex)
                if (index == -1) break
                
                // Check if it's a whole word
                val before = if (index > 0) line[index - 1] else ' '
                val after = if (index + keyword.length < line.length) line[index + keyword.length] else ' '
                
                if (!before.isLetterOrDigit() && before != '_' && !after.isLetterOrDigit() && after != '_') {
                    tokens.add(Token(
                        lineIndex,
                        index,
                        keyword.length,
                        tokenTypes.indexOf(SemanticTokenTypes.Keyword),
                        0
                    ))
                }
                
                startIndex = index + 1
            }
        }
        
        return tokens
    }

    /**
     * Finds class and interface declarations.
     */
    private fun findClassDeclarations(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val classRegex = Regex("(class|interface|object|enum class)\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
        
        classRegex.findAll(line).forEach { match ->
            val className = match.groupValues[2]
            val startIndex = match.range.first + match.value.indexOf(className)
            
            tokens.add(Token(
                lineIndex,
                startIndex,
                className.length,
                tokenTypes.indexOf(SemanticTokenTypes.Class),
                1 shl tokenModifiers.indexOf(SemanticTokenModifiers.Declaration)
            ))
        }
        
        return tokens
    }

    /**
     * Finds function declarations.
     */
    private fun findFunctionDeclarations(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val functionRegex = Regex("fun\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(")
        
        functionRegex.findAll(line).forEach { match ->
            val functionName = match.groupValues[1]
            val startIndex = match.range.first + match.value.indexOf(functionName)
            
            tokens.add(Token(
                lineIndex,
                startIndex,
                functionName.length,
                tokenTypes.indexOf(SemanticTokenTypes.Function),
                1 shl tokenModifiers.indexOf(SemanticTokenModifiers.Declaration)
            ))
        }
        
        return tokens
    }

    /**
     * Finds variable declarations.
     */
    private fun findVariables(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val varRegex = Regex("(val|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
        
        varRegex.findAll(line).forEach { match ->
            val varName = match.groupValues[2]
            val isVal = match.groupValues[1] == "val"
            val startIndex = match.range.first + match.value.indexOf(varName)
            
            val modifiers = if (isVal) {
                1 shl tokenModifiers.indexOf(SemanticTokenModifiers.Readonly) or
                1 shl tokenModifiers.indexOf(SemanticTokenModifiers.Declaration)
            } else {
                1 shl tokenModifiers.indexOf(SemanticTokenModifiers.Declaration)
            }
            
            tokens.add(Token(
                lineIndex,
                startIndex,
                varName.length,
                tokenTypes.indexOf(SemanticTokenTypes.Variable),
                modifiers
            ))
        }
        
        return tokens
    }

    /**
     * Finds string literals.
     */
    private fun findStrings(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val stringRegex = Regex("\"([^\"]*)\"|'([^']*)'")
        
        stringRegex.findAll(line).forEach { match ->
            tokens.add(Token(
                lineIndex,
                match.range.first,
                match.value.length,
                tokenTypes.indexOf(SemanticTokenTypes.String),
                0
            ))
        }
        
        return tokens
    }

    /**
     * Finds number literals.
     */
    private fun findNumbers(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b")
        
        numberRegex.findAll(line).forEach { match ->
            tokens.add(Token(
                lineIndex,
                match.range.first,
                match.value.length,
                tokenTypes.indexOf(SemanticTokenTypes.Number),
                0
            ))
        }
        
        return tokens
    }

    /**
     * Finds comments.
     */
    private fun findComments(line: String, lineIndex: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val commentIndex = line.indexOf("//")
        
        if (commentIndex != -1) {
            tokens.add(Token(
                lineIndex,
                commentIndex,
                line.length - commentIndex,
                tokenTypes.indexOf(SemanticTokenTypes.Comment),
                0
            ))
        }
        
        return tokens
    }

    /**
     * Provides semantic tokens for a specific range in the document.
     * @param document The document content
     * @param range The range to analyze
     * @return CompletableFuture with semantic tokens for the range
     */
    fun provideSemanticTokensRange(
        document: String,
        range: Range
    ): CompletableFuture<SemanticTokens> {
        val lines = document.split("\n")
        val tokens = mutableListOf<Int>()
        
        var previousLine = range.start.line
        var previousStartChar = 0
        
        for (lineIndex in range.start.line..minOf(range.end.line, lines.size - 1)) {
            val line = lines[lineIndex]
            val lineTokens = analyzeLineTokens(line, lineIndex)
            
            lineTokens.forEach { token ->
                val deltaLine = token.line - previousLine
                val deltaStartChar = if (deltaLine == 0) {
                    token.startChar - previousStartChar
                } else {
                    token.startChar
                }
                
                tokens.add(deltaLine)
                tokens.add(deltaStartChar)
                tokens.add(token.length)
                tokens.add(token.tokenType)
                tokens.add(token.tokenModifiers)
                
                previousLine = token.line
                previousStartChar = token.startChar
            }
        }
        
        return CompletableFuture.completedFuture(SemanticTokens(tokens))
    }
}
