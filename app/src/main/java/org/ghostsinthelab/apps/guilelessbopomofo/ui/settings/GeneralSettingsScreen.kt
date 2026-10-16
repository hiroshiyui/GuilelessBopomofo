/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoSoftKeyboards
import org.ghostsinthelab.apps.guilelessbopomofo.ConversionEngines
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberBooleanPreference
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberIntPreference
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberStringPreference

// In the order R.array.on_screen_bopomofo_keyboard_layouts names them.
private val SOFT_KEYBOARD_LAYOUTS: List<String> = listOf(
    BopomofoSoftKeyboards.KB_DEFAULT.layout,
    BopomofoSoftKeyboards.KB_HSU.layout,
    BopomofoSoftKeyboards.KB_ET26.layout,
    BopomofoSoftKeyboards.KB_ET.layout,
    BopomofoSoftKeyboards.KB_DACHEN_CP26.layout,
    BopomofoSoftKeyboards.KB_DACHEN_COMPACT.layout,
)

@Composable
fun GeneralSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(isGuilelessBopomofoEnabled(context)) }
    val imeSystemSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        serviceEnabled = isGuilelessBopomofoEnabled(context)
    }
    val imeSystemSettingsIntent = remember { Intent(Settings.ACTION_INPUT_METHOD_SETTINGS) }

    LaunchedEffect(Unit) {
        if (!serviceEnabled) {
            Toast.makeText(context, R.string.please_enable_guileless_bopomofo_first, Toast.LENGTH_LONG).show()
            imeSystemSettingsLauncher.launch(imeSystemSettingsIntent)
        }
    }

    SettingsScreen(modifier) {
        SettingsPrimaryHeader(stringResource(R.string.general_settings_label))

        SettingsLabel(stringResource(R.string.guileless_bopomofo_inputservice_status), Modifier.padding(top = 24.dp))
        SettingsDescription(stringResource(R.string.guileless_bopomofo_service_status_description))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.guileless_bopomofo_service_status), Modifier.weight(1f))
            SettingsCurrentValue(
                stringResource(if (serviceEnabled) R.string.service_is_enabled else R.string.service_is_disabled)
            )
        }
        SettingsButton(
            text = stringResource(R.string.launch_input_method_system_settings),
            onClick = { imeSystemSettingsLauncher.launch(imeSystemSettingsIntent) },
            modifier = Modifier.padding(top = 8.dp),
        )

        SettingsLabel(stringResource(R.string.bopomofo_keyboard_layout))
        SettingsDescription(stringResource(R.string.bopomofo_keyboard_layout_description))
        var softKeyboardLayout by rememberStringPreference(
            USER_SOFT_KEYBOARD_LAYOUT, BopomofoSoftKeyboards.KB_DEFAULT.layout
        )
        DropdownPreference(
            entries = stringArrayResource(R.array.on_screen_bopomofo_keyboard_layouts).toList(),
            values = SOFT_KEYBOARD_LAYOUTS,
            selected = softKeyboardLayout,
            onSelected = { softKeyboardLayout = it },
        )
        // The QWERTY variants only mean something for the layouts that have one.
        if (softKeyboardLayout == BopomofoSoftKeyboards.KB_HSU.layout) {
            BooleanPreferenceSwitch(R.string.switch_display_hsu_qwerty_layout, USER_DISPLAY_HSU_QWERTY_LAYOUT, false)
        }
        if (softKeyboardLayout == BopomofoSoftKeyboards.KB_ET26.layout) {
            BooleanPreferenceSwitch(R.string.switch_display_eten26_qwerty_layout, USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)
        }

        SettingsLabel(stringResource(R.string.textViewSettingsConversionEngines))
        SettingsDescription(stringResource(R.string.textViewSettingsDescriptionConversionEngines))
        ConversionEnginePreference(USER_CONVERSION_ENGINE)

        SettingsLabel(stringResource(R.string.space_as_selection_key))
        SettingsDescription(stringResource(R.string.space_as_selection_key_description))
        BooleanPreferenceSwitch(R.string.space_as_selection_key_switch_label, USER_ENABLE_SPACE_AS_SELECTION, true)

        SettingsLabel(stringResource(R.string.rearward_phrase_choice))
        SettingsDescription(stringResource(R.string.rearward_phrase_choice_description))
        BooleanPreferenceSwitch(R.string.rearward_phrase_choice_switch, USER_PHRASE_CHOICE_REARWARD, false)

        SettingsLabel(stringResource(R.string.english_prediction))
        SettingsDescription(stringResource(R.string.english_prediction_description))
        BooleanPreferenceSwitch(R.string.english_prediction_switch, USER_ENABLE_ENGLISH_PREDICTION, false)

    }
}

/** A [SwitchPreferenceRow] bound straight to a boolean preference. */
@Composable
fun BooleanPreferenceSwitch(@StringRes labelRes: Int, key: String, defaultValue: Boolean) {
    var checked by rememberBooleanPreference(key, defaultValue)
    SwitchPreferenceRow(text = stringResource(labelRes), checked = checked, onCheckedChange = { checked = it })
}

/** The three conversion engines as radio buttons, stored under [key]. */
@Composable
fun ConversionEnginePreference(key: String) {
    var engine by rememberIntPreference(key, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode)
    RadioGroupPreference(
        options = listOf(
            stringResource(R.string.radioButtonSimpleConversionEngine) to ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode,
            stringResource(R.string.radioButtonChewingConversionEngine) to ConversionEngines.CHEWING_CONVERSION_ENGINE.mode,
            stringResource(R.string.radioButtonFuzzyChewingConversionEngine) to ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode,
        ),
        selected = engine,
        onSelected = { engine = it },
    )
}

private fun isGuilelessBopomofoEnabled(context: Context): Boolean {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        ?: return false
    return inputMethodManager.enabledInputMethodList.any {
        it.serviceName == GuilelessBopomofoService::class.java.name
    }
}
