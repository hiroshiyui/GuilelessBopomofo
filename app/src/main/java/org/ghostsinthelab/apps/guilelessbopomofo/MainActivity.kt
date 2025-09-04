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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.RegisteredSharedPreferences
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EdgeToEdge
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class MainActivity : AppCompatActivity(), Vibratable, EdgeToEdge {
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
        enableEdgeToEdge()
        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        viewBinding.apply {
            textViewAppVersion.text = getString(
                R.string.app_version, BuildConfig.VERSION_NAME, ChewingBridge.chewing.version()
            )

            imageViewAppIcon.setOnClickListener {
                if (engineeringModeEnterCount >= engineeringModeEnterClicks || engineeringModeEnabled) {
                    engineeringModeEnabled = true
                    val engineeringModeIntent = Intent(this@MainActivity, EngineeringModeActivity::class.java)
                    startActivity(engineeringModeIntent)
                } else {
                    engineeringModeEnterCount += 1
                }

                return@setOnClickListener
            }

            sectionGeneral.apply {
                val startImeSystemSettingActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()
                }

                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)

                if (!isGuilelessBopomofoEnabled()) {
                    Toast.makeText(
                        applicationContext, R.string.please_enable_guileless_bopomofo_first, Toast.LENGTH_LONG
                    ).show()
                    startImeSystemSettingActivity.launch(intent)
                }

                buttonLaunchImeSystemSettings.setOnClickListener {
                    startImeSystemSettingActivity.launch(intent)
                }

                textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()

                for ((button, layout) in mapOf(
                    radioButtonLayoutDaChen to BopomofoKeyboards.KB_DEFAULT.layout,
                    radioButtonLayoutETen26 to BopomofoKeyboards.KB_ET26.layout,
                    radioButtonLayoutHsu to BopomofoKeyboards.KB_HSU.layout,
                    radioButtonLayoutDvorakHsu to BopomofoKeyboards.KB_DVORAK_HSU.layout,
                    radioButtonLayoutETen41 to BopomofoKeyboards.KB_ET.layout
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putString(RegisteredSharedPreferences.USER_KEYBOARD_LAYOUT.key, layout).apply()
                    }

                    if (sharedPreferences.getString(
                            RegisteredSharedPreferences.USER_KEYBOARD_LAYOUT.key, BopomofoKeyboards.KB_DEFAULT.layout
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
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_DISPLAY_HSU_QWERTY_LAYOUT.key, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_DISPLAY_HSU_QWERTY_LAYOUT.key, it.isChecked)
                            .apply()

                    }
                }

                switchDisplayEten26QwertyLayout.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_DISPLAY_ETEN26_QWERTY_LAYOUT.key, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_DISPLAY_ETEN26_QWERTY_LAYOUT.key, it.isChecked)
                            .apply()

                    }
                }

                switchDisplayDvorakHsuBothLayout.let {
                    if (sharedPreferences.getBoolean(
                            RegisteredSharedPreferences.USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT.key, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(
                            RegisteredSharedPreferences.USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT.key, it.isChecked
                        ).apply()

                    }
                }

                for ((button, conversionEngine) in mapOf(
                    radioButtonSimpleConversionEngine to ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode,
                    radioButtonChewingConversionEngine to ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
                    radioButtonFuzzyChewingConversionEngine to ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putInt(RegisteredSharedPreferences.USER_CONVERSION_ENGINE.key, conversionEngine).apply()
                    }

                    if (sharedPreferences.getInt(
                            RegisteredSharedPreferences.USER_CONVERSION_ENGINE.key, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
                        ) == conversionEngine
                    ) {
                        button.isChecked = true
                    }
                }

                switchSettingSpaceAsSelection.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_ENABLE_SPACE_AS_SELECTION.key, true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_ENABLE_SPACE_AS_SELECTION.key, it.isChecked)
                            .apply()
                    }
                }

                switchRearwardPhraseChoice.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_PHRASE_CHOICE_REARWARD.key, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_PHRASE_CHOICE_REARWARD.key, it.isChecked)
                            .apply()
                    }
                }
            }

            sectionUserInterface.apply {
                var hapticFeedbackPreferenceStrength = sharedPreferences.getInt(
                    RegisteredSharedPreferences.USER_HAPTIC_FEEDBACK_STRENGTH.key, GuilelessBopomofoService.defaultHapticFeedbackStrength
                )

                textViewSettingHapticFeedbaclCurrentStrength.text = String.format(
                    resources.getString(R.string.haptic_feedback_strength_setting), hapticFeedbackPreferenceStrength
                )

                seekBarHapticFeedbackStrength.value = hapticFeedbackPreferenceStrength.toFloat()
                seekBarHapticFeedbackStrength.addOnChangeListener { _, value, _ ->
                    hapticFeedbackPreferenceStrength = value.toInt()
                    performVibration(
                        applicationContext, hapticFeedbackPreferenceStrength
                    )

                    sharedPreferences.edit().putInt(
                        RegisteredSharedPreferences.USER_HAPTIC_FEEDBACK_STRENGTH.key, hapticFeedbackPreferenceStrength
                    ).apply()
                    textViewSettingHapticFeedbaclCurrentStrength.text = String.format(
                        resources.getString(R.string.haptic_feedback_strength_setting), hapticFeedbackPreferenceStrength
                    )
                }

                switchSettingApplySameHapticFeedbackStrengthToFunctionButtons.let {
                    if (sharedPreferences.getBoolean(
                            RegisteredSharedPreferences.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS.key, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean(RegisteredSharedPreferences.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS.key, it.isChecked).apply()
                    }
                }

                switchSettingFullscreenWhenInLandscape.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_FULLSCREEN_WHEN_IN_LANDSCAPE.key, true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_FULLSCREEN_WHEN_IN_LANDSCAPE.key, it.isChecked)
                            .apply()
                    }
                }

                switchSettingFullscreenWhenInPortrait.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_FULLSCREEN_WHEN_IN_PORTRAIT.key, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_FULLSCREEN_WHEN_IN_PORTRAIT.key, it.isChecked)
                            .apply()
                    }
                }

                var keyButtonPreferenceHeight = sharedPreferences.getInt(RegisteredSharedPreferences.USER_KEY_BUTTON_HEIGHT.key, 52)

                seekBarKeyButtonHeight.value = keyButtonPreferenceHeight.toFloat()

                textViewSettingKeyButtonCurrentHeight.text = String.format(
                    resources.getString(R.string.key_button_height_setting), keyButtonPreferenceHeight
                )

                seekBarKeyButtonHeight.addOnChangeListener { _, value, _ ->
                    keyButtonPreferenceHeight = value.toInt()
                    sharedPreferences.edit().putInt(RegisteredSharedPreferences.USER_KEY_BUTTON_HEIGHT.key, keyButtonPreferenceHeight)
                        .apply()
                    textViewSettingKeyButtonCurrentHeight.text = String.format(
                        resources.getString(R.string.key_button_height_setting), keyButtonPreferenceHeight
                    )
                }

                switchSettingEnableImeSwitch.let {
                    if (sharedPreferences.getBoolean(RegisteredSharedPreferences.USER_ENABLE_IME_SWITCH.key, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_ENABLE_IME_SWITCH.key, it.isChecked).apply()
                    }
                }

                switchSettingImeSwitch.let {
                    if (sharedPreferences.getBoolean(
                            RegisteredSharedPreferences.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH.key, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(RegisteredSharedPreferences.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH.key, it
                            .isChecked).apply()
                    }
                }
            }

            sectionPhysicalKeyboard.apply {
                for ((button, keys) in mapOf(
                    radioButtonNumberRow to SelectionKeys.NUMBER_ROW.set,
                    radioButtonHomeRow to SelectionKeys.HOME_ROW.set,
                    radioButtonHomeTabMixedMode1 to SelectionKeys.HOME_TAB_MIXED_MODE1.set,
                    radioButtonHomeTabMixedMode2 to SelectionKeys.HOME_TAB_MIXED_MODE2.set,
                    radioButtonDvorakHomeRow to SelectionKeys.DVORAK_HOME_ROW.set,
                    radioButtonDvorakMixedMode to SelectionKeys.DVORAK_MIXED_MODE.set,
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putString(RegisteredSharedPreferences.USER_CANDIDATE_SELECTION_KEYS_OPTION.key, keys).apply()
                    }

                    if (sharedPreferences.getString(
                            RegisteredSharedPreferences.USER_CANDIDATE_SELECTION_KEYS_OPTION.key, SelectionKeys.NUMBER_ROW.set
                        ) == keys
                    ) {
                        button.isChecked = true
                    }
                }
            }
        }

        val view = viewBinding.root
        applyInsetsAsMargins(view)
        setContentView(view)
    }

    private fun isGuilelessBopomofoEnabled(): Boolean {
        val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
