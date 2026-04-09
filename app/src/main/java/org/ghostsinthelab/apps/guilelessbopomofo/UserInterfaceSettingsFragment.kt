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

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.FragmentUserInterfaceSettingsBinding
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class UserInterfaceSettingsFragment : Fragment(), Vibratable {
    private var _binding: FragmentUserInterfaceSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserInterfaceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(APP_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)

        binding.sectionUserInterface.apply {
            var hapticFeedbackPreferenceStrength = sharedPreferences.getInt(
                USER_HAPTIC_FEEDBACK_STRENGTH, GuilelessBopomofoService.defaultHapticFeedbackStrength
            )

            textViewSettingHapticFeedbaclCurrentStrength.text =
                getString(R.string.haptic_feedback_strength_setting, hapticFeedbackPreferenceStrength)

            seekBarHapticFeedbackStrength.value = hapticFeedbackPreferenceStrength.toFloat()
            seekBarHapticFeedbackStrength.addOnChangeListener { _, value, _ ->
                hapticFeedbackPreferenceStrength = value.toInt()
                performVibration(
                    requireContext(), hapticFeedbackPreferenceStrength
                )

                sharedPreferences.edit().putInt(
                    USER_HAPTIC_FEEDBACK_STRENGTH, hapticFeedbackPreferenceStrength
                ).apply()
                textViewSettingHapticFeedbaclCurrentStrength.text =
                    getString(R.string.haptic_feedback_strength_setting, hapticFeedbackPreferenceStrength)
            }

            switchSettingApplySameHapticFeedbackStrengthToFunctionButtons.let {
                if (sharedPreferences.getBoolean(SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS, false)) {
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
                    sharedPreferences.edit().putBoolean(USER_FULLSCREEN_WHEN_IN_LANDSCAPE, it.isChecked).apply()
                }
            }

            switchSettingFullscreenWhenInPortrait.let {
                if (sharedPreferences.getBoolean(USER_FULLSCREEN_WHEN_IN_PORTRAIT, false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, _ ->
                    sharedPreferences.edit().putBoolean(USER_FULLSCREEN_WHEN_IN_PORTRAIT, it.isChecked).apply()
                }
            }

            var keyButtonPreferenceHeight = sharedPreferences.getInt(USER_KEY_BUTTON_HEIGHT, 52)

            seekBarKeyButtonHeight.value = keyButtonPreferenceHeight.toFloat()

            textViewSettingKeyButtonCurrentHeight.text =
                getString(R.string.key_button_height_setting, keyButtonPreferenceHeight)

            seekBarKeyButtonHeight.addOnChangeListener { _, value, _ ->
                keyButtonPreferenceHeight = value.toInt()
                sharedPreferences.edit().putInt(USER_KEY_BUTTON_HEIGHT, keyButtonPreferenceHeight).apply()
                textViewSettingKeyButtonCurrentHeight.text =
                    getString(R.string.key_button_height_setting, keyButtonPreferenceHeight)
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
                if (sharedPreferences.getBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false)) {
                    it.isChecked = true
                }

                it.setOnCheckedChangeListener { _, _ ->
                    sharedPreferences.edit().putBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, it.isChecked).apply()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
