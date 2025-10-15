package com.kotlinlspp

import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

/**
 * CodeActionProvider offers quick fixes and refactoring actions for common code issues.
 * It provides suggestions to automatically fix diagnostics and improve code quality.
 */
class CodeActionProvider {

    /**
     * Provides code actions for the given range and context.
     * @param uri The document URI
     * @param range The range where code actions are requested
     * @param context The code action context containing diagnostics
     * @return CompletableFuture with list of code actions
     */
    fun provideCodeActions(
        uri: String,
        range: Range,
        context: CodeActionContext,
        document: String
    ): CompletableFuture<List<CodeAction>> {
        val actions = mutableListOf<CodeAction>()
        
        // Add actions based on diagnostics
        context.diagnostics.forEach { diagnostic ->
            actions.addAll(getActionsForDiagnostic(uri, diagnostic, document))
        }
        
        // Add refactoring actions
        actions.addAll(getRefactoringActions(uri, range, document))
        
        return CompletableFuture.completedFuture(actions)
    }

    /**
     * Gets code actions for a specific diagnostic.
     */
    private fun getActionsForDiagnostic(
        uri: String,
        diagnostic: Diagnostic,
        document: String
    ): List<CodeAction> {
        val actions = mutableListOf<CodeAction>()
        
        when {
            diagnostic.message.contains("Unused import") -> {
                actions.add(createRemoveUnusedImportAction(uri, diagnostic))
            }
            diagnostic.message.contains("unmatched closing brace") -> {
                actions.add(createFixBraceAction(uri, diagnostic))
            }
            diagnostic.message.contains("Trailing comma") -> {
                actions.add(createRemoveTrailingCommaAction(uri, diagnostic))
            }
            diagnostic.message.contains("should start with") -> {
                actions.add(createFixNamingAction(uri, diagnostic, document))
            }
            diagnostic.message.contains("!!") -> {
                actions.add(createReplaceNullAssertionAction(uri, diagnostic))
            }
        }
        
        return actions
    }

    /**
     * Creates an action to remove unused imports.
     */
    private fun createRemoveUnusedImportAction(uri: String, diagnostic: Diagnostic): CodeAction {
        val action = CodeAction("Remove unused import")
        action.kind = CodeActionKind.QuickFix
        action.diagnostics = listOf(diagnostic)
        
        val edit = WorkspaceEdit()
        edit.changes = mapOf(
            uri to listOf(
                TextEdit(diagnostic.range, "")
            )
        )
        action.edit = edit
        
        return action
    }

    /**
     * Creates an action to fix brace issues.
     */
    private fun createFixBraceAction(uri: String, diagnostic: Diagnostic): CodeAction {
        val action = CodeAction("Remove extra closing brace")
        action.kind = CodeActionKind.QuickFix
        action.diagnostics = listOf(diagnostic)
        
        // This would require more sophisticated parsing
        // For now, we'll create a placeholder action
        return action
    }

    /**
     * Creates an action to remove trailing commas.
     */
    private fun createRemoveTrailingCommaAction(uri: String, diagnostic: Diagnostic): CodeAction {
        val action = CodeAction("Remove trailing comma")
        action.kind = CodeActionKind.QuickFix
        action.diagnostics = listOf(diagnostic)
        
        val edit = WorkspaceEdit()
        // Remove the comma from the position
        val newRange = Range(
            Position(diagnostic.range.start.line, diagnostic.range.start.character - 1),
            diagnostic.range.end
        )
        edit.changes = mapOf(
            uri to listOf(
                TextEdit(newRange, "")
            )
        )
        action.edit = edit
        
        return action
    }

    /**
     * Creates an action to fix naming convention issues.
     */
    private fun createFixNamingAction(
        uri: String,
        diagnostic: Diagnostic,
        document: String
    ): CodeAction {
        val lines = document.split("\n")
        val line = lines.getOrNull(diagnostic.range.start.line) ?: ""
        val start = diagnostic.range.start.character
        val end = diagnostic.range.end.character
        
        if (start >= 0 && end <= line.length) {
            val incorrectName = line.substring(start, end)
            val correctedName = if (diagnostic.message.contains("uppercase")) {
                incorrectName.replaceFirstChar { it.uppercase() }
            } else {
                incorrectName.replaceFirstChar { it.lowercase() }
            }
            
            val action = CodeAction("Rename to '$correctedName'")
            action.kind = CodeActionKind.QuickFix
            action.diagnostics = listOf(diagnostic)
            
            val edit = WorkspaceEdit()
            edit.changes = mapOf(
                uri to listOf(
                    TextEdit(diagnostic.range, correctedName)
                )
            )
            action.edit = edit
            
            return action
        }
        
        return CodeAction("Fix naming convention")
    }

    /**
     * Creates an action to replace null assertion operator.
     */
    private fun createReplaceNullAssertionAction(uri: String, diagnostic: Diagnostic): CodeAction {
        val action = CodeAction("Replace with safe call (?.)")
        action.kind = CodeActionKind.QuickFix
        action.diagnostics = listOf(diagnostic)
        
        val edit = WorkspaceEdit()
        edit.changes = mapOf(
            uri to listOf(
                TextEdit(diagnostic.range, "?.")
            )
        )
        action.edit = edit
        
        return action
    }

    /**
     * Gets refactoring actions for the selected range.
     */
    private fun getRefactoringActions(
        uri: String,
        range: Range,
        document: String
    ): List<CodeAction> {
        val actions = mutableListOf<CodeAction>()
        
        // Extract function refactoring
        if (isMultiLineSelection(range)) {
            actions.add(createExtractFunctionAction(uri, range))
        }
        
        // Add import action
        actions.add(createOrganizeImportsAction(uri))
        
        return actions
    }

    /**
     * Creates an action to extract code into a function.
     */
    private fun createExtractFunctionAction(uri: String, range: Range): CodeAction {
        val action = CodeAction("Extract to function")
        action.kind = CodeActionKind.RefactorExtract
        
        // This would require complex implementation
        // For now, it's a placeholder
        return action
    }

    /**
     * Creates an action to organize imports.
     */
    private fun createOrganizeImportsAction(uri: String): CodeAction {
        val action = CodeAction("Organize imports")
        action.kind = CodeActionKind.SourceOrganizeImports
        
        // This would sort and remove unused imports
        return action
    }

    /**
     * Checks if the range spans multiple lines.
     */
    private fun isMultiLineSelection(range: Range): Boolean {
        return range.start.line != range.end.line
    }

    /**
     * Provides source actions like organize imports on save.
     */
    fun provideSourceActions(uri: String, document: String): List<CodeAction> {
        return listOf(
            createOrganizeImportsAction(uri),
            createFormatDocumentAction(uri)
        )
    }

    /**
     * Creates an action to format the entire document.
     */
    private fun createFormatDocumentAction(uri: String): CodeAction {
        val action = CodeAction("Format document")
        action.kind = CodeActionKind.SourceFixAll
        
        // This would apply formatting rules
        return action
    }
}
