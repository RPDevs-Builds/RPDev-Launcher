/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package com.android.launcher3

import androidx.test.core.app.ApplicationProvider
import iamrp.dev.launcher.data.models.SearchProvider
import iamrp.dev.launcher.groups.AppGroupsManager
import iamrp.dev.launcher.search.LocalMathEngine
import iamrp.dev.launcher.search.SearchProviderController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlingAndEdgeCaseTest {

    @Test
    fun testMathEngineMalformedExpressionsGracefulFail() {
        // Unclosed parentheses
        assertNull(LocalMathEngine.evaluate("((5 + 2"))
        // Consecutive invalid operators
        assertNull(LocalMathEngine.evaluate("5 ++ * 2"))
        // Divide by zero
        assertNull(LocalMathEngine.evaluate("10 / 0"))
        // Empty string
        assertNull(LocalMathEngine.evaluate(""))
        // Pure whitespace
        assertNull(LocalMathEngine.evaluate("   "))
        // Giant power causing overflow / infinity
        assertNull(LocalMathEngine.evaluate("10 ^ 999999"))
        // Non-math text
        assertFalse(LocalMathEngine.isMathExpression("hello world"))
        assertFalse(LocalMathEngine.isMathExpression(""))
        assertFalse(LocalMathEngine.isMathExpression("1"))
    }

    @Test
    fun testMathEngineValidScientificCalculations() {
        assertEquals("4", LocalMathEngine.evaluate("2 + 2"))
        assertEquals("512", LocalMathEngine.evaluate("128 * 4"))
        assertEquals("30", LocalMathEngine.evaluate("15% of 200"))
        assertEquals("4", LocalMathEngine.evaluate("sqrt(16)"))
        assertEquals("10", LocalMathEngine.evaluate("abs(-10)"))
    }

    @Test
    fun testSearchProviderControllerFallbackOnUnknown() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val fallback = SearchProvider.offlineSearchProvider(context)

        assertNotNull(fallback)
        assertEquals("App search", fallback.name)
        assertTrue(fallback.enabled)
    }

    @Test
    fun testAppGroupsManagerCategoryFallback() {
        val noneCategory = AppGroupsManager.Category.NONE
        assertNotNull(noneCategory)
        assertEquals("pref_drawer_no_categorization", noneCategory.key)
    }
}
