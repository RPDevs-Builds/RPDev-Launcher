/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package com.android.launcher3

import iamrp.dev.launcher.search.LocalMathEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMathEngineTest {

    @Test
    fun testBasicArithmetic() {
        assertTrue(LocalMathEngine.isMathExpression("2 + 2"))
        assertEquals("4", LocalMathEngine.evaluate("2 + 2"))

        assertEquals("512", LocalMathEngine.evaluate("128 * 4"))
        assertEquals("25", LocalMathEngine.evaluate("100 / 4"))
        assertEquals("88", LocalMathEngine.evaluate("100 - 12"))
    }

    @Test
    fun testPrecedenceAndParentheses() {
        assertEquals("70", LocalMathEngine.evaluate("10 + 20 * 3"))
        assertEquals("90", LocalMathEngine.evaluate("(10 + 20) * 3"))
        assertEquals("100", LocalMathEngine.evaluate("2 ^ 3 + 92"))
    }

    @Test
    fun testPercentages() {
        assertEquals("30", LocalMathEngine.evaluate("15% of 200"))
        assertEquals("50", LocalMathEngine.evaluate("50% of 100"))
    }

    @Test
    fun testScientificFunctions() {
        assertEquals("4", LocalMathEngine.evaluate("sqrt(16)"))
        assertEquals("42", LocalMathEngine.evaluate("abs(-42)"))
    }

    @Test
    fun testInvalidAndEdgeCases() {
        assertFalse(LocalMathEngine.isMathExpression("chrome"))
        assertFalse(LocalMathEngine.isMathExpression("settings"))
        assertNull(LocalMathEngine.evaluate("10 / 0"))
        assertNull(LocalMathEngine.evaluate("abc + xyz"))
    }
}
