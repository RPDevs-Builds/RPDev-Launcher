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

package iamrp.dev.launcher.compose.pages.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import iamrp.dev.launcher.compose.components.BaseDialog
import iamrp.dev.launcher.compose.components.ViewWithActionBar
import iamrp.dev.launcher.compose.components.preferences.IntSelectionPrefDialogUI
import iamrp.dev.launcher.compose.components.preferences.IntentLauncherDialogUI
import iamrp.dev.launcher.compose.components.preferences.PreferenceGroup
import iamrp.dev.launcher.compose.components.preferences.StringMultiSelectionPrefDialogUI
import iamrp.dev.launcher.compose.components.preferences.StringSelectionPrefDialogUI
import iamrp.dev.launcher.compose.components.preferences.StringTextPrefDialogUI
import iamrp.dev.launcher.preferences.IntSelectionPref
import iamrp.dev.launcher.preferences.IntentLauncherPref
import iamrp.dev.launcher.preferences.StringMultiSelectionPref
import iamrp.dev.launcher.preferences.StringSelectionPref
import iamrp.dev.launcher.preferences.StringTextPref
import iamrp.dev.launcher.smartspace.weather.OWMWeatherProvider
import iamrp.dev.launcher.util.prefs

@Composable
fun WidgetsPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }
    val weatherProvider = prefs.smartspaceWeatherProvider.get().collectAsState(initial = prefs.smartspaceWeatherProvider.getValue())
    val isOwm = weatherProvider.value == OWMWeatherProvider::class.java.name
    val smartspacePrefs = listOfNotNull(
        prefs.smartspaceEnable,
        prefs.smartspaceBackground,
        prefs.smartspaceDate,
        prefs.smartspaceTime,
        prefs.smartspaceTime24H,
        prefs.smartspaceWeatherProvider,
        if (isOwm) prefs.smartspaceWeatherApiKey else null,
        if (isOwm) prefs.smartspaceWeatherCity else null,
        prefs.smartspaceWeatherUnit,
        prefs.smartspaceEventProviders
    )

    val notificationCustomColor = prefs.notificationCustomColor.get().collectAsState(initial = prefs.notificationCustomColor.getValue())
    val notificationsPrefs = listOfNotNull(
        prefs.notificationDots,
        prefs.notificationCustomColor,
        if (notificationCustomColor.value) {
            prefs.notificationBackground
        } else {
            null
        },
        prefs.notificationCount
    )

    ViewWithActionBar(
        title = stringResource(R.string.title__general_widgets_notifications)
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
                    stringResource(id = R.string.title__general_smartspace),
                    prefs = smartspacePrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    stringResource(id = R.string.pref_category__notifications),
                    prefs = notificationsPrefs,
                    onPrefDialog = onPrefDialog
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref) {
                    is IntentLauncherPref       -> IntentLauncherDialogUI(
                        pref = dialogPref as IntentLauncherPref,
                        openDialogCustom = openDialog
                    )

                    is IntSelectionPref         -> IntSelectionPrefDialogUI(
                        pref = dialogPref as IntSelectionPref,
                        openDialogCustom = openDialog
                    )

                    is StringSelectionPref      -> StringSelectionPrefDialogUI(
                        pref = dialogPref as StringSelectionPref,
                        openDialogCustom = openDialog
                    )

                    is StringMultiSelectionPref -> StringMultiSelectionPrefDialogUI(
                        pref = dialogPref as StringMultiSelectionPref,
                        openDialogCustom = openDialog
                    )

                    is StringTextPref           -> StringTextPrefDialogUI(
                        pref = dialogPref as StringTextPref,
                        openDialogCustom = openDialog
                    )
                }
            }
        }
    }
}