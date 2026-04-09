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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.FragmentPhysicalKeyboardSettingsBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys

class PhysicalKeyboardSettingsFragment : Fragment() {
    private var _binding: FragmentPhysicalKeyboardSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPhysicalKeyboardSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(APP_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)

        binding.sectionPhysicalKeyboard.apply {
            val physicalKeyboardLayouts = resources.getStringArray(R.array.physical_bopomofo_keyboard_layouts)
            val layoutMap = mapOf(
                physicalKeyboardLayouts[0] to BopomofoPhysicalKeyboards.KB_DEFAULT.layout,
                physicalKeyboardLayouts[1] to BopomofoPhysicalKeyboards.KB_HSU.layout,
                physicalKeyboardLayouts[2] to BopomofoPhysicalKeyboards.KB_IBM.layout,
                physicalKeyboardLayouts[3] to BopomofoPhysicalKeyboards.KB_GIN_YIEH.layout,
                physicalKeyboardLayouts[4] to BopomofoPhysicalKeyboards.KB_ET.layout,
                physicalKeyboardLayouts[5] to BopomofoPhysicalKeyboards.KB_ET26.layout,
                physicalKeyboardLayouts[6] to BopomofoPhysicalKeyboards.KB_DVORAK.layout,
                physicalKeyboardLayouts[7] to BopomofoPhysicalKeyboards.KB_DVORAK_HSU.layout,
                physicalKeyboardLayouts[8] to BopomofoPhysicalKeyboards.KB_DACHEN_CP26.layout,
                physicalKeyboardLayouts[9] to BopomofoPhysicalKeyboards.KB_HANYU_PINYIN.layout,
                physicalKeyboardLayouts[10] to BopomofoPhysicalKeyboards.KB_THL_PINYIN.layout,
                physicalKeyboardLayouts[11] to BopomofoPhysicalKeyboards.KB_MPS2_PINYIN.layout,
                physicalKeyboardLayouts[12] to BopomofoPhysicalKeyboards.KB_CARPALX.layout,
                physicalKeyboardLayouts[13] to BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ANSI.layout,
                physicalKeyboardLayouts[14] to BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ORTH.layout,
                physicalKeyboardLayouts[15] to BopomofoPhysicalKeyboards.KB_WORKMAN.layout,
                physicalKeyboardLayouts[16] to BopomofoPhysicalKeyboards.KB_COLEMAK.layout
            )

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                physicalKeyboardLayouts
            )

            (physicalBopomofoKeyboardLayoutDropdownMenu.editText as? AutoCompleteTextView)?.let { autoCompleteTextView ->
                autoCompleteTextView.setAdapter(adapter)
                val currentLayout = sharedPreferences.getString(
                    USER_PHYSICAL_KEYBOARD_LAYOUT, BopomofoPhysicalKeyboards.KB_DEFAULT.layout
                )
                val currentLayoutName = layoutMap.entries.find { it.value == currentLayout }?.key
                autoCompleteTextView.setText(currentLayoutName, false)

                autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    sharedPreferences.edit()
                        .putString(USER_PHYSICAL_KEYBOARD_LAYOUT, layoutMap[physicalKeyboardLayouts[position]])
                        .apply()
                }
            }

            for ((button, keys) in mapOf(
                radioButtonNumberRow to SelectionKeys.NUMBER_ROW.set,
                radioButtonTabRow to SelectionKeys.TAB_ROW.set,
                radioButtonHomeRow to SelectionKeys.HOME_ROW.set,
                radioButtonHomeTabMixedMode1 to SelectionKeys.HOME_TAB_MIXED_MODE1.set,
                radioButtonHomeTabMixedMode2 to SelectionKeys.HOME_TAB_MIXED_MODE2.set,
                radioButtonDvorakHomeRow to SelectionKeys.DVORAK_HOME_ROW.set,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
