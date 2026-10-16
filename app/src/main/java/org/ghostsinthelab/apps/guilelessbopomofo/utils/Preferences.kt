/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES

/**
 * The one and only preferences file this app keeps, so that nobody has to spell out its
 * name and mode again.
 */
val Context.appSharedPreferences: SharedPreferences
    get() = getSharedPreferences(APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)

/** [key]'s value as Compose state; assigning to it writes the preference back. */
@Composable
fun rememberBooleanPreference(key: String, defaultValue: Boolean): MutableState<Boolean> =
    rememberPreference(key, { getBoolean(key, defaultValue) }, { putBoolean(key, it) })

/** [key]'s value as Compose state; assigning to it writes the preference back. */
@Composable
fun rememberIntPreference(key: String, defaultValue: Int): MutableState<Int> =
    rememberPreference(key, { getInt(key, defaultValue) }, { putInt(key, it) })

/** [key]'s value as Compose state; assigning to it writes the preference back. */
@Composable
fun rememberStringPreference(key: String, defaultValue: String): MutableState<String> =
    rememberPreference(key, { getString(key, defaultValue) ?: defaultValue }, { putString(key, it) })

@Composable
private fun <T> rememberPreference(
    key: String,
    read: SharedPreferences.() -> T,
    write: SharedPreferences.Editor.(T) -> Unit,
): MutableState<T> {
    val sharedPreferences = LocalContext.current.appSharedPreferences
    val state = remember(key) {
        PreferenceState(mutableStateOf(sharedPreferences.read())) { value ->
            sharedPreferences.edit { write(value) }
        }
    }

    // Follow changes made elsewhere (e.g. by the IME) while this screen is showing.
    DisposableEffect(key) {
        // SharedPreferences only keeps a weak reference, so hold the listener here.
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (changedKey == key) state.backing.value = prefs.read()
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return state
}

private class PreferenceState<T>(
    val backing: MutableState<T>,
    private val persist: (T) -> Unit,
) : MutableState<T> {
    override var value: T
        get() = backing.value
        set(newValue) {
            if (backing.value == newValue) return
            backing.value = newValue
            persist(newValue)
        }

    override fun component1(): T = value
    override fun component2(): (T) -> Unit = { value = it }
}
