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

import android.content.res.Configuration
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.withTimeoutOrNull
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.SettingsCurrentValue
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme

class EngineeringModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Chewing data files status
        val chewingDataFilesStatusText: String = if (
            ChewingUtil.chewingDataFilesInstalled(applicationInfo.dataDir)
        ) {
            getString(R.string.chewing_data_files_status_ok)
        } else {
            getString(R.string.chewing_data_files_status_error)
        }

        // Hardware keyboard type
        val hardwareKeyboardTypeText: String = when (resources.configuration.keyboard) {
            Configuration.KEYBOARD_QWERTY -> getString(R.string.hardware_keyboard_type_qwerty)
            Configuration.KEYBOARD_12KEY -> getString(R.string.hardware_keyboard_type_12key)
            else -> getString(R.string.hardware_keyboard_type_unknown_or_missing)
        }

        // Hardware keyboard hidden status
        val hardwareKeyboardHiddenStatusText: String = when (resources.configuration.hardKeyboardHidden) {
            Configuration.HARDKEYBOARDHIDDEN_NO -> getString(R.string.hardware_keyboard_hidden_status_no)
            Configuration.HARDKEYBOARDHIDDEN_YES -> getString(R.string.hardware_keyboard_hidden_status_yes)
            Configuration.HARDKEYBOARDHIDDEN_UNDEFINED -> getString(R.string.hardware_keyboard_hidden_status_undefined)
            else -> getString(R.string.hardware_keyboard_hidden_status_unknown)
        }

        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        setContent {
            GuilelessTheme {
                EngineeringModeScreen(
                    statuses = listOf(
                        stringResource(R.string.chewing_data_files_status) to chewingDataFilesStatusText,
                        stringResource(R.string.labelHardwareKeyboardType) to hardwareKeyboardTypeText,
                        stringResource(R.string.labelHardwareKeyboardHiddenStatus) to hardwareKeyboardHiddenStatusText,
                    ),
                    onTestFieldLongPress = { inputMethodManager.showInputMethodPicker() },
                )
            }
        }
    }
}

private val ScreenHorizontalPadding = 16.dp
private val StatusTextSize = 16.sp

@Composable
private fun EngineeringModeScreen(statuses: List<Pair<String, String>>, onTestFieldLongPress: () -> Unit) {
    var testText by rememberSaveable { mutableStateOf("") }
    var testNumber by rememberSaveable { mutableStateOf("") }
    // Long pressing either test field is a shortcut to the IME picker, so that one can
    // switch keyboards without leaving this screen.
    val longPressToPickIme = Modifier.onLongPressObserved(onTestFieldLongPress)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .padding(horizontal = ScreenHorizontalPadding),
        ) {
            TextField(
                value = testText,
                onValueChange = { testText = it },
                placeholder = { Text(stringResource(R.string.hintEditTextTestTextInput)) },
                modifier = longPressToPickIme
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
            TextField(
                value = testNumber,
                onValueChange = { testNumber = it },
                placeholder = { Text(stringResource(R.string.hintEditTextTestNumberInput)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = longPressToPickIme
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            statuses.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        fontSize = StatusTextSize,
                        fontWeight = FontWeight.Bold,
                    )
                    SettingsCurrentValue(value, fontSize = StatusTextSize)
                }
            }
        }
    }
}

/**
 * Calls [onLongPress] when a pointer stays down past the long-press timeout, without
 * consuming anything, so the text field underneath still gets its own gestures.
 */
private fun Modifier.onLongPressObserved(onLongPress: () -> Unit): Modifier = pointerInput(onLongPress) {
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val released = withTimeoutOrNull(longPressTimeout) {
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.changes.any { it.pressed })
        }
        if (released == null) onLongPress()
    }
}
