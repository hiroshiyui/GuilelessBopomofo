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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.FragmentGeneralSettingsBinding

class GeneralSettingsFragment : Fragment() {
    private var _binding: FragmentGeneralSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGeneralSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(APP_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)

        binding.sectionGeneral.apply {
            val startImeSystemSettingActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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

            val keyboardLayouts = resources.getStringArray(R.array.on_screen_bopomofo_keyboard_layouts)
            val layoutMap = mapOf(
                keyboardLayouts[0] to BopomofoSoftKeyboards.KB_DEFAULT.layout,
                keyboardLayouts[1] to BopomofoSoftKeyboards.KB_HSU.layout,
                keyboardLayouts[2] to BopomofoSoftKeyboards.KB_ET26.layout,
                keyboardLayouts[3] to BopomofoSoftKeyboards.KB_ET.layout,
                keyboardLayouts[4] to BopomofoSoftKeyboards.KB_DACHEN_CP26.layout
            )

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                keyboardLayouts
            )
            (onScreenBopomofoKeyboardLayoutDropdownMenu.editText as? AutoCompleteTextView)?.let { autoCompleteTextView ->
                autoCompleteTextView.setAdapter(adapter)
                val currentLayout = sharedPreferences.getString(
                    USER_SOFT_KEYBOARD_LAYOUT, BopomofoSoftKeyboards.KB_DEFAULT.layout
                )
                val currentLayoutName = layoutMap.entries.find { it.value == currentLayout }?.key
                autoCompleteTextView.setText(currentLayoutName, false)

                autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    val selectedLayout = layoutMap[keyboardLayouts[position]]
                    sharedPreferences.edit().putString(USER_SOFT_KEYBOARD_LAYOUT, selectedLayout).apply()

                    switchDisplayHsuQwertyLayout.isGone = selectedLayout != BopomofoSoftKeyboards.KB_HSU.layout
                    switchDisplayEten26QwertyLayout.isGone = selectedLayout != BopomofoSoftKeyboards.KB_ET26.layout
                }

                switchDisplayHsuQwertyLayout.isGone = currentLayout != BopomofoSoftKeyboards.KB_HSU.layout
                switchDisplayEten26QwertyLayout.isGone = currentLayout != BopomofoSoftKeyboards.KB_ET26.layout
            }

            switchDisplayHsuQwertyLayout.let {
                if (sharedPreferences.getBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, _ ->
                    sharedPreferences.edit().putBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, it.isChecked).apply()
                }
            }

            switchDisplayEten26QwertyLayout.let {
                if (sharedPreferences.getBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, _ ->
                    sharedPreferences.edit().putBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, it.isChecked).apply()
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
                    sharedPreferences.edit().putBoolean(USER_ENABLE_SPACE_AS_SELECTION, it.isChecked).apply()
                }
            }

            switchRearwardPhraseChoice.let {
                if (sharedPreferences.getBoolean(USER_PHRASE_CHOICE_REARWARD, false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, _ ->
                    sharedPreferences.edit().putBoolean(USER_PHRASE_CHOICE_REARWARD, it.isChecked).apply()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isGuilelessBopomofoEnabled(): Boolean {
        val inputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
            ?: return false
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList

        enabledInputMethodList.forEach {
            if (it.serviceName == GuilelessBopomofoService::class.java.name) {
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
