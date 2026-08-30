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

package iamrp.dev.launcher.compose.components.preferences

import androidx.compose.runtime.Composable
import iamrp.dev.launcher.compose.objects.PageItem
import iamrp.dev.launcher.preferences.BooleanPref
import iamrp.dev.launcher.preferences.ColorIntPref
import iamrp.dev.launcher.preferences.DialogPref
import iamrp.dev.launcher.preferences.FloatPref
import iamrp.dev.launcher.preferences.GridSize
import iamrp.dev.launcher.preferences.GridSize2D
import iamrp.dev.launcher.preferences.IdpIntPref
import iamrp.dev.launcher.preferences.IntPref
import iamrp.dev.launcher.preferences.IntSelectionPref
import iamrp.dev.launcher.preferences.IntentLauncherPref
import iamrp.dev.launcher.preferences.LongSelectionPref
import iamrp.dev.launcher.preferences.NavigationPref
import iamrp.dev.launcher.preferences.StringMultiSelectionPref
import iamrp.dev.launcher.preferences.StringPref
import iamrp.dev.launcher.preferences.StringSelectionPref
import iamrp.dev.launcher.preferences.StringSetPref
import iamrp.dev.launcher.preferences.StringTextPref

@Composable
fun PreferenceBuilder(pref: Any, onDialogPref: (Any) -> Unit, index: Int, size: Int) = when (pref) {
    is IntentLauncherPref       -> IntentLauncherPreference(
        pref = pref,
        index = index,
        groupSize = size
    ) { onDialogPref(pref) }

    is GridSize2D               -> GridSize2DPreference(
        pref = pref,
        index = index,
        groupSize = size
    ) { onDialogPref(pref) }

    is GridSize                 -> GridSizePreference(
        pref = pref,
        index = index,
        groupSize = size
    ) { onDialogPref(pref) }

    is BooleanPref              -> SwitchPreference(
        pref = pref,
        index = index,
        groupSize = size
    )

    is NavigationPref           ->
        NavigationPreference(pref = pref, index = index, groupSize = size)

    is ColorIntPref             ->
        ColorIntPreference(pref = pref, index = index, groupSize = size)

    is StringPref               ->
        StringPreference(pref = pref, index = index, groupSize = size)

    is StringSetPref            ->
        StringSetPreference(pref = pref, index = index, groupSize = size)

    is FloatPref                ->
        SeekBarPreference(pref = pref, index = index, groupSize = size)

    is IntPref                  ->
        IntSeekBarPreference(pref = pref, index = index, groupSize = size)

    is IdpIntPref               ->
        IntSeekBarPreference(pref = pref, index = index, groupSize = size)

    is DialogPref               ->
        AlertDialogPreference(pref = pref, index = index, groupSize = size) {
            onDialogPref(
                pref
            )
        }

    is IntSelectionPref         ->
        IntSelectionPreference(
            pref = pref,
            index = index,
            groupSize = size
        ) { onDialogPref(pref) }

    is LongSelectionPref        ->
        LongSelectionPreference(
            pref = pref,
            index = index,
            groupSize = size
        ) { onDialogPref(pref) }

    is StringSelectionPref      ->
        StringSelectionPreference(
            pref = pref,
            index = index,
            groupSize = size
        ) { onDialogPref(pref) }

    is StringTextPref           ->
        StringTextPreference(
            pref = pref,
            index = index,
            groupSize = size
        ) { onDialogPref(pref) }

    is StringMultiSelectionPref -> StringMultiSelectionPreference(
        pref = pref,
        index = index,
        groupSize = size
    ) { onDialogPref(pref) }

    is PageItem                 ->
        PagePreference(
            titleId = pref.titleId,
            icon = pref.icon,
            route = pref.route,
            index = index,
            groupSize = size
        )

    else                        -> {}
}
