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

import android.view.inputmethod.InputConnection
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState

/**
 * The English suggestion strip: decides when it shows in place of the buffers, what it
 * offers, and what happens when a word on it is chosen. It reads the text field through
 * [host] and writes only [uiState]; the input method service owns both.
 */
class EnglishSuggestions(
    private val prediction: EnglishPredictor,
    private val uiState: ImeUiState,
    private val host: Host,
) {
    /** What the strip needs to know about the input method it sits in. */
    interface Host {
        val currentLayout: Layout
        val inputConnection: InputConnection?

        /** Whether libchewing is up and set to half-width English, the only time to predict. */
        fun typingHalfWidthEnglish(): Boolean

        /** Whether libchewing holds nothing yet to be committed. */
        fun buffersAreEmpty(): Boolean
    }

    // whether the text field being edited wants word suggestions at all, see onStartInput()
    private var fieldAllowsSuggestions: Boolean = false

    // the space the last chosen suggestion was committed with
    private val autoSpace = AutoSpace()

    /** Turning prediction on or off shows or hides the strip right away. */
    var isEnabled: Boolean
        get() = prediction.isEnabled
        set(value) {
            prediction.isEnabled = value
            refresh()
        }

    /** Turning next-word prediction on or off refreshes the strip right away. */
    var isNextWordEnabled: Boolean
        get() = prediction.isNextWordEnabled
        set(value) {
            prediction.isNextWordEnabled = value
            refresh()
        }

    /**
     * Lets go of the word lists for good. Unlike turning [isEnabled] off, nothing is looked
     * at again: the input method is on its way out, and libchewing may be gone already.
     */
    fun shutDown() {
        prediction.isEnabled = false
        uiState.suggestions = emptyList()
        uiState.showBuffers = true
    }

    /**
     * Follows the preference under [key] if it is one of English prediction's, reading its
     * value through [isOn].
     *
     * @return whether it was.
     */
    fun onPreferenceChanged(key: String?, isOn: (String) -> Boolean): Boolean {
        when (key) {
            USER_ENABLE_ENGLISH_PREDICTION -> isEnabled = isOn(key)
            USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION -> isNextWordEnabled = isOn(key)
            else -> return false
        }
        return true
    }

    /** A text field was just started: its [inputType] says whether it wants suggestions. */
    fun onStartInput(inputType: Int?) {
        fieldAllowsSuggestions = inputType?.let(EnglishWords::fieldAllowsSuggestions) ?: false
        autoSpace.forget()
    }

    /** The text field's selection moved, by our own commit or by the user. */
    fun onSelectionUpdated(selectionStart: Int, selectionEnd: Int) {
        // text selected by hand: whatever comes next replaces it rather than follows the space
        if (selectionStart != selectionEnd) autoSpace.forget()
        refresh()
    }

    /** The cursor is no longer right after a chosen word: Backspace, arrows, Enter and the like. */
    fun forgetAutoSpace() {
        autoSpace.forget()
    }

    /**
     * English word prediction goes with typing half-width English on the alphanumerical
     * keyboard, in a text field that welcomes suggestions. Anywhere else the keyboard stays
     * exactly as it has always been.
     */
    private fun applies(): Boolean =
        prediction.isEnabled &&
                fieldAllowsSuggestions &&
                host.typingHalfWidthEnglish() &&
                host.currentLayout in PREDICTION_LAYOUTS

    /**
     * The suggestion strip and the buffers take turns above the keyboard: the strip while
     * predicting, and only when it has something to offer, the buffers whenever there is
     * something in them. English typed after Han characters still waiting in the pre-edit
     * buffer goes into that buffer rather than the text field, so there is nothing to predict
     * from then anyway.
     */
    fun refresh() {
        val predicting = applies() && host.buffersAreEmpty()
        uiState.showBuffers = !predicting
        uiState.suggestions = if (predicting) suggestions() else emptyList()
    }

    private fun suggestions(): List<String> {
        val textBeforeCursor = host.inputConnection?.getTextBeforeCursor(PREDICTION_LOOKBEHIND, 0) ?: ""
        return prediction.suggest(
            previous = EnglishWords.previousWord(textBeforeCursor),
            prefix = EnglishWords.trailingWord(textBeforeCursor),
        )
    }

    private fun wordBeforeCursor(inputConnection: InputConnection): String =
        inputConnection.getTextBeforeCursor(PREDICTION_LOOKBEHIND, 0)
            ?.let(EnglishWords::trailingWord)
            .orEmpty()

    /** Replaces the word being typed with [word] and a space, ready for the next one. */
    fun choose(word: String) {
        val inputConnection = host.inputConnection ?: return
        // Look the word up again rather than trust what the strip was built from, the text
        // field may have changed under us in between.
        val typedWord = wordBeforeCursor(inputConnection)
        inputConnection.beginBatchEdit()
        inputConnection.deleteSurroundingText(typedWord.length, 0)
        inputConnection.commitText("$word ", 1)
        inputConnection.endBatchEdit()
        autoSpace.committed(word)
    }

    /**
     * Commits [text] from libchewing, first taking back the space a chosen suggestion came
     * with when the text is punctuation that belongs right after the word, so that `word ,`
     * becomes `word,`.
     */
    fun commit(text: String) {
        val inputConnection = host.inputConnection ?: return
        inputConnection.beginBatchEdit()
        val lookbehind = autoSpace.lookbehind
        if (lookbehind > 0) {
            val before = inputConnection.getTextBeforeCursor(lookbehind, 0)
            if (autoSpace.takesBack(before, text)) inputConnection.deleteSurroundingText(1, 0)
        }
        inputConnection.commitText(text, 1)
        inputConnection.endBatchEdit()
    }

    companion object {
        // The layouts English words are typed on, and so the ones the suggestion strip goes with.
        private val PREDICTION_LAYOUTS = setOf(Layout.QWERTY, Layout.COMPACT)

        // Far enough back to find the start of any word worth predicting.
        private const val PREDICTION_LOOKBEHIND = 48
    }
}
