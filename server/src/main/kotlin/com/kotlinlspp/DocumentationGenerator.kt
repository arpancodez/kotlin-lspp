package com.kotlinlspp

import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind

/**
 * DocumentationGenerator extracts and generates documentation from Kotlin source code.
 * It parses KDoc comments, function signatures, and class definitions to produce
 * formatted documentation in Markdown or plain text format.
 */
class DocumentationGenerator {

    /**
     * Generates documentation for a symbol at the specified position in the document.
     * @param document The source code content
     * @param symbolName The name of the symbol to document
     * @return MarkupContent with formatted documentation
     */
    fun generateDocumentation(document: String, symbolName: String): MarkupContent? {
        val lines = document.split("\n")
        
        // Find the symbol definition
        val symbolInfo = findSymbolDefinition(lines, symbolName) ?: return null
        
        // Extract KDoc comment if present
        val kdoc = extractKDocComment(lines, symbolInfo.lineNumber)
        
        // Generate documentation based on symbol type
        val documentation = when (symbolInfo.type) {
            SymbolType.CLASS -> generateClassDocumentation(symbolInfo, kdoc)
            SymbolType.FUNCTION -> generateFunctionDocumentation(symbolInfo, kdoc)
            SymbolType.PROPERTY -> generatePropertyDocumentation(symbolInfo, kdoc)
            SymbolType.INTERFACE -> generateInterfaceDocumentation(symbolInfo, kdoc)
        }
        
        return MarkupContent(MarkupKind.MARKDOWN, documentation)
    }

    /**
     * Represents different types of symbols in Kotlin code.
     */
    private enum class SymbolType {
        CLASS, FUNCTION, PROPERTY, INTERFACE
    }

    /**
     * Represents information about a symbol definition.
     */
    private data class SymbolInfo(
        val name: String,
        val type: SymbolType,
        val lineNumber: Int,
        val signature: String,
        val modifiers: List<String>
    )

    /**
     * Finds the definition of a symbol in the source code.
     */
    private fun findSymbolDefinition(lines: List<String>, symbolName: String): SymbolInfo? {
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Check for class definition
            val classRegex = Regex("(data\\s+|sealed\\s+|abstract\\s+|open\\s+)?(class)\\s+$symbolName")
            classRegex.find(trimmed)?.let {
                val modifiers = extractModifiers(trimmed)
                return SymbolInfo(
                    symbolName,
                    SymbolType.CLASS,
                    index,
                    trimmed,
                    modifiers
                )
            }
            
            // Check for interface definition
            val interfaceRegex = Regex("(interface)\\s+$symbolName")
            interfaceRegex.find(trimmed)?.let {
                return SymbolInfo(
                    symbolName,
                    SymbolType.INTERFACE,
                    index,
                    trimmed,
                    extractModifiers(trimmed)
                )
            }
            
            // Check for function definition
            val functionRegex = Regex("fun\\s+$symbolName\\s*\\(")
            functionRegex.find(trimmed)?.let {
                return SymbolInfo(
                    symbolName,
                    SymbolType.FUNCTION,
                    index,
                    trimmed,
                    extractModifiers(trimmed)
                )
            }
            
            // Check for property definition
            val propertyRegex = Regex("(val|var)\\s+$symbolName")
            propertyRegex.find(trimmed)?.let {
                return SymbolInfo(
                    symbolName,
                    SymbolType.PROPERTY,
                    index,
                    trimmed,
                    extractModifiers(trimmed)
                )
            }
        }
        
        return null
    }

    /**
     * Extracts modifiers from a declaration line.
     */
    private fun extractModifiers(line: String): List<String> {
        val modifiers = mutableListOf<String>()
        val modifierKeywords = listOf(
            "public", "private", "protected", "internal",
            "open", "abstract", "final", "sealed",
            "data", "inline", "suspend", "operator",
            "infix", "override", "companion"
        )
        
        modifierKeywords.forEach { keyword ->
            if (line.contains(Regex("\\b$keyword\\b"))) {
                modifiers.add(keyword)
            }
        }
        
        return modifiers
    }

    /**
     * Extracts KDoc comment preceding a symbol definition.
     */
    private fun extractKDocComment(lines: List<String>, symbolLineNumber: Int): String? {
        if (symbolLineNumber == 0) return null
        
        val kdocLines = mutableListOf<String>()
        var currentLine = symbolLineNumber - 1
        var foundKDocEnd = false
        
        // Check if there's a KDoc comment ending just before the symbol
        while (currentLine >= 0) {
            val trimmed = lines[currentLine].trim()
            
            if (trimmed == "*/") {
                foundKDocEnd = true
                currentLine--
                continue
            }
            
            if (foundKDocEnd) {
                if (trimmed.startsWith("/**")) {
                    // Found the start of KDoc
                    break
                } else if (trimmed.startsWith("*")) {
                    // KDoc content line
                    kdocLines.add(0, trimmed.removePrefix("*").trim())
                } else {
                    // Not a KDoc comment
                    return null
                }
            } else if (trimmed.isEmpty()) {
                // Empty line between symbol and comment
                currentLine--
                continue
            } else {
                // No KDoc comment found
                return null
            }
            
            currentLine--
        }
        
        return if (kdocLines.isNotEmpty()) kdocLines.joinToString("\n") else null
    }

    /**
     * Generates documentation for a class.
     */
    private fun generateClassDocumentation(symbolInfo: SymbolInfo, kdoc: String?): String {
        return buildString {
            append("# Class: ${symbolInfo.name}\n\n")
            
            if (symbolInfo.modifiers.isNotEmpty()) {
                append("**Modifiers**: ${symbolInfo.modifiers.joinToString(", ")}\n\n")
            }
            
            append("## Signature\n\n")
            append("```kotlin\n")
            append(symbolInfo.signature)
            append("\n```\n\n")
            
            if (kdoc != null) {
                append("## Description\n\n")
                append(kdoc)
                append("\n\n")
            }
            
            append("## Usage\n\n")
            append("To use this class, create an instance:\n\n")
            append("```kotlin\n")
            append("val instance = ${symbolInfo.name}()\n")
            append("```")
        }
    }

    /**
     * Generates documentation for a function.
     */
    private fun generateFunctionDocumentation(symbolInfo: SymbolInfo, kdoc: String?): String {
        return buildString {
            append("# Function: ${symbolInfo.name}\n\n")
            
            if (symbolInfo.modifiers.isNotEmpty()) {
                append("**Modifiers**: ${symbolInfo.modifiers.joinToString(", ")}\n\n")
            }
            
            append("## Signature\n\n")
            append("```kotlin\n")
            append(symbolInfo.signature)
            append("\n```\n\n")
            
            if (kdoc != null) {
                append("## Description\n\n")
                append(parseKDocSections(kdoc))
                append("\n\n")
            }
            
            // Extract parameters
            val params = extractParameters(symbolInfo.signature)
            if (params.isNotEmpty()) {
                append("## Parameters\n\n")
                params.forEach { (name, type) ->
                    append("- **$name**: `$type`\n")
                }
                append("\n")
            }
            
            // Extract return type
            val returnType = extractReturnType(symbolInfo.signature)
            if (returnType != null && returnType != "Unit") {
                append("## Returns\n\n")
                append("`$returnType`\n")
            }
        }
    }

    /**
     * Generates documentation for a property.
     */
    private fun generatePropertyDocumentation(symbolInfo: SymbolInfo, kdoc: String?): String {
        return buildString {
            append("# Property: ${symbolInfo.name}\n\n")
            
            if (symbolInfo.modifiers.isNotEmpty()) {
                append("**Modifiers**: ${symbolInfo.modifiers.joinToString(", ")}\n\n")
            }
            
            val isVal = symbolInfo.signature.contains("val ")
            append("**Type**: ${if (isVal) "Read-only" else "Mutable"}\n\n")
            
            append("## Signature\n\n")
            append("```kotlin\n")
            append(symbolInfo.signature)
            append("\n```\n\n")
            
            if (kdoc != null) {
                append("## Description\n\n")
                append(kdoc)
            }
        }
    }

    /**
     * Generates documentation for an interface.
     */
    private fun generateInterfaceDocumentation(symbolInfo: SymbolInfo, kdoc: String?): String {
        return buildString {
            append("# Interface: ${symbolInfo.name}\n\n")
            
            append("## Signature\n\n")
            append("```kotlin\n")
            append(symbolInfo.signature)
            append("\n```\n\n")
            
            if (kdoc != null) {
                append("## Description\n\n")
                append(kdoc)
                append("\n\n")
            }
            
            append("## Implementation\n\n")
            append("Classes implementing this interface should override all abstract methods.")
        }
    }

    /**
     * Parses KDoc sections like @param, @return, etc.
     */
    private fun parseKDocSections(kdoc: String): String {
        val lines = kdoc.split("\n")
        val mainDescription = mutableListOf<String>()
        val params = mutableMapOf<String, String>()
        var returnDoc: String? = null
        
        lines.forEach { line ->
            when {
                line.startsWith("@param") -> {
                    val parts = line.substringAfter("@param").trim().split(" ", limit = 2)
                    if (parts.size == 2) {
                        params[parts[0]] = parts[1]
                    }
                }
                line.startsWith("@return") -> {
                    returnDoc = line.substringAfter("@return").trim()
                }
                !line.startsWith("@") -> {
                    mainDescription.add(line)
                }
            }
        }
        
        return mainDescription.joinToString("\n")
    }

    /**
     * Extracts parameters from a function signature.
     */
    private fun extractParameters(signature: String): List<Pair<String, String>> {
        val params = mutableListOf<Pair<String, String>>()
        val paramsRegex = Regex("\\(([^)]*)\\)")
        val match = paramsRegex.find(signature) ?: return params
        
        val paramsString = match.groupValues[1]
        if (paramsString.isBlank()) return params
        
        paramsString.split(",").forEach { param ->
            val trimmed = param.trim()
            val parts = trimmed.split(":", limit = 2)
            if (parts.size == 2) {
                val name = parts[0].trim()
                val type = parts[1].trim().split("=")[0].trim()
                params.add(name to type)
            }
        }
        
        return params
    }

    /**
     * Extracts return type from a function signature.
     */
    private fun extractReturnType(signature: String): String? {
        val returnTypeRegex = Regex("\\)\\s*:\\s*([^{=\\s]+)")
        return returnTypeRegex.find(signature)?.groupValues?.get(1)?.trim()
    }

    /**
     * Generates a quick documentation snippet for hover display.
     * @param document The source code content
     * @param symbolName The name of the symbol
     * @return Short documentation string
     */
    fun generateQuickDoc(document: String, symbolName: String): String? {
        val lines = document.split("\n")
        val symbolInfo = findSymbolDefinition(lines, symbolName) ?: return null
        
        return buildString {
            append("**${symbolInfo.type.name.lowercase().replaceFirstChar { it.uppercase() }}**: `${symbolInfo.name}`\n\n")
            append("```kotlin\n")
            append(symbolInfo.signature)
            append("\n```")
        }
    }
}
