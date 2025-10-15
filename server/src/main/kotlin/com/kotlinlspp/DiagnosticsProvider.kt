package com.kotlinlspp

import org.eclipse.lsp4j.*

/**
 * DiagnosticsProvider analyzes Kotlin source code and provides diagnostic information
 * such as errors, warnings, and info messages to help developers identify issues.
 */
class DiagnosticsProvider {

    /**
     * Analyzes the given source code and returns a list of diagnostics.
     * @param uri The document URI
     * @param content The source code content to analyze
     * @return List of diagnostics found in the code
     */
    fun analyzeDiagnostics(uri: String, content: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        
        diagnostics.addAll(checkSyntaxErrors(content))
        diagnostics.addAll(checkUnusedImports(content))
        diagnostics.addAll(checkNamingConventions(content))
        diagnostics.addAll(checkDeprecatedUsage(content))
        
        return diagnostics
    }
    
    /**
     * Checks for basic syntax errors in the code.
     */
    private fun checkSyntaxErrors(content: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = content.split("\n")
        
        lines.forEachIndexed { index, line ->
            // Check for unmatched braces
            val openBraces = line.count { it == '{' }
            val closeBraces = line.count { it == '}' }
            
            if (openBraces != closeBraces && line.trim().endsWith("{") && !line.trim().endsWith("}}")) {
                // This is likely intentional, skip
            } else if (closeBraces > openBraces) {
                val diagnostic = Diagnostic(
                    Range(Position(index, 0), Position(index, line.length)),
                    "Possible unmatched closing brace",
                    DiagnosticSeverity.Warning,
                    "kotlin-lspp"
                )
                diagnostics.add(diagnostic)
            }
            
            // Check for missing semicolons in places where they might be needed
            if (line.trim().endsWith(",") && !line.contains("(")) {
                val diagnostic = Diagnostic(
                    Range(Position(index, line.length - 1), Position(index, line.length)),
                    "Trailing comma may be unnecessary",
                    DiagnosticSeverity.Information,
                    "kotlin-lspp"
                )
                diagnostics.add(diagnostic)
            }
        }
        
        return diagnostics
    }
    
    /**
     * Checks for unused imports.
     */
    private fun checkUnusedImports(content: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = content.split("\n")
        val imports = mutableMapOf<String, Int>()
        
        lines.forEachIndexed { index, line ->
            if (line.trim().startsWith("import ")) {
                val importName = line.substringAfter("import ").trim()
                imports[importName] = index
            }
        }
        
        // Check if each import is used (simplified check)
        imports.forEach { (importName, lineIndex) ->
            val className = importName.substringAfterLast(".")
            val usageCount = content.split("\n").count { 
                it.contains(className) && !it.trim().startsWith("import")
            }
            
            if (usageCount == 0) {
                val line = lines[lineIndex]
                val diagnostic = Diagnostic(
                    Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
                    "Unused import: $importName",
                    DiagnosticSeverity.Hint,
                    "kotlin-lspp"
                )
                diagnostics.add(diagnostic)
            }
        }
        
        return diagnostics
    }
    
    /**
     * Checks naming conventions for classes, functions, and variables.
     */
    private fun checkNamingConventions(content: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = content.split("\n")
        
        lines.forEachIndexed { index, line ->
            // Check class names (should start with uppercase)
            if (line.trim().startsWith("class ")) {
                val className = line.substringAfter("class ").split("(", " ", "{")[0]
                if (className.isNotEmpty() && className[0].isLowerCase()) {
                    val diagnostic = Diagnostic(
                        Range(Position(index, line.indexOf(className)), Position(index, line.indexOf(className) + className.length)),
                        "Class name should start with an uppercase letter",
                        DiagnosticSeverity.Warning,
                        "kotlin-lspp"
                    )
                    diagnostics.add(diagnostic)
                }
            }
            
            // Check function names (should start with lowercase)
            if (line.trim().startsWith("fun ")) {
                val functionName = line.substringAfter("fun ").split("(", " ")[0]
                if (functionName.isNotEmpty() && functionName[0].isUpperCase()) {
                    val diagnostic = Diagnostic(
                        Range(Position(index, line.indexOf(functionName)), Position(index, line.indexOf(functionName) + functionName.length)),
                        "Function name should start with a lowercase letter",
                        DiagnosticSeverity.Warning,
                        "kotlin-lspp"
                    )
                    diagnostics.add(diagnostic)
                }
            }
        }
        
        return diagnostics
    }
    
    /**
     * Checks for usage of deprecated APIs or patterns.
     */
    private fun checkDeprecatedUsage(content: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = content.split("\n")
        
        val deprecatedPatterns = mapOf(
            "!!" to "Avoid using !! operator, use safe calls or explicit null checks",
            "Thread.sleep" to "Consider using kotlinx.coroutines.delay instead"
        )
        
        lines.forEachIndexed { index, line ->
            deprecatedPatterns.forEach { (pattern, message) ->
                if (line.contains(pattern)) {
                    val startCol = line.indexOf(pattern)
                    val diagnostic = Diagnostic(
                        Range(Position(index, startCol), Position(index, startCol + pattern.length)),
                        message,
                        DiagnosticSeverity.Information,
                        "kotlin-lspp"
                    )
                    diagnostics.add(diagnostic)
                }
            }
        }
        
        return diagnostics
    }
}
