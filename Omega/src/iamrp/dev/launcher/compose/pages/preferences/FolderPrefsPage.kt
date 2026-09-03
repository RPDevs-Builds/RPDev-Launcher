/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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

package iamrp.dev.launcher.compose.pages.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import iamrp.dev.launcher.compose.components.ViewWithActionBar
import iamrp.dev.launcher.compose.components.preferences.PreferenceGroup
import iamrp.dev.launcher.util.prefs

@Composable
fun FolderPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val folderIconPref = listOf(
        prefs.desktopFolderIconShape,
        prefs.desktopFolderOpacity
    )

    val customFolderBg = prefs.desktopCustomFolderBackground.get().collectAsState(initial = prefs.desktopCustomFolderBackground.getValue())
    val folderStroke = prefs.desktopFolderStroke.get().collectAsState(initial = prefs.desktopFolderStroke.getValue())

    val folderGeneralPref = listOfNotNull(
        //prefs.desktopFolderFullScreen,
        prefs.desktopCustomFolderBackground,
        if (customFolderBg.value) {
            prefs.desktopFolderBackgroundColor
        } else null,
        prefs.desktopFolderStroke,
        if (folderStroke.value) {
            prefs.desktopFolderStrokeWidth
        } else null,
        if (folderStroke.value) {
            prefs.desktopFolderStrokeColor
        } else null,
    )

    val folderGridPrefs = listOf(
        prefs.desktopFolderColumns,
        prefs.desktopFolderRows
    )

    ViewWithActionBar(
        title = stringResource(R.string.pref_title_folder)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(
                    stringResource(id = R.string.pref_title_general),
                    prefs = folderGeneralPref,
                    onPrefDialog = onPrefDialog
                )
            }

            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_drawer_icons),
                    prefs = folderIconPref,
                    onPrefDialog = onPrefDialog
                )
            }

            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_desktop_grid),
                    prefs = folderGridPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
        }
    }
}