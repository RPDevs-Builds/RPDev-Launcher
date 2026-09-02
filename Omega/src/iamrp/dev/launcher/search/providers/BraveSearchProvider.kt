/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs-Builds
 */

package iamrp.dev.launcher.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import iamrp.dev.launcher.search.AbstractSearchProvider
import iamrp.dev.launcher.util.isAppEnabled

@Keep
class BraveSearchProvider(context: Context) : AbstractSearchProvider(context) {

    override val name = "Brave Search"
    override val id = 1010L
    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = false
    override val packageName = "com.brave.browser"

    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(packageName, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) {
        callback(Intent(Intent.ACTION_ASSIST).setPackage(packageName))
    }

    override val iconRes: Int
        get() = R.drawable.ic_search
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!
}
