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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.FragmentGeneralSettingsBinding
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.ghostsinthelab.apps.guilelessbopomofo.utils.bindRadioGroupToPreference
import org.ghostsinthelab.apps.guilelessbopomofo.utils.bindToPreference

class GeneralSettingsFragment : ViewBindingFragment<FragmentGeneralSettingsBinding>() {
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        // In the order R.array.on_screen_bopomofo_keyboard_layouts names them.
        private val SOFT_KEYBOARD_LAYOUTS: List<String> = listOf(
            BopomofoSoftKeyboards.KB_DEFAULT.layout,
            BopomofoSoftKeyboards.KB_HSU.layout,
            BopomofoSoftKeyboards.KB_ET26.layout,
            BopomofoSoftKeyboards.KB_ET.layout,
            BopomofoSoftKeyboards.KB_DACHEN_CP26.layout,
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentGeneralSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().appSharedPreferences

        binding.sectionGeneral.apply {
            val startImeSystemSettingActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()
                }
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)

            if (!isGuilelessBopomofoEnabled()) {
                Toast.makeText(
                    requireContext(), R.string.please_enable_guileless_bopomofo_first, Toast.LENGTH_LONG
                ).show()
                startImeSystemSettingActivity.launch(intent)
            }

            buttonLaunchImeSystemSettings.setOnClickListener {
                startImeSystemSettingActivity.launch(intent)
            }

            textViewServiceStatus.text = currentGuilelessBopomofoServiceStatus()

            onScreenBopomofoKeyboardLayoutDropdownMenu.bindToPreference(
                sharedPreferences,
                USER_SOFT_KEYBOARD_LAYOUT,
                BopomofoSoftKeyboards.KB_DEFAULT.layout,
                entries = resources.getStringArray(R.array.on_screen_bopomofo_keyboard_layouts).toList(),
                values = SOFT_KEYBOARD_LAYOUTS,
            ) { selectedLayout ->
                // The QWERTY variants only mean something for the layouts that have one.
                switchDisplayHsuQwertyLayout.isGone = selectedLayout != BopomofoSoftKeyboards.KB_HSU.layout
                switchDisplayEten26QwertyLayout.isGone = selectedLayout != BopomofoSoftKeyboards.KB_ET26.layout
            }

            switchDisplayHsuQwertyLayout.bindToPreference(
                sharedPreferences, USER_DISPLAY_HSU_QWERTY_LAYOUT, false
            )
            switchDisplayEten26QwertyLayout.bindToPreference(
                sharedPreferences, USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false
            )
            switchSettingSpaceAsSelection.bindToPreference(
                sharedPreferences, USER_ENABLE_SPACE_AS_SELECTION, true
            )
            switchRearwardPhraseChoice.bindToPreference(
                sharedPreferences, USER_PHRASE_CHOICE_REARWARD, false
            )
            switchEnglishPrediction.bindToPreference(
                sharedPreferences, USER_ENABLE_ENGLISH_PREDICTION, false
            )

            bindRadioGroupToPreference(
                sharedPreferences,
                USER_CONVERSION_ENGINE,
                ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
                mapOf(
                    radioButtonSimpleConversionEngine to ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode,
                    radioButtonChewingConversionEngine to ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
                    radioButtonFuzzyChewingConversionEngine to ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode,
                )
            )
        }
    }

    private fun isGuilelessBopomofoEnabled(): Boolean {
        val inputMethodManager =
            requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return false
        return inputMethodManager.enabledInputMethodList.any {
            it.serviceName == GuilelessBopomofoService::class.java.name
        }
    }

    private fun currentGuilelessBopomofoServiceStatus(): String {
        return if (isGuilelessBopomofoEnabled()) {
            getString(R.string.service_is_enabled)
        } else {
            getString(R.string.service_is_disabled)
        }
    }
}
