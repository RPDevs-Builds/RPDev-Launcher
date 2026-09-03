/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package iamrp.dev.launcher.search

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object LocalMathEngine {

    private val df = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))

    fun isMathExpression(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.length < 2) return false
        // Must contain at least one digit
        if (!trimmed.any { it.isDigit() }) return false

        // Must contain at least one math operator or known function
        val hasOperator = trimmed.any { it in "+-*/÷^%" }
        val hasFunction = listOf("sqrt", "cbrt", "sin", "cos", "tan", "log", "ln", "abs", "pi", "e").any {
            trimmed.contains(it, ignoreCase = true)
        }
        val hasPercentageOf = trimmed.contains("% of", ignoreCase = true)
        if (!hasOperator && !hasFunction && !hasPercentageOf) return false

        // Check if characters are largely mathematical
        val clean = trimmed.replace(Regex("""(?i)\b(sqrt|cbrt|sin|cos|tan|log|ln|abs|pi|e|of)\b"""), "")
        val validChars = clean.all { it.isDigit() || it.isWhitespace() || it in "+-*/xX÷^%()., " }
        return validChars
    }

    fun evaluate(expression: String): String? {
        return try {
            val sanitized = sanitize(expression)
            if (sanitized.isBlank()) return null
            val value = Parser(sanitized).parse()
            if (value.isNaN() || value.isInfinite()) return null
            df.format(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitize(input: String): String {
        var s = input.trim()
        // Replace unicode and common symbols
        s = s.replace("÷", "/")
        s = s.replace("×", "*")
        s = s.replace("X", "*")
        s = s.replace("x", "*")
        s = s.replace(",", "")
        
        // Handle "X% of Y" -> (X/100)*Y
        val percentOfRegex = Regex("""(\d+(?:\.\d+)?)\s*%\s*of\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        s = percentOfRegex.replace(s) { match ->
            val p = match.groupValues[1]
            val total = match.groupValues[2]
            "(($p/100)*$total)"
        }

        // Replace standalone percentages "X%" -> (X/100)
        val percentRegex = Regex("""(\d+(?:\.\d+)?)\s*%""")
        s = percentRegex.replace(s) { match ->
            val p = match.groupValues[1]
            "($p/100)"
        }

        // Constants
        s = s.replace(Regex("""(?i)\bpi\b"""), Math.PI.toString())
        s = s.replace(Regex("""(?i)\be\b"""), Math.E.toString())

        return s
    }

    private class Parser(private val input: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            while (ch == ' ') nextChar()
            if (pos < input.length && ch != '\u0000') {
                throw IllegalArgumentException("Unexpected: $ch")
            }
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
            } else if (ch in '0'..'9' || ch == '.') {
                while (ch in '0'..'9' || ch == '.') nextChar()
                x = input.substring(startPos, pos).toDouble()
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                val func = input.substring(startPos, pos).lowercase(Locale.ROOT)
                if (eat('(')) {
                    x = parseExpression()
                    if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis for $func")
                } else {
                    x = parseFactor()
                }
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "cbrt" -> cbrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "abs" -> abs(x)
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
            } else {
                throw IllegalArgumentException("Unexpected char: $ch")
            }

            if (eat('^')) x = x.pow(parseFactor())

            return x
        }
    }
}
