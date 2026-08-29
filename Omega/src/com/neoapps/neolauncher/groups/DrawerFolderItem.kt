/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.groups

import android.view.ViewGroup
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.folder.FolderIcon
import com.neoapps.neolauncher.groups.category.DrawerFolderInfo

class DrawerFolderItem(val info: DrawerFolderInfo) {

    private var icon: FolderIcon? = null

    fun getFolderIcon(launcher: Launcher, container: ViewGroup): FolderIcon {
        if (icon == null) {
            icon = FolderIcon.inflateFolderAndIcon(
                R.layout.all_apps_folder_icon, launcher,
                container, info
            )
        }
        return icon!!.apply {
            (parent as? ViewGroup)?.removeView(this)
            onItemsChanged(false)
        }
    }
}

object DrawerFolderSyncUtil {

    private var isSyncing = false

    /**
     * Synchronizes updates made to a Drawer Folder (in All Apps / Settings)
     * to all linked FolderInfo instances on the homescreen workspace.
     */
    @JvmStatic
    fun syncDrawerFolderToWorkspace(
        context: android.content.Context,
        folderId: Long,
        newTitle: String,
        newComponentKeys: Collection<com.android.launcher3.util.ComponentKey>,
    ) {
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            com.android.launcher3.util.Executors.MAIN_EXECUTOR.execute {
                syncDrawerFolderToWorkspace(context, folderId, newTitle, newComponentKeys)
            }
            return
        }

        if (isSyncing) return
        try {
            isSyncing = true
            val launcher = Launcher.ACTIVITY_TRACKER.getCreatedContext<Launcher>()
                ?: (context as? Launcher)

            val appState = com.android.launcher3.LauncherAppState.getInstance(context)
            val model = appState.model
            val modelWriter = launcher?.modelWriter ?: model.getWriter(false, null, null)
            val appsStore = launcher?.appsView?.appsStore

            val targetIdStr = folderId.toString().trim()
            val linkedFolderMap = mutableMapOf<com.android.launcher3.model.data.FolderInfo, FolderIcon?>()

            // 1. Check workspace views
            launcher?.workspace?.mapOverItems { info, view ->
                if (info is com.android.launcher3.model.data.FolderInfo) {
                    val idMatch = !info.linkedDrawerFolderId.isNullOrBlank() && info.linkedDrawerFolderId?.trim() == targetIdStr
                    val titleMatch = !newTitle.isNullOrBlank() && info.title?.toString()?.trim()?.equals(newTitle.trim(), ignoreCase = true) == true
                    if (idMatch || titleMatch) {
                        linkedFolderMap[info] = view as? FolderIcon
                    }
                }
                false
            }

            for ((folderInfo, folderIcon) in linkedFolderMap) {
                // Ensure link ID is set if it was matched by title
                if (folderInfo.linkedDrawerFolderId?.trim() != targetIdStr) {
                    folderInfo.linkedDrawerFolderId = targetIdStr
                    modelWriter.updateItemInDatabase(folderInfo)
                }

                // 1. Sync title if changed
                if (folderInfo.title?.toString() != newTitle) {
                    folderInfo.setTitle(newTitle, modelWriter)
                }

                val folderView = folderIcon?.folder

                // 2. Identify items to remove
                val existingItems = ArrayList(folderInfo.contents)
                val currentKeys = mutableSetOf<com.android.launcher3.util.ComponentKey>()

                for (item in existingItems) {
                    if (item is com.android.launcher3.model.data.WorkspaceItemInfo) {
                        val key = item.targetComponent?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
                            ?: item.intent?.component?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
                        if (key != null) {
                            if (!newComponentKeys.contains(key)) {
                                modelWriter.deleteItemFromDatabase(item, "syncDrawerFolder")
                                if (folderView != null) {
                                    folderView.removeFolderContent(true, item)
                                } else {
                                    folderInfo.contents.remove(item)
                                }
                            } else {
                                currentKeys.add(key)
                            }
                        }
                    }
                }

                // 3. Identify items to add
                for (compKey in newComponentKeys) {
                    if (!currentKeys.contains(compKey)) {
                        val appInfo = appsStore?.getApp(compKey)
                            ?: appsStore?.apps?.find { it.toComponentKey() == compKey }
                        val wii: com.android.launcher3.model.data.WorkspaceItemInfo
                        if (appInfo != null) {
                            wii = appInfo.makeWorkspaceItem(context)
                        } else {
                            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                                .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                                .setComponent(compKey.componentName)
                                .setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            wii = com.android.launcher3.model.data.WorkspaceItemInfo()
                            wii.intent = intent
                            wii.user = compKey.user
                            wii.itemType = com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
                            val pm = context.packageManager
                            val activityInfo = pm.resolveActivity(intent, 0)?.activityInfo
                            if (activityInfo != null) {
                                wii.title = activityInfo.loadLabel(pm)
                            }
                        }
                        wii.container = folderInfo.id
                        if (folderView != null) {
                            folderView.addFolderContent(wii)
                        } else {
                            folderInfo.add(wii)
                            modelWriter.addItemToDatabase(wii, folderInfo.id, 0, wii.cellX, wii.cellY)
                        }
                    }
                }

                // 4. Re-sort and notify UI
                com.neoapps.neolauncher.folder.FolderSortUtil.sortFolder(folderInfo, context, modelWriter)
                folderInfo.onIconChanged()
                folderIcon?.onItemsChanged(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSyncing = false
        }
    }

    /**
     * Synchronizes updates made to a Workspace Folder (adding/removing apps on homescreen)
     * back to the corresponding Drawer Folder in All Apps.
     */
    @JvmStatic
    @JvmOverloads
    fun syncWorkspaceFolderToDrawer(
        context: android.content.Context,
        linkedDrawerFolderId: String?,
        folderTitle: String? = null,
        currentContents: List<com.android.launcher3.model.data.ItemInfo>,
    ) {
        if (isSyncing) return
        try {
            isSyncing = true
            val prefs = com.neoapps.neolauncher.preferences.NeoPrefs.getInstance()
            val groups = prefs.drawerFolders.getGroups(isFolder = true)
            val matchingFolder = groups.find {
                (!linkedDrawerFolderId.isNullOrBlank() && it.id.value().toString() == linkedDrawerFolderId.trim())
                    || (!folderTitle.isNullOrBlank() && it.title.equals(folderTitle.trim(), ignoreCase = true))
            } as? com.neoapps.neolauncher.groups.category.DrawerFolders.CustomFolder ?: return

            val newKeys = currentContents.filterIsInstance<com.android.launcher3.model.data.WorkspaceItemInfo>().mapNotNull { item ->
                item.targetComponent?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
                    ?: item.intent?.component?.let { com.android.launcher3.util.ComponentKey(it, item.user) }
            }.toMutableSet()

            if (matchingFolder.contents.value != newKeys) {
                matchingFolder.contents.value = newKeys
                prefs.drawerFolders.saveToJson()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSyncing = false
        }
    }
}
