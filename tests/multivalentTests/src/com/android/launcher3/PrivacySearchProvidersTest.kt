/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package com.android.launcher3

import androidx.test.core.app.ApplicationProvider
import iamrp.dev.launcher.search.SearchProviderController
import iamrp.dev.launcher.search.providers.BraveSearchProvider
import iamrp.dev.launcher.search.providers.DuckDuckGoSearchProvider
import iamrp.dev.launcher.search.providers.SearXNGSearchProvider
import iamrp.dev.launcher.search.providers.StartpageSearchProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacySearchProvidersTest {

    @Test
    fun testBraveSearchProviderConfig() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val provider = BraveSearchProvider(context)

        assertEquals("Brave Search", provider.name)
        assertEquals(1010L, provider.id)
        assertEquals("com.brave.browser", provider.packageName)
        assertFalse(provider.supportsVoiceSearch)
        assertFalse(provider.supportsAssistant)
        assertFalse(provider.supportsFeed)
        assertNotNull(provider.icon)
    }

    @Test
    fun testStartpageSearchProviderConfig() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val provider = StartpageSearchProvider(context)

        assertEquals("Startpage", provider.name)
        assertEquals(1011L, provider.id)
        assertEquals("com.startpage.mobile", provider.packageName)
        assertFalse(provider.supportsVoiceSearch)
        assertFalse(provider.supportsAssistant)
        assertNotNull(provider.icon)
    }

    @Test
    fun testSearXNGSearchProviderConfig() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val provider = SearXNGSearchProvider(context)

        assertEquals("SearXNG", provider.name)
        assertEquals(1012L, provider.id)
        assertEquals("org.searxng", provider.packageName)
        assertFalse(provider.supportsVoiceSearch)
        assertFalse(provider.supportsAssistant)
        assertNotNull(provider.icon)
    }

    @Test
    fun testDuckDuckGoSearchProviderConfig() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val provider = DuckDuckGoSearchProvider(context)

        assertEquals(1003L, provider.id)
        assertEquals("com.duckduckgo.mobile.android", provider.packageName)
        assertFalse(provider.supportsVoiceSearch)
        assertNotNull(provider.icon)
    }

    @Test
    fun testSearchProviderControllerListIncludesPrivacyEngines() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val providers = SearchProviderController.getAppSearchProviders(context)

        val providerNames = providers.map { it.name }
        assertTrue(providerNames.contains("Brave Search"))
        assertTrue(providerNames.contains("Startpage"))
        assertTrue(providerNames.contains("SearXNG"))
    }
}
