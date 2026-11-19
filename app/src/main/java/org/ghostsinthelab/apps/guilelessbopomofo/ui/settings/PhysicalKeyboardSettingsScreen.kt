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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoPhysicalKeyboards
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessFonts
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberStringPreference

private val DropdownSpacing = 8.dp
private val SelectionKeysSpacing = 16.dp
private val TipsSpacing = 24.dp
private val TipsCardPadding = 16.dp
private val TipsIconSpacing = 8.dp
private val TipItemSpacing = 8.dp
private val TipBulletSpacing = 8.dp
private const val TIP_BULLET = "•"
private val PunctuationTableSpacing = 16.dp
private val PunctuationRowSpacing = 4.dp
// The three columns of the punctuation table share the width equally.
private const val TABLE_COLUMN_WEIGHT = 1f
private const val SHIFT_KEY_NAME = "Shift"

// Key names in the tips are set in Libertinus Keyboard, whose ligatures draw a whole name
// ("Shift", "Space", ...) as one key cap.
private val PhysicalKeyStyle = SpanStyle(
    fontFamily = GuilelessFonts.PhysicalKey,
    fontFeatureSettings = GuilelessFonts.PhysicalKeyFeatures,
)

// In the order R.array.physical_bopomofo_keyboard_layouts names them.
private val PHYSICAL_KEYBOARD_LAYOUTS: List<String> = listOf(
    BopomofoPhysicalKeyboards.KB_DEFAULT.layout,
    BopomofoPhysicalKeyboards.KB_HSU.layout,
    BopomofoPhysicalKeyboards.KB_IBM.layout,
    BopomofoPhysicalKeyboards.KB_GIN_YIEH.layout,
    BopomofoPhysicalKeyboards.KB_ET.layout,
    BopomofoPhysicalKeyboards.KB_ET26.layout,
    BopomofoPhysicalKeyboards.KB_DVORAK.layout,
    BopomofoPhysicalKeyboards.KB_DVORAK_HSU.layout,
    BopomofoPhysicalKeyboards.KB_DACHEN_CP26.layout,
    BopomofoPhysicalKeyboards.KB_HANYU_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_THL_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_MPS2_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_CARPALX.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ANSI.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ORTH.layout,
    BopomofoPhysicalKeyboards.KB_WORKMAN.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK.layout,
)

/** A Shift + [key] chord, the [symbol] it types, and the full-width punctuation libchewing puts out. */
private data class ShiftPunctuation(val key: String, val symbol: String, val output: String)

// libchewing's SPECIAL_SYMBOLS (src/conversion/symbol.rs), reached with Shift on a US layout.
private val SHIFT_PUNCTUATION: List<ShiftPunctuation> = listOf(
    ShiftPunctuation("`", "~", "～"),
    ShiftPunctuation("1", "!", "！"),
    ShiftPunctuation("2", "@", "＠"),
    ShiftPunctuation("3", "#", "＃"),
    ShiftPunctuation("4", "$", "＄"),
    ShiftPunctuation("5", "%", "％"),
    ShiftPunctuation("6", "^", "︿"),
    ShiftPunctuation("7", "&", "＆"),
    ShiftPunctuation("8", "*", "＊"),
    ShiftPunctuation("9", "(", "（"),
    ShiftPunctuation("0", ")", "）"),
    ShiftPunctuation("-", "_", "—"),
    ShiftPunctuation("=", "+", "＋"),
    ShiftPunctuation("[", "{", "『"),
    ShiftPunctuation("]", "}", "』"),
    ShiftPunctuation("\\", "|", "｜"),
    ShiftPunctuation(";", ":", "："),
    ShiftPunctuation("'", "\"", "；"),
    ShiftPunctuation(",", "<", "，"),
    ShiftPunctuation(".", ">", "。"),
    ShiftPunctuation("/", "?", "？"),
)

// The radio buttons are labelled with the keys themselves, in capitals as key caps are printed.
private val SELECTION_KEY_OPTIONS: List<Pair<String, String>> = listOf(
    "1234567890" to SelectionKeys.NUMBER_ROW.set,
    "QWERTYUIOP" to SelectionKeys.TAB_ROW.set,
    "ASDFGHJKL;" to SelectionKeys.HOME_ROW.set,
    "ASDFGQWERT" to SelectionKeys.HOME_TAB_MIXED_MODE1.set,
    "HJKL;YUIOP" to SelectionKeys.HOME_TAB_MIXED_MODE2.set,
    "AOEUIDHTNS" to SelectionKeys.DVORAK_HOME_ROW.set,
)

@Composable
fun PhysicalKeyboardSettingsScreen(modifier: Modifier = Modifier) {
    SettingsScreen(modifier) {
        SettingsPrimaryHeader(stringResource(R.string.physical_keyboard_settings))

        SettingsLabel(stringResource(R.string.physical_bopomofo_keyboard_layout))
        var physicalKeyboardLayout by rememberStringPreference(
            USER_PHYSICAL_KEYBOARD_LAYOUT, BopomofoPhysicalKeyboards.KB_DEFAULT.layout
        )
        DropdownPreference(
            entries = stringArrayResource(R.array.physical_bopomofo_keyboard_layouts).toList(),
            values = PHYSICAL_KEYBOARD_LAYOUTS,
            selected = physicalKeyboardLayout,
            onSelected = { physicalKeyboardLayout = it },
            modifier = Modifier.padding(top = DropdownSpacing),
        )

        SettingsLabel(stringResource(R.string.physical_selection_keys))
        var selectionKeys by rememberStringPreference(
            USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set
        )
        Column(Modifier.padding(top = SelectionKeysSpacing)) {
            // No ligatures here: each selection key is its own key cap.
            RadioGroupPreference(
                SELECTION_KEY_OPTIONS,
                selectionKeys,
                onSelected = { selectionKeys = it },
                labelStyle = LocalTextStyle.current.copy(fontFamily = GuilelessFonts.PhysicalKey),
            )
        }

        SettingsLabel(stringResource(R.string.textViewSettingsConversionEnginesPhysical))
        SettingsDescription(stringResource(R.string.textViewSettingsDescriptionConversionEnginesPhysical))
        ConversionEnginePreference(USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD)

        KeybindingTipsCard(Modifier.padding(top = TipsSpacing))
    }
}

/** The keybinding tips, set apart from the settings above them as an information card. */
@Composable
private fun KeybindingTipsCard(modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(TipsCardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(android.R.drawable.ic_menu_info_details), contentDescription = null)
                Text(
                    stringResource(R.string.tips_using_physical_keyboard_intro),
                    Modifier.padding(start = TipsIconSpacing),
                )
            }
            // The tips run the full width, so the icon does not indent them, one per line
            // with a gap between them.
            val tips = markupStringResource(R.string.tips_using_physical_keyboard, PhysicalKeyStyle)
            Column(
                Modifier.padding(top = TipsIconSpacing),
                verticalArrangement = Arrangement.spacedBy(TipItemSpacing),
            ) {
                tips.lines().forEach { TipBulletItem(it) }
            }
            ShiftPunctuationTable(Modifier.padding(top = PunctuationTableSpacing))
        }
    }
}

/** The Shift + key chords that type full-width punctuation, one per row. */
@Composable
private fun ShiftPunctuationTable(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PunctuationRowSpacing)) {
        Text(stringResource(R.string.punctuation_shortcuts_intro))
        ShiftPunctuationRow(
            AnnotatedString(stringResource(R.string.punctuation_shortcuts_column_keys)),
            stringResource(R.string.punctuation_shortcuts_column_symbol),
            stringResource(R.string.punctuation_shortcuts_column_output),
        )
        HorizontalDivider()
        SHIFT_PUNCTUATION.forEach { (key, symbol, output) ->
            ShiftPunctuationRow(shiftChord(key), symbol, output)
        }
    }
}

/** One row of the table, read out by a screen reader as a whole. */
@Composable
private fun ShiftPunctuationRow(keys: AnnotatedString, symbol: String, output: String) {
    Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}) {
        Text(keys, Modifier.weight(TABLE_COLUMN_WEIGHT))
        Text(symbol, Modifier.weight(TABLE_COLUMN_WEIGHT))
        Text(output, Modifier.weight(TABLE_COLUMN_WEIGHT))
    }
}

/** "Shift + [key]", each key drawn as a key cap like in the tips above. */
private fun shiftChord(key: String): AnnotatedString = buildAnnotatedString {
    withStyle(PhysicalKeyStyle) { append(SHIFT_KEY_NAME) }
    append(" + ")
    withStyle(PhysicalKeyStyle) { append(key) }
}

/** One tip of the list: wrapped lines hang level with the text, clear of the bullet. */
@Composable
private fun TipBulletItem(tip: AnnotatedString) {
    Row {
        // Decoration only: a screen reader should read the tip alone.
        Text(TIP_BULLET, Modifier.clearAndSetSemantics {})
        Text(tip, Modifier.padding(start = TipBulletSpacing).weight(1f))
    }
}

/** The lines of this string, each keeping the spans that fall within it. */
private fun AnnotatedString.lines(): List<AnnotatedString> = buildList {
    var start = 0
    while (start <= text.length) {
        val end = text.indexOf('\n', start).let { if (it < 0) text.length else it }
        add(subSequence(start, end))
        start = end + 1
    }
}
