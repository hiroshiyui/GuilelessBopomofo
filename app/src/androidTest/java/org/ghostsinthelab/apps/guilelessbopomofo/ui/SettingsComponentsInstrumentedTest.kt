package org.ghostsinthelab.apps.guilelessbopomofo.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.IntSliderPreference
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.SettingsLabel
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.SettingsPrimaryHeader
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsComponentsInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aSlider_isOneNodeWithItsNameAndItsRange() {
        composeRule.setContent {
            GuilelessTheme { IntSliderPreference(value = 52, range = 48..80, onValueChange = {}, label = "Key height") }
        }
        composeRule.onAllNodesWithContentDescription("Key height")
            .assertCountEquals(1)
            .onFirst()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun settingsHeadings_areHeadings() {
        composeRule.setContent {
            GuilelessTheme {
                SettingsPrimaryHeader("Screen")
                SettingsLabel("Section")
            }
        }
        val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        composeRule.onNodeWithText("Screen").assert(isHeading)
        composeRule.onNodeWithText("Section").assert(isHeading)
    }
}
