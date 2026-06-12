package com.example

import kotlin.math.floor

/**
 * A safe recursive descent parser for mathematical expressions.
 * Supports +, -, *, /, %, parenthesized expressions, and negative numbers.
 */
object CalculatorEngine {

    fun evaluate(expression: String): Double {
        // Clean and tokenize the standard operator equivalents
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        if (cleaned.isEmpty()) return 0.0

        return Parser(cleaned).parse()
    }

    fun formatResult(value: Double): String {
        if (value.isInfinite() || value.isNaN()) return "Error"
        
        // If it's a whole number, show as integer to avoid trailing ".0"
        return if (floor(value) == value) {
            value.toLong().toString()
        } else {
            // Keep up to 10 decimal digits cleanly
            val str = value.toString()
            if (str.length > 15) {
                // Return simple scientific notation or format decimal
                String.format("%.8f", value).trimEnd('0').trimEnd('.')
            } else {
                str
            }
        }
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected character: ${input[pos]}")
            }
            return result
        }

        // expression = term { ('+' | '-') term }
        private fun parseExpression(): Double {
            var result = parseTerm()
            while (pos < input.length) {
                val op = input[pos]
                if (op == '+' || op == '-') {
                    pos++
                    val nextTerm = parseTerm()
                    if (op == '+') result += nextTerm else result -= nextTerm
                } else {
                    break
                }
            }
            return result
        }

        // term = factor { ('*' | '/' | '%') factor }
        private fun parseTerm(): Double {
            var result = parseFactor()
            while (pos < input.length) {
                val op = input[pos]
                if (op == '*' || op == '/' || op == '%') {
                    pos++
                    val nextFactor = parseFactor()
                    if (op == '*') {
                        result *= nextFactor
                    } else if (op == '/') {
                        if (nextFactor == 0.0) throw ArithmeticException("Division by zero")
                        result /= nextFactor
                    } else {
                        if (nextFactor == 0.0) throw ArithmeticException("Modulo by zero")
                        result %= nextFactor
                    }
                } else {
                    break
                }
            }
            return result
        }

        // factor = [ '+' | '-' ] ( number | '(' expression ')' )
        private fun parseFactor(): Double {
            var sign = 1.0
            if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) {
                if (input[pos] == '-') sign = -1.0
                pos++
            }

            if (pos >= input.length) {
                throw IllegalArgumentException("Expected expression")
            }

            val result: Double
            if (input[pos] == '(') {
                pos++ // consume '('
                result = parseExpression()
                if (pos >= input.length || input[pos] != ')') {
                    throw IllegalArgumentException("Matching parenthesis not found")
                }
                pos++ // consume ')'
            } else {
                result = parseNumber()
            }

            return sign * result
        }

        private fun parseNumber(): Double {
            val start = pos
            if (pos < input.length && input[pos].isDigit()) {
                while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                    pos++
                }
                val numStr = input.substring(start, pos)
                return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number format")
            } else {
                throw IllegalArgumentException("Expected number at position $pos")
            }
        }
    }
}
