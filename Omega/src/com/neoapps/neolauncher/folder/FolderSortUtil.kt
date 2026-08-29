package com.neoapps.neolauncher.folder

import android.content.Context
import androidx.core.graphics.ColorUtils
import com.android.launcher3.Launcher
import com.android.launcher3.folder.FolderGridOrganizer
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.neoapps.neolauncher.allapps.comparator.AppColorComparator
import com.neoapps.neolauncher.data.AppTrackerRepository
import java.text.Collator
import java.util.ArrayList

object FolderSortUtil {

    fun getFolderItemComparator(context: Context, sortMode: Int): Comparator<ItemInfo>? {
        val collator = Collator.getInstance()
        return when (sortMode) {
            FolderInfo.SORT_AZ -> Comparator { a, b ->
                collator.compare(a.title?.toString() ?: "", b.title?.toString() ?: "")
            }
            FolderInfo.SORT_ZA -> Comparator { a, b ->
                collator.compare(b.title?.toString() ?: "", a.title?.toString() ?: "")
            }
            FolderInfo.SORT_MOST_USED -> {
                val appTrackerRepo = AppTrackerRepository.INSTANCE.get(context)
                val appsCount = appTrackerRepo.getAppsCount()
                val countMap = appsCount.associate { it.packageName to it.count }
                Comparator { a, b ->
                    val pkgA = a.targetComponent?.packageName ?: a.intent?.component?.packageName ?: ""
                    val pkgB = b.targetComponent?.packageName ?: b.intent?.component?.packageName ?: ""
                    val countA = countMap[pkgA] ?: 0
                    val countB = countMap[pkgB] ?: 0
                    val diff = countB.compareTo(countA)
                    if (diff != 0) diff else collator.compare(a.title?.toString() ?: "", b.title?.toString() ?: "")
                }
            }
            FolderInfo.SORT_BY_INSTALL_DATE -> {
                val pm = context.packageManager
                val cache = mutableMapOf<String, Long>()
                fun getInstallTime(pkg: String): Long {
                    return cache.getOrPut(pkg) {
                        try {
                            pm.getPackageInfo(pkg, 0).firstInstallTime
                        } catch (e: Exception) {
                            0L
                        }
                    }
                }
                Comparator { a, b ->
                    val pkgA = a.targetComponent?.packageName ?: a.intent?.component?.packageName ?: ""
                    val pkgB = b.targetComponent?.packageName ?: b.intent?.component?.packageName ?: ""
                    val timeA = getInstallTime(pkgA)
                    val timeB = getInstallTime(pkgB)
                    val diff = timeB.compareTo(timeA) // Newest first
                    if (diff != 0) diff else collator.compare(a.title?.toString() ?: "", b.title?.toString() ?: "")
                }
            }
            FolderInfo.SORT_BY_COLOR -> {
                Comparator { a, b ->
                    val colorA = if (a is WorkspaceItemInfo) a.bitmap.color else 0
                    val colorB = if (b is WorkspaceItemInfo) b.bitmap.color else 0
                    val hslA = FloatArray(3)
                    val hslB = FloatArray(3)
                    ColorUtils.colorToHSL(colorA, hslA)
                    ColorUtils.colorToHSL(colorB, hslB)
                    val h2A = AppColorComparator.remapHue(hslA[0])
                    val h2B = AppColorComparator.remapHue(hslB[0])
                    var s2A = AppColorComparator.remap(hslA[1])
                    var s2B = AppColorComparator.remap(hslB[1])
                    var l2A = AppColorComparator.remap(hslA[2])
                    var l2B = AppColorComparator.remap(hslB[2])
                    if (h2A % 2 == 1) {
                        s2A = AppColorComparator.REPETITIONS - s2A
                        l2A = AppColorComparator.REPETITIONS - l2A
                    }
                    if (h2B % 2 == 1) {
                        s2B = AppColorComparator.REPETITIONS - s2B
                        l2B = AppColorComparator.REPETITIONS - l2B
                    }
                    var result = h2A.compareTo(h2B)
                    if (result != 0) return@Comparator result
                    result = l2A.compareTo(l2B)
                    if (result != 0) return@Comparator result
                    result = s2A.compareTo(s2B)
                    if (result != 0) return@Comparator result
                    collator.compare(a.title?.toString() ?: "", b.title?.toString() ?: "")
                }
            }
            else -> null
        }
    }

    @JvmStatic
    fun sortFolder(folder: FolderInfo, context: Context, modelWriter: ModelWriter?) {
        val sortMode = folder.sortMode
        if (sortMode == FolderInfo.SORT_MANUAL) return
        val comparator = getFolderItemComparator(context, sortMode) ?: return

        folder.contents.sortWith(comparator)

        try {
            val launcher = Launcher.getLauncher(context)
            val verifier = FolderGridOrganizer.createFolderGridOrganizer(launcher.deviceProfile).setFolderInfo(folder)
            val modifiedItems = ArrayList<ItemInfo>()
            for (i in folder.contents.indices) {
                val item = folder.contents[i]
                if (verifier.updateRankAndPos(item, i)) {
                    modifiedItems.add(item)
                }
            }

            if (modelWriter != null && modifiedItems.isNotEmpty()) {
                modelWriter.moveItemsInDatabase(modifiedItems, folder.id, 0)
            }
        } catch (e: Exception) {
            // Ignore if launcher is not available in non-activity context
        }
        folder.onIconChanged()
    }
}
