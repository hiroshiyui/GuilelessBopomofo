package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.SharedPreferences
import android.content.res.Configuration

interface PhysicalKeyboardDetectable {
    val sharedPreferences: SharedPreferences

    fun physicalKeyboardEnabled(): Boolean {
        return (resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY) &&
                (resources.configuration.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES) &&
                sharedPreferences.getBoolean(
                    "user_enable_physical_keyboard", false
                )
    }
}