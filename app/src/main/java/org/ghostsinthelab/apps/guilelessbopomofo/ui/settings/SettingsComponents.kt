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

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessFonts
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.LocalGuilelessThemeExtras
import kotlin.math.roundToInt

private val ScreenPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp)
private val LabelTopMargin = 32.dp
private val DescriptionTopMargin = 4.dp
private val DescriptionBottomMargin = 16.dp
private val WidgetTopMargin = 8.dp
private val CurrentValueShape = RoundedCornerShape(4.dp)
private val CurrentValueHorizontalPadding = 8.dp
private val RadioLabelSpacing = 8.dp

/** The scrolling column each settings tab sits in (was a ScrollView + padded LinearLayout). */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ScreenPadding),
        content = content,
    )
}

/**
 * A string resource with its bold markup kept, which [stringResource] would drop: the
 * `<b>` spans become bold [SpanStyle]s.
 */
@Composable
@ReadOnlyComposable
fun boldMarkupStringResource(@StringRes id: Int): AnnotatedString {
    val text = LocalResources.current.getText(id)
    if (text !is Spanned) return AnnotatedString(text.toString())
    return buildAnnotatedString {
        append(text.toString())
        text.getSpans(0, text.length, StyleSpan::class.java)
            .filter { it.style and Typeface.BOLD != 0 }
            .forEach { addStyle(SpanStyle(fontWeight = FontWeight.Bold), text.getSpanStart(it), text.getSpanEnd(it)) }
    }
}

/** `Settings.PrimaryHeader` */
@Composable
fun SettingsPrimaryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = LocalGuilelessThemeExtras.current.primaryText,
    )
}

/** `Settings.Label`: the title of a settings section, the same space above every one. */
@Composable
fun SettingsLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = LabelTopMargin),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = LocalGuilelessThemeExtras.current.secondaryText,
    )
}

/** `Settings.Description` */
@Composable
fun SettingsDescription(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = DescriptionTopMargin, bottom = DescriptionBottomMargin),
        style = MaterialTheme.typography.bodyMedium,
    )
}

/** `Settings.CurrentValue`: a small tertiary pill showing a value. */
@Composable
fun SettingsCurrentValue(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = TextUnit.Unspecified) {
    Text(
        text = text,
        modifier = modifier
            .background(MaterialTheme.colorScheme.tertiary, CurrentValueShape)
            .padding(horizontal = CurrentValueHorizontalPadding),
        color = MaterialTheme.colorScheme.onTertiary,
        fontSize = fontSize,
        fontFamily = GuilelessFonts.KeyLabel,
        fontWeight = GuilelessFonts.KeyLabelWeight,
    )
}

/** `AppBaseTheme.MaterialButton`: a filled button with 16dp corners. */
@Composable
fun SettingsButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(GuilelessDimens.ButtonCornerRadius),
    ) {
        Text(text)
    }
}

/** A label with a switch at its end; the whole row toggles. */
@Composable
fun SwitchPreferenceRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = WidgetTopMargin)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** A vertical radio group; [options] pairs each label with the value it stands for. */
@Composable
fun <T> RadioGroupPreference(
    options: List<Pair<String, T>>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        options.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = value == selected, role = Role.RadioButton) { onSelected(value) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = value == selected, onClick = null)
                Text(text = label, modifier = Modifier.padding(start = RadioLabelSpacing))
            }
        }
    }
}

/**
 * An exposed dropdown menu. Whatever [entries] and [values] disagree on has no name to
 * show or no value to store, so it is left out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPreference(
    entries: List<String>,
    values: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val choices = remember(entries, values) { entries.zip(values) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.padding(top = WidgetTopMargin),
    ) {
        OutlinedTextField(
            value = choices.find { (_, value) -> value == selected }?.first.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = label?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { (entry, value) ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/** A whole-number slider over [range]. */
@Composable
fun IntSliderPreference(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.roundToInt()) },
        // Continuous and rounded, rather than discrete steps, so no tick marks are drawn.
        valueRange = range.first.toFloat()..range.last.toFloat(),
        modifier = modifier.fillMaxWidth(),
    )
}
