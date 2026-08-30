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
package iamrp.dev.launcher.dash

import android.content.Context
import com.android.launcher3.R
import iamrp.dev.launcher.dash.actionprovider.AllAppsShortcut
import iamrp.dev.launcher.dash.actionprovider.AudioPlayer
import iamrp.dev.launcher.dash.actionprovider.ChangeWallpaper
import iamrp.dev.launcher.dash.actionprovider.DeviceSettings
import iamrp.dev.launcher.dash.actionprovider.EditDash
import iamrp.dev.launcher.dash.actionprovider.LaunchAssistant
import iamrp.dev.launcher.dash.actionprovider.ManageApps
import iamrp.dev.launcher.dash.actionprovider.ManageVolume
import iamrp.dev.launcher.dash.actionprovider.OmegaSettings
import iamrp.dev.launcher.dash.actionprovider.SleepDevice
import iamrp.dev.launcher.dash.actionprovider.Torch
import iamrp.dev.launcher.dash.controlprovider.AutoRotation
import iamrp.dev.launcher.dash.controlprovider.Bluetooth
import iamrp.dev.launcher.dash.controlprovider.Location
import iamrp.dev.launcher.dash.controlprovider.MobileData
import iamrp.dev.launcher.dash.controlprovider.Sync
import iamrp.dev.launcher.dash.controlprovider.Wifi

val dashProviderOptions = mapOf(
    EditDash::class.java.name to R.string.edit_dash,
    ChangeWallpaper::class.java.name to R.string.wallpaper_pick,
    OmegaSettings::class.java.name to R.string.settings_button_text,
    ManageVolume::class.java.name to R.string.dash_volume_title,
    DeviceSettings::class.java.name to R.string.dash_device_settings_title,
    ManageApps::class.java.name to R.string.tab_manage_apps,
    AllAppsShortcut::class.java.name to R.string.dash_all_apps_title,
    SleepDevice::class.java.name to R.string.action_sleep,
    LaunchAssistant::class.java.name to R.string.launch_assistant,
    Torch::class.java.name to R.string.dash_torch,
    AudioPlayer::class.java.name to R.string.dash_media_player,
    Wifi::class.java.name to R.string.dash_wifi,
    MobileData::class.java.name to R.string.dash_mobile_network_title,
    Location::class.java.name to R.string.dash_location,
    Bluetooth::class.java.name to R.string.dash_bluetooth,
    AutoRotation::class.java.name to R.string.dash_auto_rotation,
    Sync::class.java.name to R.string.dash_sync
)

fun getDashActionProviders(context: Context) = listOf(
    EditDash(context),
    ChangeWallpaper(context),
    OmegaSettings(context),
    ManageVolume(context),
    DeviceSettings(context),
    ManageApps(context),
    AllAppsShortcut(context),
    SleepDevice(context),
    LaunchAssistant(context),
    Torch(context),
    AudioPlayer(context)
)

fun getDashControlProviders(context: Context) = listOf(
    Wifi(context),
    MobileData(context),
    Location(context),
    Bluetooth(context),
    AutoRotation(context),
    Sync(context)
)