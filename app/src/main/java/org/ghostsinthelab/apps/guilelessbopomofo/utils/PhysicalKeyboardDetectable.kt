package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.SharedPreferences
import android.content.res.Configuration

interface PhysicalKeyboardDetectable {
    val sharedPreferences: SharedPreferences

    fun physicalKeyboardEnabled(): Boolean {
        if ((resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY)
            && sharedPreferences.getBoolean(
                "user_enable_physical_keyboard",
                false
            )
        ) {
            return true
        }
        return false
    }
}