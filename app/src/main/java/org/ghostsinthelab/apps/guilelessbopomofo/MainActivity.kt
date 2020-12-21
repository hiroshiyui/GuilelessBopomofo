/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val LOGTAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var engineeringModeEnterCount: Int = 0
    private val engineeringModeEnterClicks: Int = 5
    private var engineeringModeEnabled: Boolean = false
    private val imeSettingsRequestCode: Int = 254
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(LOGTAG, "onCreate()")
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.apply {
            textViewAppVersion.text =
                applicationContext.packageManager.getPackageInfo(
                    this@MainActivity.packageName,
                    0
                ).versionName

            imageViewAppIcon.setOnClickListener {
                if (engineeringModeEnterCount >= engineeringModeEnterClicks || engineeringModeEnabled) {
                    engineeringModeEnabled = true
                    val engineeringModeIntent =
                        Intent(this@MainActivity, EngineeringModeActivity::class.java)
                    startActivity(engineeringModeIntent)
                } else {
                    engineeringModeEnterCount += 1
                }

                return@setOnClickListener
            }

            buttonLaunchImeSystemSettings.setOnClickListener {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                startActivityForResult(intent, imeSettingsRequestCode)
            }

            textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()

            if (sharedPreferences.getBoolean("user_fullscreen_when_in_landscape", true)) {
                switchSettingFullscreenWhenInLandscape.isChecked = true
            }

            switchSettingFullscreenWhenInLandscape.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("user_fullscreen_when_in_landscape", true)
                        .apply()
                } else {
                    sharedPreferences.edit().putBoolean("user_fullscreen_when_in_landscape", false)
                        .apply()
                }
            }

            if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                switchSettingKeyButtonsElevation.isChecked = true
            }

            switchSettingKeyButtonsElevation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("user_enable_button_elevation", true)
                        .apply()
                } else {
                    sharedPreferences.edit().putBoolean("user_enable_button_elevation", false)
                        .apply()
                }
            }

            if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                switchSettingSpaceAsSelection.isChecked = true
            }

            switchSettingSpaceAsSelection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("user_enable_space_as_selection", true)
                        .apply()
                } else {
                    sharedPreferences.edit().putBoolean("user_enable_space_as_selection", false)
                        .apply()
                }
            }

            if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                switchRearwardPhraseChoice.isChecked = true
            }

            switchRearwardPhraseChoice.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("user_phrase_choice_rearward", true).apply()
                } else {
                    sharedPreferences.edit().putBoolean("user_phrase_choice_rearward", false)
                        .apply()
                }
            }
        }

        for ((button, layout) in
        mapOf<RadioButton, String>(
            binding.radioButtonLayoutDaChen to "KB_DEFAULT",
            binding.radioButtonLayoutETen26 to "KB_ET26",
            binding.radioButtonLayoutHsu to "KB_HSU"
        )) {
            button.setOnClickListener {
                sharedPreferences.edit().putString("user_keyboard_layout", layout).apply()
            }

            if (sharedPreferences.getString(
                    "user_keyboard_layout",
                    GuilelessBopomofoService.defaultKeyboardLayout
                ) == layout
            ) {
                button.isChecked = true
            }
        }

        val view = binding.root
        setContentView(view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imeSettingsRequestCode) {
            binding.textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()
        }
    }

    private fun isGuilelessBopomofoEnabled(): Boolean {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList

        enabledInputMethodList.forEach {
            if (it.serviceName == "org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService") {
                return true
            }
        }
        return false
    }

    private fun currentGuilelessBopomofoServiceStatus(): String {
        return if (isGuilelessBopomofoEnabled()) getString(R.string.service_is_enabled) else getString(
            R.string.service_is_disabled
        )
    }
}
