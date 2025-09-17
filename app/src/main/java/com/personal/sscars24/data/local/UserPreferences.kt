package com.personal.sscars24.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ss_prefs")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {

    companion object {
        val Token = stringPreferencesKey("access_token")
    }

    suspend fun <T> storeData(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    fun <T> getData(key: Preferences.Key<T>): Flow<T?> =
        context.dataStore.data.map { it[key] }

    suspend fun <T> removeData(key: Preferences.Key<T>) {
        context.dataStore.edit { it.remove(key) }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    /*val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            version = prefs[Version],
            isLoggedIn = prefs[IsLoggedIn] ?: false
        )
    }

    suspend fun saveUserSession(version: String, isLoggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Version] = version
            prefs[IsLoggedIn] = isLoggedIn
        }
    }*/
}

data class UserSession(
    val version: String?,
    val isLoggedIn: Boolean
)
