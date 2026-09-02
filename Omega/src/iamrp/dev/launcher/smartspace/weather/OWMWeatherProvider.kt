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

package iamrp.dev.launcher.smartspace.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Icon
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import iamrp.dev.launcher.neoApp
import iamrp.dev.launcher.smartspace.Temperature
import iamrp.dev.launcher.smartspace.model.SmartspaceScores
import iamrp.dev.launcher.smartspace.model.WeatherData
import iamrp.dev.launcher.smartspace.provider.SmartspaceDataSource
import iamrp.dev.launcher.smartspace.weather.GoogleWeatherProvider.Companion.dummyTarget
import iamrp.dev.launcher.smartspace.weather.icons.WeatherIconProvider
import iamrp.dev.launcher.util.Permissions
import iamrp.dev.launcher.util.Permissions.REQUEST_PERMISSION_LOCATION_ACCESS
import iamrp.dev.launcher.util.checkLocationAccess
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OWMWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.weather_provider_owm
), CurrentWeatherCallback {
    override val isAvailable = true
    override val disabledTargets = listOf(dummyTarget)
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    private val owm by lazy { OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.getValue()) }
    private val iconProvider by lazy { WeatherIconProvider(context) }
    private var weatherData: WeatherData? = null
    private val locationAccess get() = context.checkLocationAccess()
    private val locationManager: LocationManager? by lazy {
        if (locationAccess) {
            context.getSystemService(LocationManager::class.java)
        } else null
    }

    init {
        updateData()
        internalTargets = flow {
            while (true) {
                updateData()
                emit(updateWeatherData())
                delay(TimeUnit.MINUTES.toMillis(10))
            }
        }
    }

    private fun updateWeatherData(): List<SmartspaceTarget> {
        val data = weatherData
        if (data != null && data.icon != null) {
            Log.d("OWM", "Updating weather data " + data.getTitle())
            val target = SmartspaceTarget(
                smartspaceTargetId = "OWMWeatherMap",
                headerAction = SmartspaceAction(
                    id = "OWMWeatherMap",
                    icon = Icon.createWithBitmap(data.icon),
                    title = "",
                    subtitle = data.getTitle(Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())),
                    pendingIntent = data.pendingIntent
                ),
                score = SmartspaceScores.SCORE_WEATHER,
                featureType = SmartspaceTarget.FEATURE_WEATHER,
            )
            return listOf(target)
        } else {
            return disabledTargets
        }
    }

    @SuppressLint("MissingPermission")
    fun updateData() {
        if (prefs.smartspaceWeatherCity.getValue() == "##Auto") {
            if (!locationAccess) {
                context.neoApp.activityHandler.foregroundActivity?.let {
                    Permissions.requestPermission(
                        it,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        REQUEST_PERMISSION_LOCATION_ACCESS
                    )
                }
                return
            } else {
                val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    locationManager?.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } else {
                    @Suppress("DEPRECATION")
                    val locationProvider = locationManager?.getBestProvider(Criteria(), true)
                    locationProvider?.let { locationManager?.getLastKnownLocation(it) }
                } ?: locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    owm.getCurrentWeatherByGeoCoordinates(
                        location.latitude,
                        location.longitude,
                        this
                    )
                }
            }
        } else {
            owm.getCurrentWeatherByCityName(prefs.smartspaceWeatherCity.getValue(), this)
        }
    }

    override fun onSuccess(currentWeather: CurrentWeather) {
        val temp = currentWeather.main?.temp ?: return
        val icon = currentWeather.weather.getOrNull(0)?.icon ?: return
        weatherData = WeatherData(
            iconProvider.getIcon(icon),
            Temperature(
                temp.roundToInt(),
                Temperature.Unit.Kelvin
            ),
            "https://openweathermap.org/city/${currentWeather.id}"
        )
        updateWeatherData()
    }

    override fun onFailure(throwable: Throwable?) {
        if ((prefs.smartspaceWeatherApiKey.getValue() == context.getString(R.string.default_owm_key)
                    && !BuildConfig.APPLICATION_ID.contains("debug")
                    && !BuildConfig.APPLICATION_ID.contains("alpha"))
            || throwable?.message == apiKeyError
        ) {
            Toast.makeText(context, R.string.owm_get_your_own_key, Toast.LENGTH_LONG).show()
        } else if (throwable != null) {
            Log.d("OWM", "Updating weather data failed", throwable)
            Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        }
        updateWeatherData()
    }

    companion object {

        private const val apiKeyError = "UnAuthorized. Please set a valid OpenWeatherMap API KEY" +
                " by using the setApiKey method."
    }
}