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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ghostsinthelab.apps.guilelessbopomofo.R

/**
 * Compose counterpart of `AppBaseTheme` in `values/styles.xml` (Morandi, day) and
 * `values-night/styles.xml` (Wine, night). The palette stays in `colors.xml` so both
 * the window theme and Compose read the same values.
 */
@Composable
fun GuilelessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) wineColorScheme() else morandiColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
@ReadOnlyComposable
private fun morandiColorScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase6),
    onPrimary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase8),
    primaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase2),
    secondary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase7),
    onSecondary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase10),
    secondaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase1),
    onSecondaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase4),
    tertiary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase3),
    onTertiary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase10),
    tertiaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase5),
    onTertiaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase9),
)

@Composable
@ReadOnlyComposable
private fun wineColorScheme(): ColorScheme {
    val windowBackground = colorResource(R.color.colorGuilelessBopomofoWineThemeBase10)
    return darkColorScheme(
        primary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase3),
        onPrimary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase10),
        primaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase7),
        secondary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase2),
        onSecondary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase4),
        secondaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase8),
        onSecondaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase5),
        tertiary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase8),
        onTertiary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase4),
        tertiaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase8),
        onTertiaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase4),
        // values-night sets android:windowBackground to Wine Base10
        background = windowBackground,
        surface = windowBackground,
    )
}

/** Sizes and type that the XML styles used to carry. */
object GuilelessDimens {
    // A key's slot is KeyMinHeightDefault (or the user's key height) tall; the raised face
    // sits inside it, inset as MaterialButton insets its background.
    val KeyShape = RoundedCornerShape(6.dp)
    val KeyElevation = 1.dp
    val KeyMinHeightDefault = 52.dp
    val KeyHorizontalInset = 2.dp
    val KeyVerticalInset = 6.dp
    val TextKeyMinHeight = 48.dp
    val TextKeyHorizontalPadding = 16.dp
    val KeyboardPanelVerticalPadding = 4.dp
    val ButtonCornerRadius = 16.dp

    val CandidateGridRows = 4
    val CandidateGridRowHeight = 55.dp
    val CandidateMinHeight = 48.dp
    val CandidateHorizontalInset = 4.dp
    val CandidateHorizontalPadding = 24.dp
    val CandidateTextSize = 24.sp
    val PagedCandidateTextSize = 20.sp
    val SelectionKeyTextSize = 12.sp

    val SuggestionStripHeight = 52.dp
    val SuggestionStripPadding = 4.dp
    val SuggestionMinWidth = 72.dp
    val SuggestionHorizontalPadding = 16.dp
    val SuggestionMargin = 2.dp
    val SuggestionTextSize = 18.sp

    val BufferElevation = 4.dp
    val BufferTextSize = 26.sp
    val BufferLineHeight = 34.sp
    val BufferMinHeight = 35.dp
    val PreEditBufferPadding = 12.dp
    val BopomofoBufferPadding = 2.dp
}

object GuilelessFonts {
    val KeyLabel = FontFamily.SansSerif
    val KeyLabelWeight = FontWeight.Black
    val Candidate = FontFamily.Serif
    val SelectionKey = FontFamily.Monospace
    val Buffer = FontFamily.Serif
}

/** Fixed (not day/night aware) colours from `colors.xml`. */
object GuilelessColors {
    val PreEditBufferText = Color(0xFFFFFFFF)
    val PreEditBufferBackground = Color(0xFF2196F3)
    val BopomofoBufferText = Color(0xFF333333)
    val BopomofoBufferBackground = Color(0xFF00BCD4)
    val SelectionKeyText = Color.White
}
