package com.ion.daily_tracking.data

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Tiny key/value store for profile info that doesn't belong in the activity database. */
class UserPrefs(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val displayName: Flow<String> = callbackFlow {
        trySend(currentName())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_NAME) trySend(currentName())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun currentName(): String = prefs.getString(KEY_NAME, DEFAULT_NAME) ?: DEFAULT_NAME

    fun setDisplayName(name: String) {
        prefs.edit().putString(KEY_NAME, name.ifBlank { DEFAULT_NAME }).apply()
    }

    companion object {
        private const val KEY_NAME = "display_name"
        private const val DEFAULT_NAME = "Friend"
    }
}
