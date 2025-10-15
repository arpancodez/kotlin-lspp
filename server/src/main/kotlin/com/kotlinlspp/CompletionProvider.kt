package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * CompletionProvider offers intelligent code completion suggestions for Kotlin code.
 * It provides context-aware completions for keywords, functions, classes, and variables.
 */
class CompletionProvider {

    private val kotlinKeywords = listOf(
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

    private val commonTypes = listOf(
        "String", "Int", "Long", "Double", "Float", "Boolean", "Char", "Byte",
        "Short", "Any", "Unit", "Nothing", "List", "MutableList", "Set",
        "MutableSet", "Map", "MutableMap", "Array", "Collection"
    )

    /**
     * Provides completion suggestions based on the current context.
     * @param document The text document
     * @param position The cursor position
     * @return CompletableFuture with completion items
     */
    fun provideCompletions(
        document: String,
        position: Position
    ): CompletableFuture<List<CompletionItem>> {
        val completions = mutableListOf<CompletionItem>()
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            return CompletableFuture.completedFuture(completions)
        }
        
        val currentLine = lines[position.line]
        val textBeforeCursor = currentLine.substring(0, minOf(position.character, currentLine.length))
        val prefix = extractPrefix(textBeforeCursor)
        
        // Add keyword completions
        completions.addAll(getKeywordCompletions(prefix))
        
        // Add type completions
        completions.addAll(getTypeCompletions(prefix))
        
        // Add function completions
        completions.addAll(getFunctionCompletions(document, prefix))
        
        // Add variable completions
        completions.addAll(getVariableCompletions(document, prefix))
        
        return CompletableFuture.completedFuture(completions)
    }

    /**
     * Extracts the prefix being typed before the cursor.
     */
    private fun extractPrefix(textBeforeCursor: String): String {
        val regex = Regex("[a-zA-Z_][a-zA-Z0-9_]*$")
        return regex.find(textBeforeCursor)?.value ?: ""
    }

    /**
     * Provides keyword completions.
     */
    private fun getKeywordCompletions(prefix: String): List<CompletionItem> {
        return kotlinKeywords
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { keyword ->
                CompletionItem(keyword).apply {
                    kind = CompletionItemKind.Keyword
                    detail = "Kotlin keyword"
                    documentation = "Kotlin language keyword: $keyword"
                }
            }
    }

    /**
     * Provides type completions.
     */
    private fun getTypeCompletions(prefix: String): List<CompletionItem> {
        return commonTypes
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { type ->
                CompletionItem(type).apply {
                    kind = CompletionItemKind.Class
                    detail = "Kotlin type"
                    documentation = "Common Kotlin type: $type"
                }
            }
    }

    /**
     * Provides function completions based on functions defined in the document.
     */
    private fun getFunctionCompletions(document: String, prefix: String): List<CompletionItem> {
        val completions = mutableListOf<CompletionItem>()
        val functionRegex = Regex("fun\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(")
        
        functionRegex.findAll(document).forEach { match ->
            val functionName = match.groupValues[1]
            if (functionName.startsWith(prefix, ignoreCase = true)) {
                completions.add(CompletionItem(functionName).apply {
                    kind = CompletionItemKind.Function
                    detail = "Function"
                    insertText = "$functionName()"
                    documentation = "Function defined in current file"
                })
            }
        }
        
        return completions
    }

    /**
     * Provides variable completions based on variables defined in the document.
     */
    private fun getVariableCompletions(document: String, prefix: String): List<CompletionItem> {
        val completions = mutableListOf<CompletionItem>()
        val varRegex = Regex("(val|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
        
        varRegex.findAll(document).forEach { match ->
            val varName = match.groupValues[2]
            if (varName.startsWith(prefix, ignoreCase = true)) {
                val isVal = match.groupValues[1] == "val"
                completions.add(CompletionItem(varName).apply {
                    kind = CompletionItemKind.Variable
                    detail = if (isVal) "val (immutable)" else "var (mutable)"
                    documentation = "Variable defined in current file"
                })
            }
        }
        
        return completions
    }

    /**
     * Provides completion item details for resolve requests.
     */
    fun resolveCompletionItem(item: CompletionItem): CompletableFuture<CompletionItem> {
        // Add additional details to the completion item if needed
        return CompletableFuture.completedFuture(item)
    }
}
