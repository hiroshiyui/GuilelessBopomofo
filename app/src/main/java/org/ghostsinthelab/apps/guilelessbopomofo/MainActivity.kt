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
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding
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
        sharedPreferences = getSharedPreferences(APP_SHARED_PREFERENCES, MODE_PRIVATE)

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
                        sharedPreferences.edit().putString(USER_KEYBOARD_LAYOUT, layout).apply()
                    }

                    if (sharedPreferences.getString(
                            USER_KEYBOARD_LAYOUT, BopomofoKeyboards.KB_DEFAULT.layout
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
                    if (sharedPreferences.getBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, it.isChecked)
                            .apply()

                    }
                }

                switchDisplayEten26QwertyLayout.let {
                    if (sharedPreferences.getBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, it.isChecked)
                            .apply()

                    }
                }

                switchDisplayDvorakHsuBothLayout.let {
                    if (sharedPreferences.getBoolean(
                            USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(
                            USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT, it.isChecked
                        ).apply()

                    }
                }

                for ((button, conversionEngine) in mapOf(
                    radioButtonSimpleConversionEngine to ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode,
                    radioButtonChewingConversionEngine to ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
                    radioButtonFuzzyChewingConversionEngine to ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode
                )) {
                    button.setOnClickListener {
                        sharedPreferences.edit().putInt(USER_CONVERSION_ENGINE, conversionEngine).apply()
                    }

                    if (sharedPreferences.getInt(
                            USER_CONVERSION_ENGINE, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
                        ) == conversionEngine
                    ) {
                        button.isChecked = true
                    }
                }

                switchSettingSpaceAsSelection.let {
                    if (sharedPreferences.getBoolean(USER_ENABLE_SPACE_AS_SELECTION, true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_ENABLE_SPACE_AS_SELECTION, it.isChecked)
                            .apply()
                    }
                }

                switchRearwardPhraseChoice.let {
                    if (sharedPreferences.getBoolean(USER_PHRASE_CHOICE_REARWARD, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_PHRASE_CHOICE_REARWARD, it.isChecked)
                            .apply()
                    }
                }
            }

            sectionUserInterface.apply {
                var hapticFeedbackPreferenceStrength = sharedPreferences.getInt(
                    USER_HAPTIC_FEEDBACK_STRENGTH, GuilelessBopomofoService.defaultHapticFeedbackStrength
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
                        USER_HAPTIC_FEEDBACK_STRENGTH, hapticFeedbackPreferenceStrength
                    ).apply()
                    textViewSettingHapticFeedbaclCurrentStrength.text = String.format(
                        resources.getString(R.string.haptic_feedback_strength_setting), hapticFeedbackPreferenceStrength
                    )
                }

                switchSettingApplySameHapticFeedbackStrengthToFunctionButtons.let {
                    if (sharedPreferences.getBoolean(
                            SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit()
                            .putBoolean(SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS, it.isChecked).apply()
                    }
                }

                switchSettingFullscreenWhenInLandscape.let {
                    if (sharedPreferences.getBoolean(USER_FULLSCREEN_WHEN_IN_LANDSCAPE, true)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_FULLSCREEN_WHEN_IN_LANDSCAPE, it.isChecked)
                            .apply()
                    }
                }

                switchSettingFullscreenWhenInPortrait.let {
                    if (sharedPreferences.getBoolean(USER_FULLSCREEN_WHEN_IN_PORTRAIT, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_FULLSCREEN_WHEN_IN_PORTRAIT, it.isChecked)
                            .apply()
                    }
                }

                var keyButtonPreferenceHeight = sharedPreferences.getInt(USER_KEY_BUTTON_HEIGHT, 52)

                seekBarKeyButtonHeight.value = keyButtonPreferenceHeight.toFloat()

                textViewSettingKeyButtonCurrentHeight.text = String.format(
                    resources.getString(R.string.key_button_height_setting), keyButtonPreferenceHeight
                )

                seekBarKeyButtonHeight.addOnChangeListener { _, value, _ ->
                    keyButtonPreferenceHeight = value.toInt()
                    sharedPreferences.edit().putInt(USER_KEY_BUTTON_HEIGHT, keyButtonPreferenceHeight)
                        .apply()
                    textViewSettingKeyButtonCurrentHeight.text = String.format(
                        resources.getString(R.string.key_button_height_setting), keyButtonPreferenceHeight
                    )
                }

                switchSettingEnableImeSwitch.let {
                    if (sharedPreferences.getBoolean(USER_ENABLE_IME_SWITCH, false)) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_ENABLE_IME_SWITCH, it.isChecked).apply()
                    }
                }

                switchSettingImeSwitch.let {
                    if (sharedPreferences.getBoolean(
                            USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false
                        )
                    ) {
                        it.isChecked = true
                    }

                    it.setOnCheckedChangeListener { _, _ ->
                        sharedPreferences.edit().putBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, it
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
                        sharedPreferences.edit().putString(USER_CANDIDATE_SELECTION_KEYS_OPTION, keys).apply()
                    }

                    if (sharedPreferences.getString(
                            USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set
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
