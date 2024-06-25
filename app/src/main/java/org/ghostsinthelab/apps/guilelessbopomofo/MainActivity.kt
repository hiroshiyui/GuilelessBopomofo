/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibrable

class MainActivity : AppCompatActivity(), Vibrable {
    private val logTag: String = "MainActivity"

    // ViewBinding
    private lateinit var viewBinding: ActivityMainBinding

    private var engineeringModeEnterCount: Int = 0
    private val engineeringModeEnterClicks: Int = 5
    private var engineeringModeEnabled: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "onCreate()")
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        viewBinding.apply {
            textViewAppVersion.text =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    applicationContext.packageManager.getPackageInfo(
                        this@MainActivity.packageName,
                        0
                    ).versionName
                } else { // from API Level 33 (Android 13 TIRAMISU):
                    applicationContext.packageManager.getPackageInfo(
                        this@MainActivity.packageName,
                        PackageInfoFlags.of(0)
                    ).versionName
                }

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

            sectionGeneral.apply {
                val startImeSystemSettingActivity =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()
                    }

                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)

                if (!isGuilelessBopomofoEnabled()) {
                    Toast.makeText(applicationContext, R.string.please_enable_guileless_bopomofo_first, Toast.LENGTH_LONG).show()
                    startImeSystemSettingActivity.launch(intent)
                }

                buttonLaunchImeSystemSettings.setOnClickListener {
                    startImeSystemSettingActivity.launch(intent)
                }

                textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()
                textViewServiceStatus.setTextColor(getColor(R.color.colorAccent))

                for ((button, layout) in
                mapOf(
                    radioButtonLayoutDaChen to "KB_DEFAULT",
                    radioButtonLayoutETen26 to "KB_ET26",
                    radioButtonLayoutHsu to "KB_HSU",
                    radioButtonLayoutDvorakHsu to "KB_DVORAK_HSU",
                    radioButtonLayoutETen41 to "KB_ET"
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

                switchDisplayHsuQwertyLayout.let {
                    if (sharedPreferences.getBoolean("user_display_hsu_qwerty_layout", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_display_hsu_qwerty_layout", it.isChecked).apply()

                    }
                }

                switchDisplayEten26QwertyLayout.let {
                    if (sharedPreferences.getBoolean("user_display_eten26_qwerty_layout", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_display_eten26_qwerty_layout", it.isChecked).apply()

                    }
                }

                switchDisplayDvorakHsuBothLayout.let {
                    if (sharedPreferences.getBoolean(
                            "user_display_dvorak_hsu_both_layout",
                            false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_display_dvorak_hsu_both_layout", it.isChecked).apply()

                    }
                }

                switchSettingSpaceAsSelection.let {
                    if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enable_space_as_selection", it.isChecked).apply()
                    }
                }

                switchRearwardPhraseChoice.let {
                    if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_phrase_choice_rearward", it.isChecked).apply()
                    }
                }
            }

            sectionUserInterface.apply {
                var hapticFeedbackPreferenceStrength =
                    sharedPreferences.getInt(
                        "user_haptic_feedback_strength",
                        GuilelessBopomofoService.defaultHapticFeedbackStrength
                    )

                textViewSettingHapticFeedbaclCurrentStrength.text =
                    String.format(
                        resources.getString(R.string.haptic_feedback_strength_setting),
                        hapticFeedbackPreferenceStrength
                    )

                seekBarHapticFeedbackStrength.value = hapticFeedbackPreferenceStrength.toFloat()
                seekBarHapticFeedbackStrength.addOnChangeListener { _, value, _ ->
                    hapticFeedbackPreferenceStrength = value.toInt()
                    performVibration(
                        applicationContext,
                        hapticFeedbackPreferenceStrength
                    )

                    sharedPreferences.edit()
                        .putInt(
                            "user_haptic_feedback_strength",
                            hapticFeedbackPreferenceStrength
                        ).apply()
                    textViewSettingHapticFeedbaclCurrentStrength.text =
                        String.format(
                            resources.getString(R.string.haptic_feedback_strength_setting),
                            hapticFeedbackPreferenceStrength
                        )
                }

                switchSettingApplySameHapticFeedbackStrengthToFunctionButtons.let {
                    if (sharedPreferences.getBoolean(
                            "same_haptic_feedback_to_function_buttons",
                            false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("same_haptic_feedback_to_function_buttons", it.isChecked)
                            .apply()
                    }
                }

                switchSettingFullscreenWhenInLandscape.let {
                    if (sharedPreferences.getBoolean("user_fullscreen_when_in_landscape", true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_fullscreen_when_in_landscape", it.isChecked).apply()
                    }
                }

                switchSettingFullscreenWhenInPortrait.let {
                    if (sharedPreferences.getBoolean("user_fullscreen_when_in_portrait", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_fullscreen_when_in_portrait", it.isChecked).apply()
                    }
                }

                switchSettingKeyButtonsElevation.let {
                    if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enable_button_elevation", it.isChecked).apply()
                    }
                }

                var keyButtonPreferenceHeight =
                    sharedPreferences.getInt("user_key_button_height", 52)

                seekBarKeyButtonHeight.value = keyButtonPreferenceHeight.toFloat()

                textViewSettingKeyButtonCurrentHeight.text =
                    String.format(
                        resources.getString(R.string.key_button_height_setting),
                        keyButtonPreferenceHeight
                    )

                seekBarKeyButtonHeight.addOnChangeListener { _, value, _ ->
                    keyButtonPreferenceHeight = value.toInt()
                    sharedPreferences.edit()
                        .putInt("user_key_button_height", keyButtonPreferenceHeight).apply()
                    textViewSettingKeyButtonCurrentHeight.text =
                        String.format(
                            resources.getString(R.string.key_button_height_setting),
                            keyButtonPreferenceHeight
                        )
                }

                switchSettingImeSwitch.let {
                    if (sharedPreferences.getBoolean(
                            "user_enable_double_touch_ime_switch",
                            false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enable_double_touch_ime_switch", it.isChecked).apply()
                    }
                }
            }

            sectionPhysicalKeyboard.apply {
                switchSettingEnablePhysicalKeyboard.let {
                    if (sharedPreferences.getBoolean("user_enable_physical_keyboard", false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enable_physical_keyboard", it.isChecked).apply()
                    }
                }

                for ((button, keys) in
                mapOf(
                    radioButtonNumberRow to "NUMBER_ROW",
                    radioButtonHomeRow to "HOME_ROW",
                    radioButtonHomeTabMixedMode1 to "HOME_TAB_MIXED_MODE1",
                    radioButtonHomeTabMixedMode2 to "HOME_TAB_MIXED_MODE2",
                    radioButtonDvorakHomeRow to "DVORAK_HOME_ROW",
                    radioButtonDvorakMixedMode to "DVORAK_MIXED_MODE"
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit()
                            .putString("user_candidate_selection_keys_option", keys)
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
            }
        }

        val view = viewBinding.root
        setContentView(view)
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
