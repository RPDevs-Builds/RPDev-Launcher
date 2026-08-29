/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.icons.cache

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.os.Build
import android.os.Build.VERSION
import android.os.UserHandle
import android.util.Log
import com.android.launcher3.Flags.useNewIconForArchivedApps
import com.android.launcher3.icons.BaseIconFactory.IconOptions
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.IconProvider

open class LauncherActivityCachingLogic : CachingLogic<LauncherActivityInfo> {

    override fun getComponent(item: LauncherActivityInfo): ComponentName = item.componentName

    override fun getUser(item: LauncherActivityInfo): UserHandle = item.user

    override fun getLabel(item: LauncherActivityInfo): CharSequence? = item.label

    override fun getApplicationInfo(item: LauncherActivityInfo) = item.applicationInfo

    override fun loadIcon(
        context: Context,
        cache: BaseIconCache,
        item: LauncherActivityInfo,
    ): BitmapInfo {
        val activityInfo = if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item.activityInfo
        } else {
            context.packageManager.getActivityInfo(item.componentName, 0)
        }
        cache.iconFactory.use { li ->
            val iconOptions: IconOptions =
                IconOptions()
                    .setUser(item.user)
                    .assumeFullBleedIcon(
                        // b/358123888: Pre-archived apps can have BitmapDrawables without insets
                        useNewIconForArchivedApps() &&
                                VERSION.SDK_INT >= 35 &&
                                activityInfo.isArchived
                    )
                    .setSourceHint(getSourceHint(item, cache))
            val iconDrawable = cache.iconProvider.getIcon(activityInfo, li.fullResIconDpi)
            if (VERSION.SDK_INT >= 30 && context.packageManager.isDefaultApplicationIcon(
                    iconDrawable
                )
            ) {
                Log.w(
                    TAG,
                    "loadIcon: Default app icon returned from PackageManager." +
                        " component=${item.componentName}, user=${item.user}",
                    Exception(),
                )
                // Make sure this default icon always matches BaseIconCache#getDefaultIcon
                return cache.getDefaultIcon(item.user)
            }
            return li.createBadgedIconBitmap(iconDrawable, iconOptions)
        }
    }

    override fun getFreshnessIdentifier(
        item: LauncherActivityInfo,
        iconProvider: IconProvider,
    ): String? = iconProvider.getStateForApp(getApplicationInfo(item))

    companion object{
        const val TAG = "LauncherActivityCachingLogic"
    }
}
