package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.SharedPreferences
import android.content.res.Configuration

interface PhysicalKeyboardDetectable {
    val sharedPreferences: SharedPreferences

    fun physicalKeyboardEnabled(): Boolean {
        if ((resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY) &&
            (resources.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) &&
            sharedPreferences.getBoolean(
                "user_enable_physical_keyboard", false
            )
        ) {
            return true
        }
        return false
    }
}