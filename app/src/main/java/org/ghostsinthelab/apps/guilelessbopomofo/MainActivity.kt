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
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

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
            textViewServiceStatus.setTextColor(getColor(R.color.colorAccent))

            switchSettingFullscreenWhenInLandscape.let {
                if (sharedPreferences.getBoolean("user_fullscreen_when_in_landscape", true)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_fullscreen_when_in_landscape", isChecked).apply()
                }

            }

            switchSettingFullscreenWhenInPortrait.let {
                if (sharedPreferences.getBoolean("user_fullscreen_when_in_portrait", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_fullscreen_when_in_portrait", isChecked).apply()
                }
            }

            switchSettingKeyButtonsElevation.let {
                if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_enable_button_elevation", isChecked).apply()
                }
            }

            switchSettingSpaceAsSelection.let {
                if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_enable_space_as_selection", isChecked).apply()
                }
            }

            switchRearwardPhraseChoice.let {
                if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_phrase_choice_rearward", isChecked).apply()
                }
            }

            switchSettingEnablePhysicalKeyboard.let {
                if (sharedPreferences.getBoolean("user_enable_physical_keyboard", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit()
                        .putBoolean("user_enable_physical_keyboard", isChecked).apply()
                }
            }

            val seekBarShiftValue = 40
            var keyButtonPreferenceHeight = sharedPreferences.getInt("user_key_button_height", 52)
            seekBarKeyButtonHeight.progress = keyButtonPreferenceHeight - seekBarShiftValue
            textViewSettingKeyButtonCurrentHeight.text = "${keyButtonPreferenceHeight}dp"

            seekBarKeyButtonHeight.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    keyButtonPreferenceHeight = progress + seekBarShiftValue
                    sharedPreferences.edit()
                        .putInt("user_key_button_height", keyButtonPreferenceHeight).apply()
                    textViewSettingKeyButtonCurrentHeight.text = "${keyButtonPreferenceHeight}dp"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })

            switchDisplayHsuQwertyLayout.let {
                if (sharedPreferences.getBoolean("user_display_hsu_qwerty_layout", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit().putBoolean("user_display_hsu_qwerty_layout", isChecked).apply()

                }
            }

            switchDisplayEten26QwertyLayout.let {
                if (sharedPreferences.getBoolean("user_display_eten26_qwerty_layout", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit().putBoolean("user_display_eten26_qwerty_layout", isChecked).apply()

                }
            }

            switchDisplayDvorakHsuBothLayout.let {
                if (sharedPreferences.getBoolean("user_display_dvorak_hsu_both_layout", false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, isChecked ->
                    sharedPreferences.edit().putBoolean("user_display_dvorak_hsu_both_layout", isChecked).apply()

                }
            }
        }

        for ((button, layout) in
        mapOf<RadioButton, String>(
            binding.radioButtonLayoutDaChen to "KB_DEFAULT",
            binding.radioButtonLayoutETen26 to "KB_ET26",
            binding.radioButtonLayoutHsu to "KB_HSU",
            binding.radioButtonLayoutDvorakHsu to "KB_DVORAK_HSU"
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

        for ((button, strength) in
        mapOf<RadioButton, Long>(
            binding.radioButtonHapticFeedbackStrengthLight to Vibratable.VibrationStrength.LIGHT.strength,
            binding.radioButtonHapticFeedbackStrengthMedium to Vibratable.VibrationStrength.NORMAL.strength,
            binding.radioButtonHapticFeedbackStrengthHeavy to Vibratable.VibrationStrength.STRONG.strength
        )) {
            button.setOnClickListener {
                sharedPreferences.edit().putInt("user_haptic_feedback_strength", strength.toInt())
                    .apply()
            }

            if (sharedPreferences.getInt(
                    "user_haptic_feedback_strength",
                    GuilelessBopomofoService.defaultHapticFeedbackStrength
                ) == strength.toInt()
            ) {
                button.isChecked = true
            }
        }

        for ((button, keys) in
        mapOf<RadioButton, String>(
            binding.radioButtonNumberRow to "NUMBER_ROW",
            binding.radioButtonHomeRow to "HOME_ROW",
            binding.radioButtonHomeTabMixedMode1 to "HOME_TAB_MIXED_MODE1",
            binding.radioButtonHomeTabMixedMode2 to "HOME_TAB_MIXED_MODE2",
            binding.radioButtonDvorakHomeRow to "DVORAK_HOME_ROW",
            binding.radioButtonDvorakMixedMode to "DVORAK_MIXED_MODE"
        )) {
            button.setOnClickListener {
                sharedPreferences.edit().putString("user_candidate_selection_keys_option", keys)
                    .apply()
            }

            if (sharedPreferences.getString(
                    "user_candidate_selection_keys_option",
                    "NUMBER_ROW"
                ) == keys
            ) {
                button.isChecked = true
            }
        }

        for ((button, keymap) in
        mapOf<RadioButton, String>(
            binding.radioButtonPhysicalKeyboardKeymapQwerty to "KEYMAP_QWERTY",
            binding.radioButtonPhysicalKeyboardKeymapDvorak to "KEYMAP_DVORAK"
        )) {
            button.setOnClickListener {
                sharedPreferences.edit().putString("user_physical_keyboard_keymap_option", keymap)
                    .apply()
            }

            if (sharedPreferences.getString(
                    "user_physical_keyboard_keymap_option",
                    "KEYMAP_QWERTY"
                ) == keymap
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
