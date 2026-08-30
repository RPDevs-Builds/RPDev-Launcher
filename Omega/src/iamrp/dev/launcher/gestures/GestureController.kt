/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package iamrp.dev.launcher.gestures

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.lifecycleScope
import com.android.launcher3.util.TouchController
import com.android.launcher3.util.VibratorWrapper
import iamrp.dev.launcher.RPDevLauncher
import iamrp.dev.launcher.gestures.gestures.DoubleTapGesture
import iamrp.dev.launcher.gestures.gestures.LaunchAssistantGesture
import iamrp.dev.launcher.gestures.gestures.LongPressGesture
import iamrp.dev.launcher.gestures.gestures.PressBackGesture
import iamrp.dev.launcher.gestures.gestures.PressHomeGesture
import iamrp.dev.launcher.gestures.gestures.SwipeDownGesture
import iamrp.dev.launcher.gestures.gestures.SwipeUpDockGesture
import iamrp.dev.launcher.gestures.gestures.SwipeUpGesture
import iamrp.dev.launcher.gestures.handlers.NotificationsOpenGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenDashGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenDrawerGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenOverlayGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenOverviewGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenSettingsGestureHandler
import iamrp.dev.launcher.gestures.handlers.OpenWidgetsGestureHandler
import iamrp.dev.launcher.gestures.handlers.PressBackGestureHandler
import iamrp.dev.launcher.gestures.handlers.SleepGestureHandler
import iamrp.dev.launcher.gestures.handlers.SleepGestureHandlerTimeout
import iamrp.dev.launcher.gestures.handlers.StartGlobalSearchGestureHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class GestureController(val launcher: RPDevLauncher) : TouchController {
    val scope = MainScope()

    val blankGestureHandler = BlankGestureHandler(launcher, null)
    private val doubleTapGesture = DoubleTapGesture(this)
    private val pressHomeGesture = PressHomeGesture(this)
    private val pressBackGesture = PressBackGesture(this)
    private val longPressGesture = LongPressGesture(this)
    private val assistantGesture = LaunchAssistantGesture(this)

    val swipeUpGesture = SwipeUpGesture(this)
    val swipeUpDockGesture = SwipeUpDockGesture(this)
    val swipeDownGesture = SwipeDownGesture(this)

    var touchDownPoint = PointF()

    private var swipeUpOverride: Pair<GestureHandler, Long>? = null

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    fun onDoubleTap() {
        triggerGesture(doubleTapGesture)
    }

    fun onLongPress() {
        triggerGesture(longPressGesture)
    }

    fun onPressHome() {
        triggerGesture(pressHomeGesture)
    }

    fun onPressBack() {
        triggerGesture(pressBackGesture)
    }

    fun onSwipeUp() {
        triggerGesture(swipeUpGesture)
    }

    fun onSwipeUpDock() {
        triggerGesture(swipeUpDockGesture)
    }

    fun onSwipeDown() {
        triggerGesture(swipeDownGesture)
    }

    fun onLaunchAssistant() {
        triggerGesture(assistantGesture)
    }

    fun triggerGesture(gesture: Gesture, withHaptic: Boolean = false) {
        if (!gesture.isEnabled) return
        launcher.lifecycleScope.launch {
            val handler = gesture.handler.value
            if (handler is BlankGestureHandler) return@launch
            handler.onGestureTrigger(this@GestureController)
            if (withHaptic) VibratorWrapper.INSTANCE.get(launcher)
                .vibrate(VibratorWrapper.OVERVIEW_HAPTIC)
        }
    }

    fun setSwipeUpOverride(handler: GestureHandler, downTime: Long) {
        if (swipeUpOverride?.second != downTime) {
            swipeUpOverride = Pair(handler, downTime)
        }
    }

    fun getSwipeUpOverride(downTime: Long): GestureHandler? {
        swipeUpOverride?.let {
            if (it.second == downTime) {
                return it.first
            } else {
                swipeUpOverride = null
            }
        }
        return null
    }

    fun createGestureHandler(jsonString: String) =
        createGestureHandler(launcher, jsonString, blankGestureHandler)

    companion object {
        private const val TAG = "GestureController"
        private val LEGACY_SLEEP_HANDLERS = listOf(
            "iamrp.dev.launcher.gestures.handlers.SleepGestureHandlerDeviceAdmin",
            "iamrp.dev.launcher.gestures.handlers.SleepGestureHandlerAccessibility"
        )

        fun createGestureHandler(
            context: Context,
            jsonString: String?,
            fallback: GestureHandler,
        ): GestureHandler {
            if (!jsonString.isNullOrEmpty()) {
                val config: JSONObject? = try {
                    JSONObject(jsonString ?: "{ }")
                } catch (e: JSONException) {
                    null
                }
                var className = config?.getString("class") ?: jsonString
                if (className in LEGACY_SLEEP_HANDLERS) {
                    className = SleepGestureHandler::class.java.name
                }
                val configValue =
                    if (config?.has("config") == true) config.getJSONObject("config") else null
                try {
                    val handler = Class.forName(className)
                        .getConstructor(Context::class.java, JSONObject::class.java)
                        .newInstance(context, configValue) as GestureHandler
                    if (handler.isAvailable) return handler
                } catch (t: Throwable) {
                    Log.e(TAG, "can't create gesture handler", t)
                }
            }
            return fallback
        }

        fun getClassName(jsonString: String): String {
            val config: JSONObject? = try {
                JSONObject(jsonString)
            } catch (e: JSONException) {
                null
            }
            val className = config?.getString("class") ?: jsonString
            return if (className in LEGACY_SLEEP_HANDLERS) {
                SleepGestureHandler::class.java.name
            } else {
                className
            }
        }

        fun getGestureHandlers(context: Context, isSwipeUp: Boolean, hasBlank: Boolean) =
            mutableListOf(
                PressBackGestureHandler(context, null),
                SleepGestureHandler(context, null),
                SleepGestureHandlerTimeout(context, null),
                OpenDashGestureHandler(context, null),
                OpenDrawerGestureHandler(context, null),
                OpenWidgetsGestureHandler(context, null),
                NotificationsOpenGestureHandler(context, null),
                OpenOverlayGestureHandler(context, null),
                OpenOverviewGestureHandler(context, null),
                StartGlobalSearchGestureHandler(context, null),
                OpenSettingsGestureHandler(context, null),
            ).apply {
                if (hasBlank) {
                    add(0, BlankGestureHandler(context, null))
                }
            }.filter { it.isAvailableForSwipeUp(isSwipeUp) }
    }
}
