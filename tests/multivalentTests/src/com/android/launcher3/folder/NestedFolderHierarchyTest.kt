package com.android.launcher3.folder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_FOLDER
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit test for nested folders, cycle detection, and hierarchy navigation.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class NestedFolderHierarchyTest {

    @Test
    fun `FolderInfo accepts folder item type`() {
        assertTrue(FolderInfo.willAcceptItemType(ITEM_TYPE_FOLDER))
        assertTrue(FolderInfo.willAcceptItemType(ITEM_TYPE_APPLICATION))
    }

    @Test
    fun `FolderInfo adds nested folder cleanly`() {
        val parentFolder = FolderInfo().apply {
            id = 100
            title = "Parent"
        }
        val childFolder = FolderInfo().apply {
            id = 101
            title = "Child"
            container = 100
        }

        parentFolder.add(childFolder)
        assertEquals(1, parentFolder.contents.size)
        assertTrue(parentFolder.contents.contains(childFolder))
    }

    @Test
    fun `Cycle detection prevents self-nesting and circular hierarchies`() {
        val folderA = FolderInfo().apply {
            id = 1
            title = "A"
        }
        val folderB = FolderInfo().apply {
            id = 2
            title = "B"
            container = 1
        }
        val folderC = FolderInfo().apply {
            id = 3
            title = "C"
            container = 2
        }

        // Direct self-containment check
        assertTrue(folderA.wouldCreateCycle(folderA))

        // Normal nesting A -> B -> C
        folderA.add(folderB)
        folderB.add(folderC)
        assertFalse(folderA.wouldCreateCycle(FolderInfo().apply { id = 4 }))

        // Circular attempt: placing A into C (which is inside B which is inside A)
        assertTrue(folderC.wouldCreateCycle(folderA))
    }

    @Test
    fun `Nested folder contents preservation`() {
        val root = FolderInfo().apply { id = 10 }
        val nested = FolderInfo().apply { id = 20; container = 10 }
        val app = WorkspaceItemInfo().apply {
            id = 30
            title = "Test App"
            container = 20
        }

        nested.add(app)
        root.add(nested)

        assertEquals(1, root.contents.size)
        val extractedNested = root.contents[0] as FolderInfo
        assertEquals(20, extractedNested.id)
        assertEquals(1, extractedNested.contents.size)
        assertEquals(30, extractedNested.contents[0].id)
    }
}
