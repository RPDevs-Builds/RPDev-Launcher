package com.android.launcher3

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import iamrp.dev.launcher.backup.FileInfo
import iamrp.dev.launcher.data.models.GestureItemInfo
import iamrp.dev.launcher.util.hasFlag
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for gesture models, backup metadata, and flags.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class BackupManagerAndGesturesTest {

    @Test
    fun `FileInfo serialization and deserialization retains metadata`() {
        val original = FileInfo(
            version = 2,
            launcherVersion = "3.0-alpha",
            launcherBuild = 100,
            flags = FileInfo.FLAG_HOMESCREEN or FileInfo.FLAG_SETTINGS or FileInfo.FLAG_WALLPAPER,
            device = "Pixel 8 Pro",
            theme = "System",
            date = 1725235200000L
        )

        val jsonString = original.toString()
        val parsed = FileInfo.fromString(jsonString)

        assertEquals(original.version, parsed.version)
        assertEquals(original.launcherVersion, parsed.launcherVersion)
        assertEquals(original.launcherBuild, parsed.launcherBuild)
        assertEquals(original.flags, parsed.flags)
        assertEquals(original.device, parsed.device)
        assertEquals(original.theme, parsed.theme)
        assertEquals(original.date, parsed.date)
    }

    @Test
    fun `FileInfo flags validation`() {
        val flags = FileInfo.FLAG_HOMESCREEN or FileInfo.FLAG_SETTINGS
        assertTrue(flags.hasFlag(FileInfo.FLAG_HOMESCREEN))
        assertTrue(flags.hasFlag(FileInfo.FLAG_SETTINGS))
        assertFalse(flags.hasFlag(FileInfo.FLAG_WALLPAPER))
    }

    @Test
    fun `GestureItemInfo data integrity`() {
        val key = com.android.launcher3.util.ComponentKey(
            android.content.ComponentName("com.google.android.calculator", "com.android.calculator2.Calculator"),
            android.os.Process.myUserHandle()
        )
        val gestureItem = GestureItemInfo(
            key = key,
            swipeUp = "action_open_app",
            swipeDown = "action_notifications"
        )

        assertEquals(key, gestureItem.key)
        assertEquals("action_open_app", gestureItem.swipeUp)
        assertEquals("action_notifications", gestureItem.swipeDown)
    }
}
