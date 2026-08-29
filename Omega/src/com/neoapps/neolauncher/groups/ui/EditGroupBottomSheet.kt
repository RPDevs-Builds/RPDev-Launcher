/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.neoapps.neolauncher.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.ComponentName
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.neoapps.neolauncher.data.IconOverrideRepository
import com.neoapps.neolauncher.iconpack.IconPackProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.folder.FolderIcon
import com.android.launcher3.model.data.FolderInfo
import com.neoapps.neolauncher.preferences.PreferenceActivity
import com.neoapps.neolauncher.compose.navigation.Routes
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.DialogPositiveButton
import com.neoapps.neolauncher.compose.components.preferences.BasePreference
import com.neoapps.neolauncher.compose.pages.ColorSelectionDialog
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.groups.AppGroups
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.category.DrawerFolders
import com.neoapps.neolauncher.groups.category.DrawerTabs
import com.neoapps.neolauncher.groups.category.FlowerpotTabs.Companion.KEY_FLOWERPOT
import com.neoapps.neolauncher.groups.category.FlowerpotTabs.Companion.TYPE_FLOWERPOT
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.theme.AccentColorOption
import com.neoapps.neolauncher.util.Config
import com.neoapps.neolauncher.util.prefs

@Composable
fun EditGroupBottomSheet(
    category: AppGroupsManager.Category,
    group: AppGroups.Group,
    onClose: (Int) -> Unit,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    val flowerpotManager = Flowerpot.Manager.getInstance(context)
    val config = group.customizations
    val keyboardController = LocalSoftwareKeyboardController.current
    val openDialog = remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(group.title) }

    var cornerRadius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        cornerRadius = prefs.profileWindowCornerRadius.getValue().dp
    }

    var isHidden by remember {
        mutableStateOf(
            (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value != false
        )
    }

    val colorPicker = remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(
            (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value
                ?: AppGroups.KEY_FLOWERPOT_DEFAULT
        )
    }
    val allAppsTab = "profile{\"matchesAll\":true}}"

    val apps: Array<ComponentKey> = if (group.type == allAppsTab) {
        prefs.drawerHiddenAppSet.getValue().map { ComponentKey.fromString(it)!! }.toTypedArray()
    } else {
        (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value?.toTypedArray()
            ?: emptyArray()
    }

    val folderId = remember(group) {
        (group as? DrawerFolders.Folder)?.id?.value()?.toInt() ?: group.title.hashCode()
    }
    val folderCompKey = remember(folderId) {
        ComponentKey(
            ComponentName("com.neoapps.neolauncher.folder", "folder_$folderId"),
            Process.myUserHandle()
        )
    }
    val repo = IconOverrideRepository.INSTANCE.get(context)
    val overrideItem by repo.observeTarget(folderCompKey).collectAsState(initial = null)
    val ipp = IconPackProvider.INSTANCE.get(context)
    val currentCustomIcon = remember(overrideItem) {
        overrideItem?.iconPickerItem?.let { item ->
            try {
                ipp.getDrawable(item.toIconEntry(), 0, Process.myUserHandle())
            } catch (e: Exception) {
                null
            }
        }
    }
    val selectedApps = remember { mutableStateListOf(*apps) }
    val editIconRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            context.prefs.withChangeCallback { it.reloadGrid() }
        }
    var color by remember {
        mutableStateOf(
            (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value
                ?: context.prefs.profileAccentColor.getValue()
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (category == AppGroupsManager.Category.FOLDER) {
            item {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            editIconRequest.launch(
                                PreferenceActivity.navigateIntent(context, "${Routes.EDIT_ICON}/$folderCompKey")
                            )
                        }
                ) {
                    if (currentCustomIcon != null) {
                        Image(
                            painter = rememberDrawablePainter(currentCustomIcon),
                            contentDescription = title,
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_folder_outline),
                            contentDescription = title,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (overrideItem != null) {
                    Text(
                        text = stringResource(R.string.reset_custom_icon),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    repo.deleteOverride(folderCompKey)
                                    context.prefs.withChangeCallback { it.reloadGrid() }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
                shape = MaterialTheme.shapes.large,
                label = {
                    Text(
                        text = stringResource(id = R.string.name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                isError = title.isEmpty()
            )
        }
        val summary = context.resources.getQuantityString(
            R.plurals.tab_apps_count,
            selectedApps.size,
            selectedApps.size
        )
        item {
            when (group.type) {
                allAppsTab -> {
                    BasePreference(
                        titleId = R.string.title__drawer_hide_apps,
                        summary = summary,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_apps),
                                contentDescription = null,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                            )
                        },
                        index = 0,
                        groupSize = 2
                    ) { openDialog.value = true }

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                GroupAppSelection(
                                    selectedApps = selectedApps.map { it.toString() }.toSet(),
                                ) {
                                    val componentsSet =
                                        it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                            .toMutableSet()
                                    selectedApps.clear()
                                    selectedApps.addAll(componentsSet)
                                    prefs.drawerHiddenAppSet.setValue(selectedApps.map { key -> key.toString() }
                                        .toSet())
                                    openDialog.value = false
                                }
                            }
                        }
                    }
                }

                TYPE_FLOWERPOT -> {
                    BasePreference(
                        titleId = R.string.pref_appcategorization_flowerpot_title,
                        summary = flowerpotManager.getAllPots()
                            .find { it.name == selectedCategory }?.displayName ?: selectedCategory,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_category),
                                contentDescription = null,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                            )
                        },
                        index = 0,
                        groupSize = 2
                    ) { openDialog.value = true }

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                FlowerpotCategoryPage(selectedCategory = selectedCategory) {
                                selectedCategory = it
                                (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value =
                                    it
                                openDialog.value = false
                            }
                            }
                        }
                    }
                }

                DrawerTabs.TYPE_CUSTOM, DrawerFolders.TYPE_CUSTOM -> {
                    BasePreference(
                        titleId = R.string.tab_manage_apps,
                        summary = summary,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_apps),
                                contentDescription = null,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                            )
                        },
                        index = 0,
                        groupSize = 3
                    ) { openDialog.value = true }
                    Spacer(modifier = Modifier.height(4.dp))
                    BasePreference(
                        titleId = R.string.tab_hide_from_main,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.tab_hide_from_main),
                                contentDescription = null,
                            )
                        },
                        endWidget = {
                            Switch(
                                modifier = Modifier
                                    .height(24.dp),
                                checked = isHidden,
                                onCheckedChange = {
                                    isHidden = it
                                }
                            )
                        },
                        onClick = { isHidden = !isHidden },
                        index = 1,
                        groupSize = 3
                    )

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                GroupAppSelection(
                                    selectedApps = selectedApps.map { it.toString() }
                                        .toSet(),
                                ) {
                                    val componentsSet =
                                        it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                            .toMutableSet()
                                    selectedApps.clear()
                                    selectedApps.addAll(componentsSet)
                                    (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                                        componentsSet
                                }
                            }
                        }
                    }
                }
            }
            if (group.type != DrawerFolders.TYPE_CUSTOM) {
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.tab_color,
                    startWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_color_donut),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp),
                            tint = Color(AccentColorOption.fromString(color).accentColor)
                        )
                    },
                    index = 2,
                    groupSize = 3
                ) {
                    colorPicker.value = true
                }
                if (colorPicker.value) {
                    BaseDialog(openDialogCustom = colorPicker) {
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.padding(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            ColorSelectionDialog(
                                defaultColor = color,
                                onCancel = {
                                    colorPicker.value = false
                                },
                                onSave = {
                                    color = it
                                    colorPicker.value = false
                                }
                            )
                        }
                    }
                }
            }
            if (category == AppGroupsManager.Category.FOLDER) {
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.drawer_folder_custom_icon,
                    summary = if (overrideItem != null) stringResource(R.string.custom_icon_applied) else stringResource(R.string.default_icon),
                    startWidget = {
                        if (currentCustomIcon != null) {
                            Image(
                                painter = rememberDrawablePainter(currentCustomIcon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_folder_outline),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    endWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        editIconRequest.launch(
                            PreferenceActivity.navigateIntent(context, "${Routes.EDIT_ICON}/$folderCompKey")
                        )
                    },
                    index = 2,
                    groupSize = 4
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.drawer_folder_add_to_home,
                    startWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_build),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        try {
                            val launcher = Launcher.ACTIVITY_TRACKER.getCreatedContext<Launcher>() ?: (context as? Launcher)
                            if (launcher != null) {
                                val workspace = launcher.workspace
                                if (workspace != null) {
                                    val page = workspace.currentPage
                                    val cellLayout = workspace.getChildAt(page) as? com.android.launcher3.CellLayout
                                    val screenId = workspace.getScreenIdForPageIndex(page)
                                    if (cellLayout != null) {
                                        val targetCell = IntArray(2)
                                        if (cellLayout.findCellForSpan(targetCell, 1, 1)) {
                                            val fi = launcher.addFolder(cellLayout, LauncherSettings.Favorites.CONTAINER_DESKTOP, screenId, targetCell[0], targetCell[1])
                                            fi.folderInfo.setTitle(title, launcher.modelWriter)
                                            val folderId = (group as? DrawerFolders.Folder)?.id?.value()?.toString()
                                            if (!folderId.isNullOrBlank()) {
                                                fi.folderInfo.linkedDrawerFolderId = folderId
                                                launcher.modelWriter.updateItemInDatabase(fi.folderInfo)
                                            }
                                            val appsStore = launcher.appsView?.appsStore
                                            selectedApps.forEach { compKey ->
                                                val appInfo = appsStore?.getApp(compKey)
                                                    ?: appsStore?.apps?.find { it.toComponentKey() == compKey }
                                                if (appInfo != null) {
                                                    val wii = appInfo.makeWorkspaceItem(launcher)
                                                    fi.folder.addFolderContent(wii)
                                                }
                                            }
                                            android.widget.Toast.makeText(context, R.string.drawer_folder_added_to_home, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            onClose(Config.BS_NONE)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    index = 3,
                    groupSize = 4
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { onClose(Config.BS_SELECT_TAB_TYPE) }
                )
                DialogPositiveButton(
                    cornerRadius = cornerRadius,
                    textId = R.string.tab_bottom_sheet_save,
                    onClick = {
                        (config[AppGroups.KEY_TITLE] as? AppGroups.StringCustomization)?.value =
                            title
                        if (group.type == TYPE_FLOWERPOT) {
                            (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value =
                                selectedCategory
                        } else {
                            (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value =
                                isHidden
                            (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                                selectedApps.toMutableSet()
                        }
                        if (category != AppGroupsManager.Category.FOLDER) {
                            (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value =
                                color
                        }
                        group.customizations.applyFrom(config)

                        when (category) {
                            AppGroupsManager.Category.FOLDER -> {
                                prefs.drawerFolders.saveToJson()
                                val folderId = (group as? DrawerFolders.Folder)?.id?.value()
                                if (folderId != null) {
                                    com.neoapps.neolauncher.groups.DrawerFolderSyncUtil.syncDrawerFolderToWorkspace(
                                        context = context,
                                        folderId = folderId,
                                        newTitle = title,
                                        newComponentKeys = selectedApps
                                    )
                                }
                            }

                            AppGroupsManager.Category.TAB,
                            AppGroupsManager.Category.FLOWERPOT,
                                                             -> {
                                prefs.drawerTabs.saveToJson()
                                prefs.reloadTabs()
                            }

                            else                             -> {}
                        }
                        onClose(Config.BS_SELECT_TAB_TYPE)
                    }
                )
            }
        }
    }
}
