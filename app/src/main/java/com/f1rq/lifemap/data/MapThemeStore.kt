package com.f1rq.lifemap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifemap_prefs")

object MapThemeStore {
    private val MAP_THEME_KEY = stringPreferencesKey("map_theme")

    fun mapThemeFlow(context: Context): Flow<MapTheme> =
        context.dataStore.data
            .map { prefs: Preferences ->
                val name: String? = prefs[MAP_THEME_KEY]
                MapTheme.values().firstOrNull { it.name == name } ?: MapTheme.POSITRON
            }
            .distinctUntilChanged()

    suspend fun setMapTheme(context: Context, theme: MapTheme) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[MAP_THEME_KEY] = theme.name
        }
    }
}