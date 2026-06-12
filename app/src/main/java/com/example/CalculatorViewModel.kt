package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    data class HistoryItem(
        val id: Long,
        val expression: String,
        val result: String
    )

    private var historyIdCounter = 0L

    fun onInput(char: String) {
        _expression.update { current ->
            val updated = when (char) {
                "C" -> ""
                "⌫" -> if (current.isNotEmpty()) current.dropLast(1) else ""
                "+" , "−" , "×" , "÷" , "%" -> appendOperator(current, char)
                "." -> appendDecimal(current)
                "()" -> appendParenthesis(current)
                else -> {
                    // It is a digit (0-9)
                    if (current == "0") char else current + char
                }
            }
            updateLiveResult(updated)
            updated
        }
    }

    private fun appendOperator(current: String, op: String): String {
        if (current.isEmpty()) {
            if (op == "−") return op // Allow negative numbers to start
            return ""
        }
        val lastChar = current.last()
        return if (lastChar == '+' || lastChar == '−' || lastChar == '×' || lastChar == '÷' || lastChar == '%') {
            // Replace trailing operator with the newly keyed one
            current.dropLast(1) + op
        } else {
            current + op
        }
    }

    private fun appendDecimal(current: String): String {
        if (current.isEmpty()) return "0."
        val lastChar = current.last()
        if (lastChar == '+' || lastChar == '−' || lastChar == '×' || lastChar == '÷' || lastChar == '%' || lastChar == '(') {
            return current + "0."
        }
        if (lastChar == ')') return current + "×0."

        // Find the last token / number start and check if it already contains a dot
        val lastNumber = current.split('+', '−', '×', '÷', '%', '(', ')').lastOrNull() ?: ""
        if (lastNumber.contains('.')) return current // Do not add multiple decimals to the same token

        return current + "."
    }

    private fun appendParenthesis(current: String): String {
        if (current.isEmpty()) return "("
        val lastChar = current.last()

        // Count balanced parentheses
        var openCount = 0
        var closeCount = 0
        current.forEach {
            if (it == '(') openCount++
            if (it == ')') closeCount++
        }

        return if (openCount > closeCount) {
            // We can close if preceding is digit or ')'
            if (lastChar.isDigit() || lastChar == ')') {
                current + ")"
            } else {
                current + "("
            }
        } else {
            // If previous is digit or ')', insert implicit multiplication
            if (lastChar.isDigit() || lastChar == ')') {
                current + "×("
            } else {
                current + "("
            }
        }
    }

    private fun updateLiveResult(expr: String) {
        if (expr.isEmpty()) {
            _liveResult.value = ""
            return
        }
        try {
            // Try to pre-close open parentheses for a nicer live output
            var openCount = 0
            var closeCount = 0
            expr.forEach {
                if (it == '(') openCount++
                if (it == ')') closeCount++
            }
            var testExpr = expr
            val diff = openCount - closeCount
            if (diff > 0) {
                testExpr += ")".repeat(diff)
            }

            val rawEval = CalculatorEngine.evaluate(testExpr)
            _liveResult.value = "= " + CalculatorEngine.formatResult(rawEval)
        } catch (e: Exception) {
            _liveResult.value = "" // Silently hide or keep blank live results on incomplete formulas
        }
    }

    fun onEvaluate() {
        val expr = _expression.value
        if (expr.isEmpty()) return

        try {
            // Close parentheses if unbalanced
            var openCount = 0
            var closeCount = 0
            expr.forEach {
                if (it == '(') openCount++
                if (it == ')') closeCount++
            }
            val balanceExpr = if (openCount > closeCount) {
                expr + ")".repeat(openCount - closeCount)
            } else {
                expr
            }

            val finalVal = CalculatorEngine.evaluate(balanceExpr)
            val resultFormatted = CalculatorEngine.formatResult(finalVal)

            if (resultFormatted != "Error") {
                // Add to history
                _history.update { currentList ->
                    val newItem = HistoryItem(
                        id = historyIdCounter++,
                        expression = balanceExpr,
                        result = resultFormatted
                    )
                    listOf(newItem) + currentList
                }
                _expression.value = resultFormatted
                _liveResult.value = ""
            } else {
                _liveResult.value = "Error"
            }
        } catch (e: Exception) {
            _liveResult.value = "Error"
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun loadFromHistory(historyExpression: String) {
        _expression.value = historyExpression
        updateLiveResult(historyExpression)
    }
}
