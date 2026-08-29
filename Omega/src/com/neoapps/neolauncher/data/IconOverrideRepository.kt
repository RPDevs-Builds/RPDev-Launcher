package com.neoapps.neolauncher.data

import android.content.Context
import com.android.launcher3.LauncherAppState
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.model.tasks.PackageUpdatedTask
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.data.models.IconOverride
import com.neoapps.neolauncher.data.models.IconPickerItem
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

@LauncherAppSingleton
class IconOverrideRepository  @Inject constructor(@ApplicationContext private val context: Context,) {

    private val scope = MainScope() + CoroutineName("IconOverrideRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).iconOverrideDao()
    private var _overridesMap = mapOf<ComponentKey, IconPickerItem>()
    val overridesMap get() = _overridesMap
    private val updatePackageQueue = ConcurrentLinkedQueue<ComponentKey>()

    init {
        scope.launch {
            dao.observeAll()
                .flowOn(Dispatchers.Main)
                .collect { overrides ->
                    _overridesMap = overrides.associateBy(
                        keySelector = { it.target },
                        valueTransform = { it.iconPickerItem }
                    )
                }
        }
    }

    suspend fun setOverride(target: ComponentKey, item: IconPickerItem) {
        _overridesMap = _overridesMap + (target to item)
        dao.insert(IconOverride(target, item))
        updatePackageIcons(target)
    }

    suspend fun deleteOverride(target: ComponentKey) {
        _overridesMap = _overridesMap - target
        dao.delete(target)
        updatePackageIcons(target)
    }

    fun observeTarget(target: ComponentKey) = dao.observeTarget(target)
    fun observeCount() = dao.observeCount()

    suspend fun deleteAll() {
        val targets = dao.getAll()
        _overridesMap = emptyMap()
        dao.deleteAll()
        updatePackageIcons(targets)
    }

    private fun updatePackageIcons(target: ComponentKey) {
        if (target.componentName.packageName == "com.neoapps.neolauncher.folder") {
            return
        }
        val appState = LauncherAppState.getInstance(context)
        appState.model.enqueueModelUpdateTask(
            PackageUpdatedTask(
                PackageUpdatedTask.OP_UPDATE,
                target.user,
                target.componentName.packageName
            )
        )
    }

    private fun updatePackageIcons(targets: List<IconOverride>) {
        val appState = LauncherAppState.getInstance(context)
        val packageUserSet = targets
            .map { it.target.componentName.packageName to it.target.user }
            .filter { (pkg, _) -> pkg != "com.neoapps.neolauncher.folder" }
            .toSet()
        packageUserSet.forEach { (pkg, user) ->
            appState.model.enqueueModelUpdateTask(
                PackageUpdatedTask(
                    PackageUpdatedTask.OP_UPDATE,
                    user,
                    pkg
                )
            )
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconOverrideRepository)
    }
}
