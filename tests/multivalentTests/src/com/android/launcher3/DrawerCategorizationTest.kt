/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package com.android.launcher3

import iamrp.dev.launcher.allapps.FILTER_CATEGORY_ALL
import iamrp.dev.launcher.allapps.appCategoryIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DrawerCategorizationTest {

    @Test
    fun testCategoryFilterConstants() {
        assertEquals("All", FILTER_CATEGORY_ALL)
    }

    @Test
    fun testCategoryIconResolution() {
        assertNotNull(FILTER_CATEGORY_ALL.appCategoryIcon)
        assertNotNull("game".appCategoryIcon)
        assertNotNull("communication".appCategoryIcon)
        assertNotNull("tools".appCategoryIcon)
        assertNotNull("music".appCategoryIcon)
        assertNotNull("photography".appCategoryIcon)
        assertNotNull("education".appCategoryIcon)
        assertNotNull("unknown_category".appCategoryIcon)
    }
}
