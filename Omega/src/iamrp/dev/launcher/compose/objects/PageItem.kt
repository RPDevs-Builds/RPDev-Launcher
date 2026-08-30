package iamrp.dev.launcher.compose.objects

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.launcher3.R
import iamrp.dev.launcher.compose.icons.Phosphor
import iamrp.dev.launcher.compose.icons.PhosphorCustom
import iamrp.dev.launcher.compose.icons.custom.DeviceMobileDock
import iamrp.dev.launcher.compose.icons.phosphor.BracketsCurly
import iamrp.dev.launcher.compose.icons.phosphor.ClockCounterClockwise
import iamrp.dev.launcher.compose.icons.phosphor.Copyleft
import iamrp.dev.launcher.compose.icons.phosphor.DotsNine
import iamrp.dev.launcher.compose.icons.phosphor.Folder
import iamrp.dev.launcher.compose.icons.phosphor.Info
import iamrp.dev.launcher.compose.icons.phosphor.ListDashes
import iamrp.dev.launcher.compose.icons.phosphor.MagnifyingGlass
import iamrp.dev.launcher.compose.icons.phosphor.Monitor
import iamrp.dev.launcher.compose.icons.phosphor.Palette
import iamrp.dev.launcher.compose.icons.phosphor.ScribbleLoop
import iamrp.dev.launcher.compose.icons.phosphor.SquaresFour
import iamrp.dev.launcher.compose.icons.phosphor.Translate
import iamrp.dev.launcher.compose.navigation.NavRoute

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector? = null,
    val route: NavRoute,
) {
    companion object {
        val PrefsProfile = PageItem(
            titleId = R.string.title__general_profile,
            icon = Phosphor.Palette,
            route = NavRoute.Profile()
        )
        val PrefsDesktop = PageItem(
            titleId = R.string.title__general_desktop,
            icon = Phosphor.Monitor,
            route = NavRoute.Desktop()
        )

        val PrefsFolder = PageItem(
            titleId = R.string.title_general_folder,
            icon = Phosphor.Folder,
            route = NavRoute.Folder()
        )
        val PrefsDock = PageItem(
            titleId = R.string.title__general_dock,
            icon = PhosphorCustom.DeviceMobileDock,
            route = NavRoute.Dock()
        )
        val PrefsDrawer = PageItem(
            titleId = R.string.title__general_drawer,
            icon = Phosphor.DotsNine,
            route = NavRoute.Drawer()
        )
        val PrefsWidgetsNotifications = PageItem(
            titleId = R.string.title__general_widgets_notifications,
            icon = Phosphor.SquaresFour,
            route = NavRoute.Widgets()
        )
        val PrefsSearchFeed = PageItem(
            titleId = R.string.title__general_search_feed,
            icon = Phosphor.MagnifyingGlass,
            route = NavRoute.Search()
        )
        val PrefsGesturesDash = PageItem(
            titleId = R.string.title__general_gestures_dash,
            icon = Phosphor.ScribbleLoop,
            route = NavRoute.Gestures()
        )
        val PrefsBackup = PageItem(
            titleId = R.string.backups,
            icon = Phosphor.ClockCounterClockwise,
            route = NavRoute.Backup()
        )
        val PrefsDeveloper = PageItem(
            titleId = R.string.developer_options_title,
            icon = Phosphor.BracketsCurly,
            route = NavRoute.Dev()
        )
        val PrefsAbout = PageItem(
            titleId = R.string.title__general_about,
            icon = Phosphor.Info,
            route = NavRoute.About()
        )
        val AboutTranslators = PageItem(
            titleId = R.string.about_translators,
            icon = Phosphor.Translate,
            route = NavRoute.About.Translators()
        )
        val AboutLicense = PageItem(
            titleId = R.string.category__about_licenses,
            icon = Phosphor.Copyleft,
            route = NavRoute.About.License()
        )
        val AboutChangelog = PageItem(
            titleId = R.string.title__about_changelog,
            icon = Phosphor.ListDashes,
            route = NavRoute.About.Changelog()
        )

        val Acknowledgement = PageItem(
            titleId = R.string.title__about_acknowledgement,
            icon = Phosphor.Copyleft,
            route = NavRoute.About.Acknowledgement()
        )
    }
}