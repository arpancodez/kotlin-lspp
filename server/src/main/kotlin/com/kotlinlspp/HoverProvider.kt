package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * HoverProvider provides hover information and documentation for code elements.
 * When users hover over identifiers, it displays relevant information like type signatures,
 * documentation, and usage examples.
 */
class HoverProvider {

    /**
     * Provides hover information for the symbol at the given position.
     * @param document The text document content
     * @param position The position where the hover is requested
     * @return CompletableFuture with hover information
     */
    fun provideHover(document: String, position: Position): CompletableFuture<Hover?> {
        val lines = document.split("\n")
        
        if (position.line >= lines.size) {
            return CompletableFuture.completedFuture(null)
        }
        
        val line = lines[position.line]
        val word = extractWordAtPosition(line, position.character)
        
        if (word.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }
        
        val hoverContent = buildHoverContent(document, word)
        
        return if (hoverContent != null) {
            val hover = Hover(
                MarkupContent(MarkupKind.MARKDOWN, hoverContent),
                Range(Position(position.line, 0), Position(position.line, line.length))
            )
            CompletableFuture.completedFuture(hover)
        } else {
            CompletableFuture.completedFuture(null)
        }
    }

    /**
     * Extracts the word at the specified character position in a line.
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
     * Builds hover content for the given word based on its context in the document.
     */
    private fun buildHoverContent(document: String, word: String): String? {
        // Check if it's a keyword
        val keywordInfo = getKeywordInfo(word)
        if (keywordInfo != null) return keywordInfo
        
        // Check if it's a function
        val functionInfo = getFunctionInfo(document, word)
        if (functionInfo != null) return functionInfo
        
        // Check if it's a class
        val classInfo = getClassInfo(document, word)
        if (classInfo != null) return classInfo
        
        // Check if it's a variable
        val variableInfo = getVariableInfo(document, word)
        if (variableInfo != null) return variableInfo
        
        return null
    }

    /**
     * Returns hover information for Kotlin keywords.
     */
    private fun getKeywordInfo(word: String): String? {
        return when (word) {
            "val" -> "**val** - Declares a read-only property or local variable\n\nImmutable reference that can be assigned only once."
            "var" -> "**var** - Declares a mutable property or local variable\n\nMutable reference that can be reassigned."
            "fun" -> "**fun** - Declares a function\n\nUsed to define a function in Kotlin."
            "class" -> "**class** - Declares a class\n\nDefines a new class type."
            "object" -> "**object** - Declares a singleton object\n\nCreates a singleton instance."
            "interface" -> "**interface** - Declares an interface\n\nDefines a contract for classes."
            "companion" -> "**companion object** - Declares a companion object\n\nAllows you to call members without creating an instance."
            "data" -> "**data class** - Declares a data class\n\nAutomatically generates equals(), hashCode(), toString(), copy() and componentN() functions."
            "sealed" -> "**sealed class** - Declares a sealed class\n\nRestricts class hierarchies, useful for representing restricted type hierarchies."
            "enum" -> "**enum class** - Declares an enumeration\n\nDefines a type-safe enumeration."
            "when" -> "**when** - Conditional expression\n\nReplaces switch statements with more powerful pattern matching."
            "if" -> "**if** - Conditional expression\n\nEvaluates a condition and returns a value based on the result."
            "for" -> "**for** - Loop statement\n\nIterates over collections or ranges."
            "while" -> "**while** - Loop statement\n\nRepeats a block while a condition is true."
            "return" -> "**return** - Returns from a function\n\nExits the current function and optionally returns a value."
            "null" -> "**null** - Represents the absence of a value\n\nKotlin's representation of a null reference."
            "suspend" -> "**suspend** - Marks a coroutine function\n\nIndicates that a function can be suspended and resumed."
            "override" -> "**override** - Overrides a member\n\nExplicitly marks that a function or property overrides a member from a superclass."
            "abstract" -> "**abstract** - Marks an abstract member\n\nDeclares a member that must be implemented by subclasses."
            "open" -> "**open** - Allows inheritance\n\nMarks a class, function, or property as open for inheritance or overriding."
            "private" -> "**private** - Private visibility\n\nVisible only within the same class or file."
            "protected" -> "**protected** - Protected visibility\n\nVisible within the class and its subclasses."
            "internal" -> "**internal** - Internal visibility\n\nVisible within the same module."
            "public" -> "**public** - Public visibility\n\nVisible everywhere (default visibility)."
            else -> null
        }
    }

    /**
     * Returns hover information for functions defined in the document.
     */
    private fun getFunctionInfo(document: String, functionName: String): String? {
        val functionRegex = Regex("fun\\s+$functionName\\s*\\(([^)]*)\\)\\s*:\\s*([^{\\n]+)?")
        val match = functionRegex.find(document) ?: return null
        
        val params = match.groupValues[1].trim()
        val returnType = match.groupValues.getOrNull(2)?.trim() ?: "Unit"
        
        return buildString {
            append("**Function**: `$functionName`\n\n")
            append("```kotlin\n")
            append("fun $functionName($params): $returnType\n")
            append("```\n\n")
            append("Defined in current file")
        }
    }

    /**
     * Returns hover information for classes defined in the document.
     */
    private fun getClassInfo(document: String, className: String): String? {
        val classRegex = Regex("(data\\s+)?(class|interface|object)\\s+$className")
        val match = classRegex.find(document) ?: return null
        
        val modifier = match.groupValues[1].trim()
        val kind = match.groupValues[2]
        
        return buildString {
            append("**${modifier.ifEmpty { "" }} $kind**: `$className`\n\n")
            append("```kotlin\n")
            append("${modifier.ifEmpty { "" }} $kind $className\n")
            append("```\n\n")
            append("Defined in current file")
        }
    }

    /**
     * Returns hover information for variables defined in the document.
     */
    private fun getVariableInfo(document: String, varName: String): String? {
        val varRegex = Regex("(val|var)\\s+$varName\\s*:\\s*([^=\\n]+)?")
        val match = varRegex.find(document) ?: return null
        
        val kind = match.groupValues[1]
        val type = match.groupValues.getOrNull(2)?.trim() ?: "Inferred"
        
        return buildString {
            append("**Variable**: `$varName`\n\n")
            append("```kotlin\n")
            append("$kind $varName: $type\n")
            append("```\n\n")
            append(if (kind == "val") "Read-only variable" else "Mutable variable")
        }
    }
}
