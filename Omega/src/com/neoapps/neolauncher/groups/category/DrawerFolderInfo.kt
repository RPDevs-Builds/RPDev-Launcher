/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.groups.category

import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.neoapps.neolauncher.compose.components.ComposeBottomSheet
import com.neoapps.neolauncher.groups.ui.EditGroupBottomSheet
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.prefs

class DrawerFolderInfo(val drawerFolder: DrawerFolders.Folder) : FolderInfo() {

    private var changed = false
    lateinit var appsStore: AllAppsStore

    init {
        id = drawerFolder.id.value().toInt()
        linkedDrawerFolderId = drawerFolder.id.value().toString()
    }

    override fun setTitle(title: CharSequence?, modelWriter: ModelWriter?) {
        super.setTitle(title, modelWriter)
        drawerFolder.title = title?.toString() ?: ""
    }

    override fun onIconChanged() {
        super.onIconChanged()
        drawerFolder.context.prefs.withChangeCallback {
            it.reloadGrid()
        }
    }

    fun syncToDrawerFolderAndWorkspace() {
        try {
            val newKeys = contents.filterIsInstance<com.android.launcher3.model.data.WorkspaceItemInfo>().mapNotNull { item ->
                item.targetComponent?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
                    ?: item.intent?.component?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
            }.toMutableSet()
            if (newKeys.isNotEmpty() || changed) {
                (drawerFolder as? DrawerFolders.CustomFolder)?.contents?.value = newKeys
                drawerFolder.context.prefs.drawerFolders.saveToJson()
                com.neoapps.neolauncher.groups.DrawerFolderSyncUtil.syncDrawerFolderToWorkspace(
                    context = drawerFolder.context,
                    folderId = drawerFolder.id.value(),
                    newTitle = drawerFolder.title,
                    newComponentKeys = newKeys
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onCloseComplete() {
        if (changed) {
            changed = false
            syncToDrawerFolderAndWorkspace()
        }
    }

    fun showEdit(launcher: Launcher) {
        val prefs = NeoPrefs.getInstance()
        ComposeBottomSheet.show(launcher) {
            EditGroupBottomSheet(
                category = prefs.drawerGroupsType!!,
                group = drawerFolder,
                onClose = { AbstractFloatingView.closeAllOpenViews(launcher) }
            )
        }
    }
}
