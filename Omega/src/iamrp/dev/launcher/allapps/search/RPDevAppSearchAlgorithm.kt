package iamrp.dev.launcher.allapps.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.asLiveData
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.ModelTaskController
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import iamrp.dev.launcher.launcher
import iamrp.dev.launcher.preferences.NeoPrefs
import iamrp.dev.launcher.search.LocalMathEngine
import iamrp.dev.launcher.search.SearchProviderController
import iamrp.dev.launcher.util.prefs
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.Locale

class RPDevAppSearchAlgorithm(val context: Context, addNoResultsMessage: Boolean) :
    DefaultAppSearchAlgorithm(context, addNoResultsMessage) {

    private val prefs = context.prefs
    private var searchHiddenAppsEnable = false

    init {
        prefs.searchHiddenApps.get().asLiveData().observeForever {
            searchHiddenAppsEnable = it
        }
    }

    override fun destroy() {
        super.destroy()
        try {
            prefs.searchHiddenApps.get().asLiveData().removeObserver {
                searchHiddenAppsEnable = false
            }
        } catch (_: Exception) {}
    }

    override fun doSearch(query: String?, callback: SearchCallback<AdapterItem>?) {
        if (callback == null) return
        val safeQuery = query ?: ""
        Log.d("RPDevAppSearchAlgorithm", "doSearch: $safeQuery")
        Launcher.getLauncher(context).model.enqueueModelUpdateTask { _: ModelTaskController?, _: BgDataModel?, apps: AllAppsList? ->
            val appList = apps?.data ?: try {
                context.launcher.allApps
            } catch (_: Exception) {
                emptyList<AppInfo>()
            }
            val result = getSearchResult(appList, safeQuery)
            val suggestions: ArrayList<String> = arrayListOf()

            // On-Device Math Calculation
            if (safeQuery.isNotEmpty() && LocalMathEngine.isMathExpression(safeQuery)) {
                LocalMathEngine.evaluate(safeQuery)?.let { mathResult ->
                    suggestions.add("$safeQuery = $mathResult")
                }
            }

            if (mAddNoResultsMessage && result.isEmpty() && suggestions.isEmpty()) {
                result.add(getEmptyMessageAdapterItem(safeQuery))
            }
            mResultHandler.post { callback.onSearchResult(safeQuery, result, suggestions) }
            if (callback.showWebResults()) {
                val webSuggestions = getSuggestions(safeQuery)
                suggestions.addAll(webSuggestions)
                callback.setShowWebResults(false)
            }
            mResultHandler.post { callback.onSearchResult(safeQuery, result, suggestions) }
        }
    }

    private fun getSearchResult(apps: List<AppInfo>, query: String): ArrayList<AdapterItem> {
        val targetApps = if (searchHiddenAppsEnable) {
            try {
                context.launcher.allApps.toMutableList()
            } catch (_: Exception) {
                apps.toMutableList()
            }
        } else {
            apps.toMutableList()
        }

        return if (prefs.searchFuzzy.getValue()) {
            getFuzzySearchResult(targetApps, query)
        } else {
            getTitleMatchResultKT(targetApps, query)
        }
    }

    private fun getFuzzySearchResult(
        apps: List<AppInfo>,
        query: String,
    ): ArrayList<AdapterItem> {
        val result = ArrayList<AdapterItem>()
        if (query.isBlank() || apps.isEmpty()) return result

        try {
            val matcher = FuzzySearch.extractSorted(
                query.lowercase(Locale.getDefault()), apps,
                { it?.title?.toString() ?: "" }, WeightedRatio(), 65
            )
            var resultCount = 0
            val total = matcher.size
            var i = 0
            while (i < total && resultCount < MAX_RESULTS_COUNT) {
                val info = matcher[i].referent
                if (info != null) {
                    val appItem = AdapterItem.asApp(info)
                    result.add(appItem)
                    resultCount++
                }
                i++
            }
        } catch (e: Exception) {
            Log.e("RPDevAppSearchAlgorithm", "Fuzzy search error", e)
        }

        return result
    }

    private fun getTitleMatchResultKT(
        apps: List<AppInfo>,
        query: String,
    ): ArrayList<AdapterItem> {
        val result = ArrayList<AdapterItem>()
        if (query.isBlank() || apps.isEmpty()) return result

        val queryTextLower = query.lowercase(Locale.getDefault())
        val matcher = StringMatcherUtility.StringMatcher.getInstance()

        var resultCount = 0
        var i = 0
        val total = apps.size
        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = apps[i]
            val title = info.title?.toString() ?: ""
            if (StringMatcherUtility.matches(queryTextLower, title, matcher)) {
                val appItem = AdapterItem.asApp(info)
                result.add(appItem)
                resultCount++
            }
            i++
        }
        return result
    }

    private fun getSuggestions(query: String): ArrayList<String> {
        if (query.isBlank() || !NeoPrefs.getInstance().searchGlobal.getValue()) {
            return arrayListOf()
        }
        return try {
            val provider = SearchProviderController.getInstance(context).activeSearchProvider
            if (!provider.suggestionUrl.isNullOrEmpty()) {
                provider.getSuggestions(query)
            } else arrayListOf()
        } catch (e: Exception) {
            Log.w("RPDevAppSearchAlgorithm", "Failed to fetch web suggestions", e)
            arrayListOf()
        }
    }
}