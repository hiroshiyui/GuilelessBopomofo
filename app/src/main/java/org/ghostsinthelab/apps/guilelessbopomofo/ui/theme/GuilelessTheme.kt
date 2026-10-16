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

/*
 * Every Material 3 role comes from the palette, so that nothing falls back to the baseline
 * purple. The input view draws the keyboard on surfaceContainer and the keys (and
 * candidates, suggestions) on surfaceContainerHighest, their glyphs in onSurface; function
 * keys are tertiary and Enter is secondary.
 */

@Composable
@ReadOnlyComposable
private fun morandiColorScheme(): ColorScheme {
    val surface = colorResource(R.color.colorGuilelessBopomofoMorandiSurface)
    val base4 = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase4)
    val base8 = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase8)
    val base10 = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase10)
    return lightColorScheme(
        primary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase6),
        onPrimary = base8,
        primaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase2),
        onPrimaryContainer = base10,
        inversePrimary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase3),
        secondary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase7),
        onSecondary = base10,
        secondaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase1),
        onSecondaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiOnSecondaryContainer),
        tertiary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase3),
        onTertiary = base10,
        tertiaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase5),
        onTertiaryContainer = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase9),
        background = surface,
        onBackground = base10,
        surface = surface,
        onSurface = base10,
        surfaceVariant = base4,
        onSurfaceVariant = colorResource(R.color.colorGuilelessBopomofoMorandiOnSurfaceVariant),
        inverseSurface = base10,
        inverseOnSurface = base8,
        outline = colorResource(R.color.colorGuilelessBopomofoMorandiOutline),
        outlineVariant = colorResource(R.color.colorGuilelessBopomofoMorandiOutlineVariant),
        surfaceBright = surface,
        surfaceDim = base4,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = colorResource(R.color.colorGuilelessBopomofoMorandiSurfaceContainerLow),
        surfaceContainer = base8,
        surfaceContainerHigh = colorResource(R.color.colorGuilelessBopomofoMorandiSurfaceContainerHigh),
        surfaceContainerHighest = base4,
    )
}

@Composable
@ReadOnlyComposable
private fun wineColorScheme(): ColorScheme {
    // values-night sets android:windowBackground to Wine Base10 as well
    val base4 = colorResource(R.color.colorGuilelessBopomofoWineThemeBase4)
    val base5 = colorResource(R.color.colorGuilelessBopomofoWineThemeBase5)
    val base8 = colorResource(R.color.colorGuilelessBopomofoWineThemeBase8)
    val base10 = colorResource(R.color.colorGuilelessBopomofoWineThemeBase10)
    val surfaceContainerHigh = colorResource(R.color.colorGuilelessBopomofoWineSurfaceContainerHigh)
    return darkColorScheme(
        primary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase3),
        onPrimary = base10,
        primaryContainer = colorResource(R.color.colorGuilelessBopomofoWineThemeBase7),
        onPrimaryContainer = base4,
        inversePrimary = colorResource(R.color.colorGuilelessBopomofoMorandiThemeBase6),
        secondary = colorResource(R.color.colorGuilelessBopomofoWineThemeBase2),
        onSecondary = base4,
        secondaryContainer = base8,
        onSecondaryContainer = base4,
        tertiary = base8,
        onTertiary = base4,
        tertiaryContainer = base8,
        onTertiaryContainer = base4,
        background = base10,
        onBackground = base4,
        surface = base10,
        onSurface = base4,
        surfaceVariant = base5,
        onSurfaceVariant = colorResource(R.color.colorGuilelessBopomofoWineOnSurfaceVariant),
        inverseSurface = base4,
        inverseOnSurface = base10,
        outline = colorResource(R.color.colorGuilelessBopomofoWineOutline),
        outlineVariant = colorResource(R.color.colorGuilelessBopomofoWineOutlineVariant),
        surfaceBright = surfaceContainerHigh,
        surfaceDim = base10,
        surfaceContainerLowest = colorResource(R.color.colorGuilelessBopomofoWineSurfaceContainerLowest),
        surfaceContainerLow = colorResource(R.color.colorGuilelessBopomofoWineSurfaceContainerLow),
        surfaceContainer = base10,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = base5,
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
}
