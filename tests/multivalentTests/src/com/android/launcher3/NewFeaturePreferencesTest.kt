/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package com.android.launcher3

import iamrp.dev.launcher.preferences.PrefKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NewFeaturePreferencesTest {

    @Test
    fun testDesktopSubgridPrefKey() {
        assertNotNull(PrefKey.DESKTOP_SUBGRID)
        assertEquals("desktop_subgrid", PrefKey.DESKTOP_SUBGRID.name)
    }

    @Test
    fun testDockSearchBarPrefKey() {
        assertNotNull(PrefKey.DOCK_SEARCHBAR_ENABLED)
        assertEquals("dock_searchbar_enabled", PrefKey.DOCK_SEARCHBAR_ENABLED.name)
    }
}
