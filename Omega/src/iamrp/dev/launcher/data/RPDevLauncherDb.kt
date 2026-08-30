/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package iamrp.dev.launcher.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.launcher3.util.MainThreadInitializedObject
import iamrp.dev.launcher.data.models.AppTracker
import iamrp.dev.launcher.data.models.GestureItemInfo
import iamrp.dev.launcher.data.models.IconOverride
import iamrp.dev.launcher.data.models.PeopleInfo
import iamrp.dev.launcher.data.models.SearchProvider
import iamrp.dev.launcher.data.models.SearchProvider.Companion.addedProvidersV6
import iamrp.dev.launcher.data.models.SearchProvider.Companion.defaultProviders
import iamrp.dev.launcher.data.models.SearchProvider.Companion.offlineSearchProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin

@Database(
    entities = [
        IconOverride::class,
        AppTracker::class,
        PeopleInfo::class,
        GestureItemInfo::class,
        SearchProvider::class,
    ],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 4,
            to = 5,
        ),
        AutoMigration(
            from = 5,
            to = 6,
            spec = RPDevLauncherDb.Companion.MigrationSpec5to6::class
        ),
        AutoMigration(
            from = 6,
            to = 7,
            spec = RPDevLauncherDb.Companion.MigrationSpec6to7::class
        ),
        AutoMigration(
            from = 5,
            to = 7,
            spec = RPDevLauncherDb.Companion.MigrationSpec5to7::class
        ),
        AutoMigration(
            from = 7,
            to = 8,
        ),
    ]
)
@TypeConverters(Converters::class)
abstract class RPDevLauncherDb : RoomDatabase() {

    abstract fun iconOverrideDao(): IconOverrideDao
    abstract fun appTrackerDao(): AppTrackerDao
    abstract fun peopleDao(): PeopleDao
    abstract fun gestureItemInfoDao(): GestureItemInfoDao
    abstract fun searchProviderDao(): SearchProviderDao

    companion object {

        @OptIn(DelicateCoroutinesApi::class)
        val INSTANCE = MainThreadInitializedObject { context ->
            Room.databaseBuilder(context, RPDevLauncherDb::class.java, "RPDevLauncher.db")
                .build()
                .apply {
                    GlobalScope.launch(Dispatchers.IO) {
                        if (searchProviderDao().getCount() == 0) {
                            searchProviderDao().insert(offlineSearchProvider(context))
                            defaultProviders.forEach(searchProviderDao()::insert)
                        }
                    }
                }
        }

        class MigrationSpec5to6 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(5, 6)
            }
        }

        class MigrationSpec6to7 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(6, 7)
            }
        }

        class MigrationSpec5to7 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(5, 7)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun onPostMigrate(from: Int, to: Int) {
            val preRepos = mutableListOf<SearchProvider>()
            if (from == 5) preRepos.addAll(addedProvidersV6)
            GlobalScope.launch(Dispatchers.IO) {
                preRepos.forEach {
                    getKoin().get<SearchProviderRepository>().insert(it)
                }
                if (to == 7) GlobalScope.launch(Dispatchers.IO) {
                    getKoin().get<SearchProviderRepository>().emptyTable()
                    defaultProviders.forEach {
                        getKoin().get<SearchProviderRepository>().insert(it)
                    }
                }
            }
        }
    }
}

val reposModule = module {
    single { SearchProviderRepository(get()) }
    single { PeopleRepository(get()) }
    // TODO migrate others
}