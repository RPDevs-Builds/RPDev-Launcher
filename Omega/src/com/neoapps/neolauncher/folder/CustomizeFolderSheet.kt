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

package com.neoapps.neolauncher.folder

import android.app.Activity
import android.content.Intent
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.groups.ui.GroupAppSelection
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import com.android.launcher3.LauncherSettings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.neoapps.neolauncher.compose.components.ComposeSwitchView
import com.neoapps.neolauncher.compose.components.preferences.PreferenceItem
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.data.IconOverrideRepository
import com.neoapps.neolauncher.iconpack.IconPackProvider
import com.neoapps.neolauncher.preferences.PreferenceActivity
import kotlinx.coroutines.flow.flowOf

@Composable
fun CustomizeFolderSheet(
    launcher: Launcher,
    folder: FolderInfo,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    val infoProvider: CustomInfoProvider<ItemInfo> =
        CustomInfoProvider.forItem(context, folder)

    var title by remember { mutableStateOf("") }
    val defaultTitle by remember { mutableStateOf("") }

    DisposableEffect(key1 = null) {
        title = folder.title?.toString() ?: defaultTitle
        onDispose {
            val previousTitle = infoProvider.getCustomTitle(folder)
            val newTitle = if (title != defaultTitle) title else null
            if (newTitle != previousTitle) {
                //folder.setTitle(newTitle)
            }
            val model = LauncherAppState.getInstance(context).model
            /*model.onPackageChanged(
                folder.toComponentKey().componentName.toString(),
                folder.toComponentKey().user
            )*/
        }
    }

    CustomizeFolderView(
        launcher = launcher,
        folder = folder,
        title = title,
        onTitleChange = { title = it },
        defaultTitle = defaultTitle,
        onClose = onClose,
    )
}

@Composable
fun CustomizeFolderView(
    launcher: Launcher,
    folder: FolderInfo,
    title: String,
    onTitleChange: (String) -> Unit,
    defaultTitle: String,
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = IconOverrideRepository.INSTANCE.get(context)
    val coverMode = remember { mutableStateOf(folder.isCoverMode) }
    val reverseActions = remember { mutableStateOf(folder.isCoverReverseActions) }
    val showFolderName = remember { mutableStateOf(folder.isCoverShowFolderName) }
    val indicatorStyle = remember { mutableIntStateOf(folder.coverIndicatorStyle) }
    val sortMode = remember { mutableIntStateOf(folder.sortMode) }
    val previewStyle = remember { mutableIntStateOf(folder.previewStyle) }
    var selectedCoverItem by remember { mutableStateOf(folder.coverInfo) }
    var openCoverAppDialog by remember { mutableStateOf(false) }

    val folderComponentKey = remember(folder.id) {
        folder.folderComponentKey
    }
    val overrideItem by repo.observeTarget(folderComponentKey).collectAsState(initial = null)

    val editIconRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            folder.onIconChanged()
            if (it.resultCode == Activity.RESULT_OK) {
                onClose()
            }
        }

    val openEditIcon = {
        editIconRequest.launch(
            PreferenceActivity.navigateIntent(context, "${Routes.EDIT_ICON}/$folderComponentKey")
        )
    }

    val baseIcon = if (coverMode.value && selectedCoverItem != null) {
        selectedCoverItem?.newIcon(context, BitmapInfo.FLAG_THEMED)
    } else {
        folder.getFolderIcon(launcher)
    }

    val ipp = IconPackProvider.INSTANCE.get(context)
    val currentIcon = remember(overrideItem, baseIcon) {
        overrideItem?.iconPickerItem?.let { item ->
            try {
                ipp.getDrawable(item.toIconEntry(), 0, folder.user) ?: baseIcon
            } catch (e: Exception) {
                baseIcon
            }
        } ?: baseIcon
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.width(48.dp),
            thickness = 2.dp,
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                    openEditIcon()
                }
        ) {
            Image(
                painter = rememberDrawablePainter(currentIcon),
                contentDescription = title,
                modifier = Modifier
                    .requiredSize(64.dp)
            )
        }

        if (overrideItem != null) {
            Text(
                text = stringResource(R.string.reset_custom_icon),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        scope.launch {
                            repo.deleteOverride(folderComponentKey)
                            folder.onIconChanged()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        if (!coverMode.value || showFolderName.value) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.folder_hint_text)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val groupSize = if (coverMode.value) 5 else 1

            ComposeSwitchView(
                title = stringResource(R.string.folder_cover_mode),
                summary = stringResource(R.string.folder_cover_mode_desc),
                isChecked = folder.isCoverMode,
                index = 0,
                groupSize = groupSize,
                onChange = { newValue ->
                    folder.setCoverMode(newValue, launcher.modelWriter)
                    coverMode.value = newValue
                }
            )

            if (coverMode.value) {
                ComposeSwitchView(
                    title = stringResource(R.string.folder_cover_reverse_actions),
                    summary = stringResource(R.string.folder_cover_reverse_actions_desc),
                    isChecked = reverseActions.value,
                    index = 1,
                    groupSize = groupSize,
                    onChange = { newValue ->
                        folder.setCoverReverseActions(newValue, launcher.modelWriter)
                        reverseActions.value = newValue
                    }
                )

                ComposeSwitchView(
                    title = stringResource(R.string.folder_cover_show_folder_title),
                    summary = stringResource(R.string.folder_cover_show_folder_title_desc),
                    isChecked = showFolderName.value,
                    index = 2,
                    groupSize = groupSize,
                    onChange = { newValue ->
                        folder.setCoverShowFolderName(newValue, launcher.modelWriter)
                        showFolderName.value = newValue
                    }
                )

                var indicatorMenuExpanded by remember { mutableStateOf(false) }

                val indicatorSummary = when (indicatorStyle.intValue) {
                    FolderInfo.COVER_INDICATOR_NOTCH -> stringResource(R.string.folder_cover_indicator_notch)
                    FolderInfo.COVER_INDICATOR_DOT -> stringResource(R.string.folder_cover_indicator_dot)
                    FolderInfo.COVER_INDICATOR_COUNT -> stringResource(R.string.folder_cover_indicator_count)
                    else -> stringResource(R.string.folder_cover_indicator_none)
                }

                Box {
                    PreferenceItem(
                        title = stringResource(R.string.folder_cover_indicator),
                        summary = indicatorSummary,
                        index = 3,
                        groupSize = groupSize,
                        onClick = {
                            indicatorMenuExpanded = true
                        }
                    )

                    DropdownMenu(
                        expanded = indicatorMenuExpanded,
                        onDismissRequest = { indicatorMenuExpanded = false }
                    ) {
                        val styles = listOf(
                            FolderInfo.COVER_INDICATOR_NONE to stringResource(R.string.folder_cover_indicator_none),
                            FolderInfo.COVER_INDICATOR_NOTCH to stringResource(R.string.folder_cover_indicator_notch),
                            FolderInfo.COVER_INDICATOR_DOT to stringResource(R.string.folder_cover_indicator_dot),
                            FolderInfo.COVER_INDICATOR_COUNT to stringResource(R.string.folder_cover_indicator_count),
                        )
                        styles.forEach { (style, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        RadioButton(
                                            selected = (indicatorStyle.intValue == style),
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    folder.setCoverIndicatorStyle(style, launcher.modelWriter)
                                    indicatorStyle.intValue = style
                                    indicatorMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                PreferenceItem(
                    title = stringResource(R.string.folder_cover_select_app),
                    summary = selectedCoverItem?.title?.toString()
                        ?: folder.coverInfo?.title?.toString()
                        ?: stringResource(R.string.folder_cover_default_app),
                    index = 4,
                    groupSize = groupSize,
                    onClick = {
                        openCoverAppDialog = true
                    }
                )
            }
        }

        // Folder Preview Style Section (when not in Cover Mode)
        if (!coverMode.value) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val previewSummary = when (previewStyle.intValue) {
                    FolderInfo.PREVIEW_STYLE_RADIAL -> stringResource(R.string.folder_preview_radial)
                    FolderInfo.PREVIEW_STYLE_STACKED -> stringResource(R.string.folder_preview_stacked)
                    FolderInfo.PREVIEW_STYLE_FAN -> stringResource(R.string.folder_preview_fan)
                    else -> stringResource(R.string.folder_preview_grid)
                }

                var previewMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    PreferenceItem(
                        title = stringResource(R.string.folder_preview_style),
                        summary = previewSummary,
                        index = 0,
                        groupSize = 1,
                        onClick = {
                            previewMenuExpanded = true
                        }
                    )

                    DropdownMenu(
                        expanded = previewMenuExpanded,
                        onDismissRequest = { previewMenuExpanded = false }
                    ) {
                        val previewOptions = listOf(
                            FolderInfo.PREVIEW_STYLE_GRID to stringResource(R.string.folder_preview_grid),
                            FolderInfo.PREVIEW_STYLE_RADIAL to stringResource(R.string.folder_preview_radial),
                            FolderInfo.PREVIEW_STYLE_STACKED to stringResource(R.string.folder_preview_stacked),
                            FolderInfo.PREVIEW_STYLE_FAN to stringResource(R.string.folder_preview_fan),
                        )
                        previewOptions.forEach { (style, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        RadioButton(
                                            selected = (previewStyle.intValue == style || (style == FolderInfo.PREVIEW_STYLE_GRID && previewStyle.intValue == FolderInfo.PREVIEW_STYLE_DEFAULT)),
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    folder.setPreviewStyle(style, launcher.modelWriter)
                                    previewStyle.intValue = style
                                    previewMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Folder Sorting Section
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val sortSummary = when (sortMode.intValue) {
                FolderInfo.SORT_AZ -> stringResource(R.string.folder_sort_az)
                FolderInfo.SORT_ZA -> stringResource(R.string.folder_sort_za)
                FolderInfo.SORT_MOST_USED -> stringResource(R.string.folder_sort_most_used)
                FolderInfo.SORT_BY_INSTALL_DATE -> stringResource(R.string.folder_sort_install_date)
                FolderInfo.SORT_BY_COLOR -> stringResource(R.string.folder_sort_color)
                else -> stringResource(R.string.folder_sort_manual)
            }

            var sortMenuExpanded by remember { mutableStateOf(false) }

            Box {
                PreferenceItem(
                    title = stringResource(R.string.folder_sort_by),
                    summary = sortSummary,
                    index = 0,
                    groupSize = 1,
                    onClick = {
                        sortMenuExpanded = true
                    }
                )

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    val sortOptions = listOf(
                        FolderInfo.SORT_MANUAL to stringResource(R.string.folder_sort_manual),
                        FolderInfo.SORT_AZ to stringResource(R.string.folder_sort_az),
                        FolderInfo.SORT_ZA to stringResource(R.string.folder_sort_za),
                        FolderInfo.SORT_MOST_USED to stringResource(R.string.folder_sort_most_used),
                        FolderInfo.SORT_BY_INSTALL_DATE to stringResource(R.string.folder_sort_install_date),
                        FolderInfo.SORT_BY_COLOR to stringResource(R.string.folder_sort_color),
                    )
                    sortOptions.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    RadioButton(
                                        selected = (sortMode.intValue == mode),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            },
                            onClick = {
                                folder.setSortMode(mode, launcher.modelWriter)
                                sortMode.intValue = mode
                                FolderSortUtil.sortFolder(folder, context, launcher.modelWriter)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Manage Apps Section
        var openManageAppsDialog by remember { mutableStateOf(false) }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val appsCountSummary = context.resources.getQuantityString(
                R.plurals.tab_apps_count,
                folder.contents.size,
                folder.contents.size
            )
            PreferenceItem(
                title = stringResource(R.string.folder_manage_apps),
                summary = appsCountSummary,
                index = 0,
                groupSize = 1,
                onClick = {
                    openManageAppsDialog = true
                }
            )
        }

        if (openManageAppsDialog) {
            val initialSelected = remember(folder.contents.size) {
                folder.contents.filterIsInstance<WorkspaceItemInfo>().mapNotNull {
                    val comp = it.targetComponent ?: it.intent?.component
                    if (comp != null) ComponentKey(comp, it.user).toString() else null
                }.toSet()
            }
            BaseDialog(openDialogCustom = remember { mutableStateOf(true) }) {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.padding(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    GroupAppSelection(
                        selectedApps = initialSelected,
                    ) { newSelectedKeys ->
                        val currentItems = ArrayList(folder.contents)
                        val currentMap = mutableMapOf<String, WorkspaceItemInfo>()
                        currentItems.filterIsInstance<WorkspaceItemInfo>().forEach { item ->
                            val comp = item.targetComponent ?: item.intent?.component
                            if (comp != null) {
                                currentMap[ComponentKey(comp, item.user).toString()] = item
                            }
                        }

                        val keysToRemove = currentMap.keys - newSelectedKeys
                        val keysToAdd = newSelectedKeys - currentMap.keys

                        keysToRemove.forEach { keyStr ->
                            val item = currentMap[keyStr]
                            if (item != null) {
                                launcher.modelWriter.deleteItemFromDatabase(item, "manage apps")
                                folder.contents.remove(item)
                            }
                        }

                        if (keysToAdd.isNotEmpty()) {
                            val appsStore = launcher.appsView?.appsStore
                            keysToAdd.forEach { keyStr ->
                                val key = ComponentKey.fromString(keyStr)
                                if (key != null) {
                                    val appInfo = appsStore?.getApp(key)
                                        ?: appsStore?.apps?.find { it.toComponentKey() == key }
                                    if (appInfo != null) {
                                        val wii = appInfo.makeWorkspaceItem(context)
                                        wii.container = folder.id
                                        folder.add(wii)
                                        launcher.modelWriter.addItemToDatabase(wii, folder.id, 0, wii.cellX, wii.cellY)
                                    }
                                }
                            }
                        }

                        if (folder is com.neoapps.neolauncher.groups.category.DrawerFolderInfo) {
                            val newKeys = newSelectedKeys.mapNotNull { ComponentKey.fromString(it) }.toMutableSet()
                            (folder.drawerFolder as? com.neoapps.neolauncher.groups.category.DrawerFolders.CustomFolder)?.contents?.value = newKeys
                            com.neoapps.neolauncher.preferences.NeoPrefs.getInstance().drawerFolders.saveToJson()
                        } else if (!folder.linkedDrawerFolderId.isNullOrBlank()) {
                            val newKeys = newSelectedKeys.mapNotNull { ComponentKey.fromString(it) }.toMutableSet()
                            val drawerFolder = com.neoapps.neolauncher.preferences.NeoPrefs.getInstance().drawerFolders
                                .getGroups(isFolder = true)
                                .find { it.id.value().toString() == folder.linkedDrawerFolderId } as? com.neoapps.neolauncher.groups.category.DrawerFolders.CustomFolder
                            if (drawerFolder != null) {
                                drawerFolder.contents.value = newKeys
                                com.neoapps.neolauncher.preferences.NeoPrefs.getInstance().drawerFolders.saveToJson()
                            }
                        }
                        FolderSortUtil.sortFolder(folder, context, launcher.modelWriter)
                        folder.onIconChanged()
                        openManageAppsDialog = false
                    }
                }
            }
        }

        // Cover App Picker
        val folderItems = remember(folder.contents.size, selectedCoverItem) {
            folder.contents.filterIsInstance<WorkspaceItemInfo>()
        }

        if (coverMode.value && folderItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.folder_cover_select_app),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = stringResource(R.string.folder_cover_select_app_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    // Default / First App Card
                    item {
                        val isDefaultSelected = (selectedCoverItem == null)
                        Card(
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    folder.setCoverApp(null, launcher)
                                    selectedCoverItem = null
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isDefaultSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDefaultSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.folder_cover_indicator_none),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    items(folderItems) { item ->
                        val isSelected = (selectedCoverItem == item)
                        Card(
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    folder.setCoverApp(item, launcher)
                                    selectedCoverItem = item
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = rememberDrawablePainter(item.newIcon(context, BitmapInfo.FLAG_THEMED)),
                                    contentDescription = item.title?.toString() ?: "",
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title?.toString() ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (openCoverAppDialog) {
            var searchQuery by remember { mutableStateOf("") }
            val filteredFolderApps = remember(folderItems, searchQuery) {
                if (searchQuery.isBlank()) {
                    folderItems
                } else {
                    val query = searchQuery.trim().lowercase()
                    folderItems.filter {
                        val titleStr = it.title?.toString()?.lowercase() ?: ""
                        val pkgStr = it.targetComponent?.packageName?.lowercase()
                            ?: it.intent?.component?.packageName?.lowercase() ?: ""
                        titleStr.contains(query) || pkgStr.contains(query)
                    }
                }
            }

            BaseDialog(openDialogCustom = remember { mutableStateOf(true) }) {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(max = 520.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.folder_cover_select_app),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.folder_cover_select_app_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        if (folderItems.size > 5) {
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(stringResource(R.string.search_apps)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search_apps)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(android.R.string.cancel)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Option 1: Default (First App in Folder)
                            item {
                                val isDefaultSelected = (selectedCoverItem == null)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            folder.setCoverApp(null, launcher)
                                            selectedCoverItem = null
                                            openCoverAppDialog = false
                                        }
                                        .background(
                                            if (isDefaultSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.folder_cover_default_app),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.folder_cover_default_app_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isDefaultSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }

                            items(filteredFolderApps, key = { it.id.toString() + "_" + (it.title ?: "") }) { item ->
                                val isSelected = (selectedCoverItem == item)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            folder.setCoverApp(item, launcher)
                                            selectedCoverItem = item
                                            openCoverAppDialog = false
                                        }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = rememberDrawablePainter(item.newIcon(context, BitmapInfo.FLAG_THEMED)),
                                        contentDescription = item.title?.toString() ?: "",
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title?.toString() ?: "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { openCoverAppDialog = false }) {
                                Text(stringResource(android.R.string.cancel))
                            }
                        }
                    }
                }
            }
        }
    }
}