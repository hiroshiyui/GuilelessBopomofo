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
package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import android.text.InputType
import android.view.inputmethod.InputConnection
import java.lang.reflect.Proxy
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnglishSuggestionsTest {

    /** A text field that only knows the text before its cursor. */
    private class FakeField(var text: String) {
        val edits = mutableListOf<String>()
        val connection: InputConnection = Proxy.newProxyInstance(
            InputConnection::class.java.classLoader, arrayOf(InputConnection::class.java)
        ) { _, method, args ->
            when (method.name) {
                // plain text only: the flags that would ask for styled text are ignored
                "getTextBeforeCursor" -> text.takeLast(args[0] as Int)
                "deleteSurroundingText" -> {
                    text = text.dropLast(args[0] as Int); edits += "delete ${args[0]}"; true
                }
                "commitText" -> {
                    text += args[0].toString(); edits += "commit ${args[0]}"; true
                }
                "beginBatchEdit", "endBatchEdit" -> true
                else -> error("unexpected ${method.name}")
            }
        } as InputConnection
    }

    private class FakePredictor : EnglishPredictor {
        override var isEnabled = false
        override var isNextWordEnabled = false
        var answer = emptyList<String>()
        var asked: Pair<String, String>? = null
        override fun suggest(previous: String, prefix: String): List<String> {
            asked = previous to prefix
            return answer
        }
    }

    private class FakeHost(val textField: FakeField) : EnglishSuggestions.Host {
        override var currentLayout = Layout.QWERTY
        override val inputConnection: InputConnection get() = textField.connection
        var english = true
        var buffersEmpty = true
        override fun typingHalfWidthEnglish() = english
        override fun buffersAreEmpty() = buffersEmpty
    }

    private val field = FakeField("")
    private val host = FakeHost(field)
    private val predictor = FakePredictor()
    private val uiState = ImeUiState()
    private val suggestions = EnglishSuggestions(predictor, uiState, host)

    private fun startPlainTextField() = suggestions.onStartInput(InputType.TYPE_CLASS_TEXT)

    private fun enabledInPlainTextField() {
        startPlainTextField()
        suggestions.isEnabled = true
    }

    // --- when the strip shows ---

    @Test
    fun offByDefault_theBuffersShow() {
        startPlainTextField()
        suggestions.refresh()
        assertTrue(uiState.showBuffers)
        assertEquals(emptyList<String>(), uiState.suggestions)
    }

    @Test
    fun enabledInAPlainTextField_theStripShowsWhatIsPredicted() {
        predictor.answer = listOf("hello", "help")
        field.text = "say hel"
        enabledInPlainTextField()
        assertFalse(uiState.showBuffers)
        assertEquals(listOf("hello", "help"), uiState.suggestions)
        assertEquals("say" to "hel", predictor.asked)
    }

    @Test
    fun notInAPasswordField() {
        suggestions.onStartInput(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        suggestions.isEnabled = true
        assertTrue(uiState.showBuffers)
    }

    @Test
    fun notWhileTypingBopomofo() {
        host.english = false
        enabledInPlainTextField()
        assertTrue(uiState.showBuffers)
    }

    @Test
    fun notOnTheBopomofoKeyboard() {
        host.currentLayout = Layout.MAIN
        enabledInPlainTextField()
        assertTrue(uiState.showBuffers)
    }

    @Test
    fun theBuffersComeBackWhileSomethingWaitsInThem() {
        enabledInPlainTextField()
        assertFalse(uiState.showBuffers)
        host.buffersEmpty = false
        suggestions.refresh()
        assertTrue(uiState.showBuffers)
    }

    // --- choosing a word ---

    @Test
    fun choosing_replacesTheTypedWordAndAddsASpace() {
        field.text = "say hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        assertEquals("say hello ", field.text)
        assertEquals(listOf("delete 3", "commit hello "), field.edits)
    }

    @Test
    fun punctuationRightAfter_takesTheSpaceBack() {
        field.text = "hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        suggestions.commit(",")
        assertEquals("hello,", field.text)
    }

    @Test
    fun aLetterRightAfter_keepsTheSpace() {
        field.text = "hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        suggestions.commit("w")
        assertEquals("hello w", field.text)
        suggestions.commit(",")
        assertEquals("hello w,", field.text)
    }

    @Test
    fun aSpaceTheUserTyped_isNeverTakenBack() {
        field.text = "hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        // the cursor is moved elsewhere, after a space of the user's own
        field.text = "the "
        suggestions.commit(",")
        assertEquals("the ,", field.text)
    }

    @Test
    fun selectingText_forgetsTheSpace() {
        field.text = "hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        suggestions.onSelectionUpdated(0, 3)
        suggestions.commit(",")
        assertEquals("hello ,", field.text)
    }

    @Test
    fun aNewTextField_forgetsTheSpace() {
        field.text = "hel"
        enabledInPlainTextField()
        suggestions.choose("hello")
        startPlainTextField()
        suggestions.commit(",")
        assertEquals("hello ,", field.text)
    }

    @Test
    fun shutDown_clearsTheStripWithoutLookingAtAnything() {
        enabledInPlainTextField()
        assertFalse(uiState.showBuffers)
        suggestions.shutDown()
        assertTrue(uiState.showBuffers)
        assertEquals(emptyList<String>(), uiState.suggestions)
        assertFalse(predictor.isEnabled)
    }

    // --- preferences ---

    private fun preferencesSaying(vararg values: Pair<String, Boolean>): (String) -> Boolean {
        val map = values.toMap()
        return { key -> map[key] ?: false }
    }

    @Test
    fun onPreferenceChanged_followsItsOwnKeys() {
        startPlainTextField()
        assertTrue(suggestions.onPreferenceChanged(USER_ENABLE_ENGLISH_PREDICTION, preferencesSaying(USER_ENABLE_ENGLISH_PREDICTION to true)))
        assertTrue(predictor.isEnabled)
        assertFalse(uiState.showBuffers)
        assertTrue(suggestions.onPreferenceChanged(USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION, preferencesSaying(USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION to true)))
        assertTrue(predictor.isNextWordEnabled)
    }

    @Test
    fun onPreferenceChanged_declinesOtherKeys() {
        assertFalse(suggestions.onPreferenceChanged("something_else", preferencesSaying()))
        assertFalse(suggestions.onPreferenceChanged(null, preferencesSaying()))
    }
}
