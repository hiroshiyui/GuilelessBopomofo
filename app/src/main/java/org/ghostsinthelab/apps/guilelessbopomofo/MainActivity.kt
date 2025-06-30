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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class MainActivity : AppCompatActivity(), Vibratable {
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
            textViewAppVersion.text = getString(
                R.string.app_version,
                BuildConfig.VERSION_NAME,
                ChewingBridge.chewing.version()
            )

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
                    Toast.makeText(
                        applicationContext,
                        R.string.please_enable_guileless_bopomofo_first,
                        Toast.LENGTH_LONG
                    ).show()
                    startImeSystemSettingActivity.launch(intent)
                }

                buttonLaunchImeSystemSettings.setOnClickListener {
                    startImeSystemSettingActivity.launch(intent)
                }

                textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()

                for ((button, layout) in
                mapOf(
                    radioButtonLayoutDaChen to BopomofoKeyboards.KB_DEFAULT.layout,
                    radioButtonLayoutETen26 to BopomofoKeyboards.KB_ET26.layout,
                    radioButtonLayoutHsu to BopomofoKeyboards.KB_HSU.layout,
                    radioButtonLayoutDvorakHsu to BopomofoKeyboards.KB_DVORAK_HSU.layout,
                    radioButtonLayoutETen41 to BopomofoKeyboards.KB_ET.layout
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putString("user_keyboard_layout", layout).apply()
                    }

                    if (sharedPreferences.getString(
                            "user_keyboard_layout",
                            BopomofoKeyboards.KB_DEFAULT.layout
                        ) == layout
                    ) {
                        button.isChecked = true
                    }
                }

                radioButtonLayoutHsu.apply {
                    this.setOnCheckedChangeListener { _, isChecked ->
                        switchDisplayHsuQwertyLayout.isGone = !isChecked
                    }

                    switchDisplayHsuQwertyLayout.isGone = !this.isChecked
                }

                radioButtonLayoutDvorakHsu.apply {
                    this.setOnCheckedChangeListener { _, isChecked ->
                        switchDisplayDvorakHsuBothLayout.isGone = !isChecked
                    }

                    switchDisplayDvorakHsuBothLayout.isGone = !this.isChecked
                }

                radioButtonLayoutETen26.apply {
                    this.setOnCheckedChangeListener { _, isChecked ->
                        switchDisplayEten26QwertyLayout.isGone = !isChecked
                    }

                    switchDisplayEten26QwertyLayout.isGone = !this.isChecked
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

                for ((button, conversionEngine) in
                mapOf(
                    radioButtonSimpleConversionEngine to ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode,
                    radioButtonChewingConversionEngine to ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
                    radioButtonFuzzyChewingConversionEngine to ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putInt("user_conversion_engine", conversionEngine)
                            .apply()
                    }

                    if (sharedPreferences.getInt(
                            "user_conversion_engine",
                            ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
                        ) == conversionEngine
                    ) {
                        button.isChecked = true
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
                    if (sharedPreferences.getBoolean("user_enable_physical_keyboard", true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enable_physical_keyboard", it.isChecked).apply()
                    }
                }

                switchSettingEnhancedCompatPhysicalKeyboard.let {
                    if (sharedPreferences.getBoolean(
                            "user_enhanced_compat_physical_keyboard",
                            false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean("user_enhanced_compat_physical_keyboard", it.isChecked)
                            .apply()
                    }
                }

                for ((button, keys) in
                mapOf(
                    radioButtonNumberRow to SelectionKeys.NUMBER_ROW.set,
                    radioButtonHomeRow to SelectionKeys.HOME_ROW.set,
                    radioButtonHomeTabMixedMode1 to SelectionKeys.HOME_TAB_MIXED_MODE1.set,
                    radioButtonHomeTabMixedMode2 to SelectionKeys.HOME_TAB_MIXED_MODE2.set,
                    radioButtonDvorakHomeRow to SelectionKeys.DVORAK_HOME_ROW.set,
                    radioButtonDvorakMixedMode to SelectionKeys.DVORAK_MIXED_MODE.set,
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit()
                            .putString("user_candidate_selection_keys_option", keys)
                            .apply()
                    }

                    if (sharedPreferences.getString(
                            "user_candidate_selection_keys_option",
                            SelectionKeys.NUMBER_ROW.set
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
