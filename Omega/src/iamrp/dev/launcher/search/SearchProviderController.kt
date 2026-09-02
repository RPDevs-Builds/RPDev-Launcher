/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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
 */

package iamrp.dev.launcher.search

import android.content.Context
import iamrp.dev.launcher.data.SearchProviderRepository
import iamrp.dev.launcher.data.models.SearchProvider
import iamrp.dev.launcher.search.providers.BaiduSearchProvider
import iamrp.dev.launcher.search.providers.BingSearchProvider
import iamrp.dev.launcher.search.providers.BraveSearchProvider
import iamrp.dev.launcher.search.providers.DuckDuckGoSearchProvider
import iamrp.dev.launcher.search.providers.EdgeSearchProvider
import iamrp.dev.launcher.search.providers.FirefoxSearchProvider
import iamrp.dev.launcher.search.providers.GoogleGoSearchProvider
import iamrp.dev.launcher.search.providers.SFinderSearchProvider
import iamrp.dev.launcher.search.providers.SearXNGSearchProvider
import iamrp.dev.launcher.search.providers.StartpageSearchProvider
import iamrp.dev.launcher.theme.ThemeManager
import iamrp.dev.launcher.theme.ThemeOverride
import iamrp.dev.launcher.util.SingletonHolder
import iamrp.dev.launcher.util.ensureOnMainThread
import iamrp.dev.launcher.util.useApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class SearchProviderController(private val context: Context) {
    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())

    private var themeRes: Int = 0

    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    val searchProvidersState: StateFlow<List<SearchProvider>>
        get() = getKoin().get<SearchProviderRepository>().activeProviders
    private val _searchProviderSelector: MutableStateFlow<Int> = MutableStateFlow(0)
    val searchProviderSelector: Flow<Int> = _searchProviderSelector.map {
        it.coerceIn(0, searchProvidersState.value.size)
    }
    val activeSearchProvider: SearchProvider
        get() = searchProvidersState.value.getOrNull(_searchProviderSelector.value)
            ?: searchProvidersState.value.firstOrNull()
            ?: SearchProvider.offlineSearchProvider(context)

    fun changeSearchProvider() {
        val size = searchProvidersState.value.size
        if (size > 0) {
            _searchProviderSelector.tryEmit((_searchProviderSelector.value + 1) % size)
        }
    }

    inner class ThemeListener : ThemeOverride.ThemeOverrideListener {

        override val isAlive = true

        override fun applyTheme(themeRes: Int) {
            this@SearchProviderController.themeRes = themeRes
        }

        override fun reloadTheme() {
            applyTheme(themeOverride.getTheme(context))
        }
    }

    companion object : SingletonHolder<SearchProviderController, Context>(
        ensureOnMainThread(
            useApplicationContext(::SearchProviderController)
        )
    ) {
        fun getSearchProvidersMap(): Map<Long, String> =
            getKoin().get<SearchProviderRepository>()
                .allProviders
                .value
                .associate {
                    Pair(it.id, it.name)
                }

        fun getSearchProviders(): Map<Long, SearchProvider> =
            getKoin().get<SearchProviderRepository>()
                .allProviders
                .value
                .associateBy { it.id }

        fun getAppSearchProviders(context: Context): List<AbstractSearchProvider> {
            val list = listOf(
                BaiduSearchProvider(context),
                BingSearchProvider(context),
                BraveSearchProvider(context),
                DuckDuckGoSearchProvider(context),
                EdgeSearchProvider(context),
                FirefoxSearchProvider(context),
                GoogleGoSearchProvider(context),
                SearXNGSearchProvider(context),
                SFinderSearchProvider(context),
                StartpageSearchProvider(context)
            )
            return list
        }
    }
}