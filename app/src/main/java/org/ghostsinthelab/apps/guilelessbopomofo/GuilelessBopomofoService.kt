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
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_C
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_GRAVE
import android.view.KeyEvent.KEYCODE_I
import android.view.KeyEvent.KEYCODE_R
import android.view.KeyEvent.KEYCODE_SHIFT_LEFT
import android.view.KeyEvent.KEYCODE_V
import android.view.KeyEvent.KEYCODE_X
import android.view.KeyEvent.KEYCODE_Z
import android.view.KeyEvent.META_SHIFT_ON
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.deviceIsEmulator
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.buffers.PreEditBufferTextView
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ImeLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.CapsLock
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Del
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Down
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.End
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Enter
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Escape
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Home
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Left
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.LeftAlt
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyHandler
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Right
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.RightShift
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Space
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Up
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.VolumeDown
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.VolumeUp
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.EnglishPrediction
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.EnglishWords
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.ScowlWordPredictor
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EdgeToEdge
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehavior
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehaviorResolver
import org.ghostsinthelab.apps.guilelessbopomofo.utils.KeyEventExtension
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class GuilelessBopomofoService : InputMethodService(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener,
    KeyEventExtension, EdgeToEdge {
    private val logTag = "GuilelessBopomofoSvc"
    @Volatile
    private var shiftKeyIsLocked: Boolean = false

    @Volatile
    private var shiftKeyIsActive: Boolean = false

    private lateinit var viewBinding: ImeLayoutBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val englishPrediction = EnglishPrediction(this) { assets.open(ScowlWordPredictor.ASSET_PATH) }

    // whether the text field being edited wants word suggestions, see onStartInputView()
    private var fieldAllowsSuggestions: Boolean = false

    // The physical keys we answer to ourselves. Every handler is stateless, so one instance
    // of each lasts for the lifetime of the service.
    private val physicalKeyDispatcher: Map<Int, PhysicalKeyHandler> = mapOf(
        KeyEvent.KEYCODE_DPAD_DOWN to Down(),
        KeyEvent.KEYCODE_DPAD_UP to Up(),
        KeyEvent.KEYCODE_DPAD_LEFT to Left(),
        KeyEvent.KEYCODE_DPAD_RIGHT to Right(),
        KeyEvent.KEYCODE_ALT_LEFT to LeftAlt(),
        KeyEvent.KEYCODE_SHIFT_RIGHT to RightShift(),
        KeyEvent.KEYCODE_ENTER to Enter(),
        KeyEvent.KEYCODE_SPACE to Space(),
        KeyEvent.KEYCODE_ESCAPE to Escape(),
        KeyEvent.KEYCODE_DEL to Del(),
        KeyEvent.KEYCODE_CAPS_LOCK to CapsLock(),
        KeyEvent.KEYCODE_MOVE_END to End(),
        KeyEvent.KEYCODE_MOVE_HOME to Home(),
        KeyEvent.KEYCODE_VOLUME_UP to VolumeUp(),
        KeyEvent.KEYCODE_VOLUME_DOWN to VolumeDown(),
        // Add more mappings here for each physical key you want to handle separately
    )

    companion object {
        private const val CANDIDATES_PER_PAGE = 10

        // The layouts one can leave the keyboard from, rather than merely stepping back
        // into the main one.
        private val DISMISSIBLE_LAYOUTS = setOf(Layout.MAIN, Layout.COMPACT, Layout.QWERTY)

        // The layouts that are a detour from the main one, and can be stepped out of.
        private val SUB_LAYOUTS = setOf(Layout.SYMBOLS, Layout.CANDIDATES)

        // The layouts English words are typed on, and so the ones the suggestion strip goes with.
        private val PREDICTION_LAYOUTS = setOf(Layout.QWERTY, Layout.COMPACT)

        // Far enough back to find the start of any word worth predicting.
        private const val PREDICTION_LOOKBEHIND = 48

        val defaultHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength

        @Volatile
        var userHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength
    }

    override fun onCreate() {
        Log.d(logTag, "onCreate()")
        super.onCreate()

        if (Build.MANUFACTURER == "Google" && Build.BOARD.startsWith("goldfish_")) {
            deviceIsEmulator = true
        }

        // set Back key disposition
        backDisposition = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BACK_DISPOSITION_ADJUST_NOTHING
        } else {
            BACK_DISPOSITION_DEFAULT
        }

        sharedPreferences = appSharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        try {
            initializeChewing()
        } catch (exception: UnsatisfiedLinkError) {
            reportChewingInitFailure("Failed to load native library", exception)
        } catch (exception: Chewing.ChewingInitException) {
            reportChewingInitFailure("Failed to initialize Chewing", exception)
        } catch (exception: IOException) {
            reportChewingInitFailure("Failed to setup Chewing data", exception)
        }

        // Register EventBus after initialization so handlers won't fire on uninitialized state
        EventBus.getDefault().register(this)

        userHapticFeedbackStrength =
            sharedPreferences.getInt(USER_HAPTIC_FEEDBACK_STRENGTH, defaultHapticFeedbackStrength)

        englishPrediction.isEnabled = sharedPreferences.getBoolean(USER_ENABLE_ENGLISH_PREDICTION, false)
    }


    override fun onCreateCandidatesView(): View? {
        // I want to implement my own candidate selection UI
        Log.d(logTag, "onCreateCandidatesView()")
        return null
    }

    // Disable fullscreen mode when device's orientation is landscape
    override fun onEvaluateFullscreenMode(): Boolean {
        Log.d(logTag, "onEvaluateFullscreenMode()")

        return when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE ->
                sharedPreferences.getBoolean(USER_FULLSCREEN_WHEN_IN_LANDSCAPE, true)

            Configuration.ORIENTATION_PORTRAIT ->
                sharedPreferences.getBoolean(USER_FULLSCREEN_WHEN_IN_PORTRAIT, false)

            else -> false
        }
    }

    override fun onCreateInputView(): View {
        Log.d(logTag, "onCreateInputView()")
        viewBinding = ImeLayoutBinding.inflate(this.layoutInflater)

        applyInputViewBottomEdgeWithGradient(viewBinding.root, viewBinding.imeBottomGradientSpacer)

        return viewBinding.root
    }

    override fun onEvaluateInputViewShown(): Boolean {
        Log.d(logTag, "onEvaluateInputViewShown()")
        super.onEvaluateInputViewShown()
        // always show the input view whether physical keyboard is connected or not
        return true
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(logTag, "onStartInputView()")

        // detect if physical keyboard is presented
        physicalKeyboardPresented =
            (resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY) && (resources.configuration.hardKeyboardHidden ==
                    Configuration.HARDKEYBOARDHIDDEN_NO) && (!deviceIsEmulator)

        // re-apply the conversion engine to match the just-detected keyboard state
        applyConversionEngine()

        // if the input type is phone or number, switch to symbol (alphanumeric) mode
        val inputType = info?.inputType?.and(InputType.TYPE_MASK_CLASS)
        if (inputType == InputType.TYPE_CLASS_PHONE || inputType == InputType.TYPE_CLASS_NUMBER) {
            ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        }

        fieldAllowsSuggestions = info?.let { EnglishWords.fieldAllowsSuggestions(it.inputType) } ?: false

        viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
        EventBus.getDefault().post(Events.UpdateBufferViews())
    }

    // The text field tells us here about every change of its own, what we have just committed
    // included, so it is where the word being typed is looked at again.
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        refreshSuggestions()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy()")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        try {
            ChewingBridge.chewing.delete()
        } catch (e: Exception) {
            Log.e(logTag, "Failed to cleanup Chewing context", e)
        }
        EventBus.getDefault().unregister(this)
        englishPrediction.isEnabled = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyDown()")

        if (!isInputViewShown) {
            return super.onKeyDown(keyCode, event)
        }

        // have to make Back key work as is at very first, or some back operations will be blocked
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(logTag, "Back key pressed")
            requestHideSelf(0)
            return super.onKeyDown(keyCode, event)
        }

        assureViewBindingInitialized()

        // handles physical functional keys
        if (physicalKeyDispatcher[keyCode]?.onKeyDown(this, keyCode, event) == true) {
            return true
        }

        // handles printing (character) keys
        if (event != null && event.isPrintingKey) {
            // if a printing key has been pressed, assume that user have a physical keyboard connected anyway...
            val wasPhysicalKeyboardPresented = physicalKeyboardPresented
            physicalKeyboardPresented = true
            if (!wasPhysicalKeyboardPresented) {
                applyConversionEngine()
            }
            onPrintingKeyDown(event)
            return true
        }

        // pass-through other KeyEvent, or we will make some physical keys like volume keys invalid
        if (event != null) {
            currentInputConnection?.sendKeyEvent(event)
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyUp()")

        if (!isInputViewShown) {
            return super.onKeyUp(keyCode, event)
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, event)
        }

        assureViewBindingInitialized()

        // handles physical functional keys
        if (physicalKeyDispatcher[keyCode]?.onKeyUp(this, keyCode, event) == true) {
            return true
        }

        if (event?.isPrintingKey == true) {
            // Detect if a candidate had been chosen by user
            viewBinding.keyboardPanel.let {
                if (it.currentLayout == Layout.CANDIDATES) {
                    it.candidateKeySelected()
                }
            }
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyLongPress()")

        if (!isInputViewShown) {
            super.onKeyLongPress(keyCode, event)
            return false
        }

        assureViewBindingInitialized()

        // handles physical functional keys
        if (physicalKeyDispatcher[keyCode]?.onKeyLongPress(this, keyCode, event) == true) {
            return true
        }

        return super.onKeyLongPress(keyCode, event)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        physicalKeyboardPresented = false
        // restore the soft-keyboard conversion engine for the next session
        if (ChewingBridge.chewing.context != 0L) {
            applyConversionEngine()
        }
        Log.d(logTag, "onFinishInputView()")
    }

    private fun initializeChewing() {
        val dataPath = applicationInfo.dataDir
        ChewingUtil.setupChewingData(this, dataPath)
        ChewingBridge.chewing.connect(dataPath)
        Log.d(logTag, "Chewing context ptr: ${ChewingBridge.chewing.context}")

        if (sharedPreferences.getBoolean(USER_ENABLE_SPACE_AS_SELECTION, true)) {
            ChewingBridge.chewing.setSpaceAsSelection(1)
        }

        if (sharedPreferences.getBoolean(USER_PHRASE_CHOICE_REARWARD, false)) {
            ChewingBridge.chewing.setPhraseChoiceRearward(1)
        }

        // set conversion engine (traditional, fuzzy or default (chewing))
        applyConversionEngine()

        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(CANDIDATES_PER_PAGE)
        ChewingBridge.chewing.configSetInt("chewing.sort_candidates_by_frequency", 1)

        applySelectionKeys(
            sharedPreferences.getString(USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set)
        )
    }

    /**
     * Leaves the IME running without libchewing behind it: there is nothing to type with,
     * but the keyboard still draws itself and the user is told why.
     */
    private fun reportChewingInitFailure(logMessage: String, exception: Throwable) {
        ChewingBridge.chewing.context = 0
        Log.e(logTag, logMessage, exception)
        Toast.makeText(
            applicationContext, getString(R.string.libchewing_init_fail, exception.message), Toast.LENGTH_LONG
        ).show()
    }

    private fun applySelectionKeys(selectionKeysOption: String?) {
        selectionKeysOption?.let {
            ChewingBridge.chewing.setSelKey(SelectionKeys.valueOf(it).keys, CANDIDATES_PER_PAGE)
        }
    }

    private fun applyConversionEngine() {
        val key = if (physicalKeyboardPresented)
            USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
        else
            USER_CONVERSION_ENGINE
        val mode = sharedPreferences.getInt(
            key, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
        )
        ChewingBridge.chewing.configSetInt("chewing.conversion_engine", mode)
    }

    // handles both physical and virtual printing key-down events, routes to chewing.handleDefault()
    private fun onPrintingKeyDown(event: KeyEvent) {
        Log.d(logTag, "onPrintingKeyDown()")

        // Switch to compact layout if physical keyboard is present and current layout is not compact.
        // Skip while the candidates window is shown (e.g. the `‵` symbol picker): a printing key here
        // is a selection key, and switching away would tear down the candidates view before onKeyUp()
        // can render the next-level list, leaving sub-menus invisible (still effective in libchewing).
        if (physicalKeyboardPresented &&
            viewBinding.keyboardPanel.currentLayout != Layout.COMPACT &&
            viewBinding.keyboardPanel.currentLayout != Layout.CANDIDATES
        ) {
            viewBinding.keyboardPanel.switchToCompactLayout()
        }

        if (handleNumPadKey(event)) return
        if (handleGraveKeyForSymbols(event)) return
        if (handleAltIForImePicker(event)) return

        var keyPressed: Char = event.unicodeChar.toChar()

        if (shiftKeyIsActive) {
            currentInputConnection?.sendKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT))
            keyPressed = event.getUnicodeChar(META_SHIFT_ON).toChar()
        }

        if (handleCtrlKeySequence(event)) return

        // If in candidate selection window, the selection keys have to be mapped to DVORAK layout
        // TODO: This should be resolved in the future, in libchewing.
        if (ChewingBridge.chewing.getKBString() == BopomofoPhysicalKeyboards.KB_DVORAK_HSU.layout && viewBinding.keyboardPanel.currentLayout == Layout.CANDIDATES) {
            keyPressed = ChewingUtil.qwertyToDvorakKeyMapping(keyPressed)
        }

        ChewingBridge.chewing.handleDefault(keyPressed)
        EventBus.getDefault().post(Events.UpdateBufferViews())
        EventBus.getDefault().post(Events.UpdateCursorPosition())

        releaseShiftKeyIfNeeded()
    }

    private fun handleNumPadKey(event: KeyEvent): Boolean {
        if (!event.isNumPadKey()) return false
        currentInputConnection?.sendKeyEvent(event)
        EventBus.getDefault().post(Events.UpdateBufferViews())
        return true
    }

    private fun handleGraveKeyForSymbols(event: KeyEvent): Boolean {
        if (event.keyCode == KEYCODE_GRAVE && ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode && !event.isShiftPressed) {
            viewBinding.keyboardPanel.switchToLayout(Layout.SYMBOLS)
            return true
        }
        return false
    }

    private fun handleAltIForImePicker(event: KeyEvent): Boolean {
        if (event.keyCode == KEYCODE_I && event.isAltPressed) {
            (this.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.showInputMethodPicker()
            return true
        }
        return false
    }

    private fun handleCtrlKeySequence(event: KeyEvent): Boolean {
        if (!event.isCtrlPressed) return false
        currentInputConnection?.apply {
            when (event.keyCode) {
                KEYCODE_A -> performContextMenuAction(android.R.id.selectAll)
                KEYCODE_Z -> performContextMenuAction(android.R.id.undo)
                KEYCODE_X -> performContextMenuAction(android.R.id.cut)
                KEYCODE_C -> performContextMenuAction(android.R.id.copy)
                KEYCODE_V -> performContextMenuAction(android.R.id.paste)
                KEYCODE_R -> performContextMenuAction(android.R.id.redo)
            }
        }
        return true
    }

    private fun releaseShiftKeyIfNeeded() {
        if (shiftKeyIsActive && !shiftKeyIsLocked) {
            Log.d(logTag, "Release Shift key")
            releaseShiftKey()
        }
    }

    private fun releaseShiftKey() {
        viewBinding.keyboardPanel.releaseShiftKey()
        currentInputConnection?.sendKeyEvent(KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT))
    }

    /**
     * English word prediction goes with typing half-width English on the alphanumerical
     * keyboard, in a text field that welcomes suggestions. Anywhere else the keyboard stays
     * exactly as it has always been.
     */
    private fun suggestionStripApplies(): Boolean =
        englishPrediction.isEnabled &&
                fieldAllowsSuggestions &&
                ChewingBridge.chewing.context != 0L &&
                ChewingBridge.chewing.getChiEngMode() == ChiEngMode.SYMBOL.mode &&
                ChewingBridge.chewing.getShapeMode() == ShapeMode.HALF.mode &&
                viewBinding.keyboardPanel.currentLayout in PREDICTION_LAYOUTS

    /**
     * The suggestion strip and the buffers take turns above the keyboard: the strip while
     * predicting, and only when it has something to offer, the buffers whenever there is
     * something in them. English typed after Han characters still waiting in the pre-edit
     * buffer goes into that buffer rather than the text field, so there is nothing to predict
     * from then anyway.
     */
    private fun refreshSuggestions() {
        if (!::viewBinding.isInitialized) return

        val predicting = suggestionStripApplies() && !ChewingUtil.anyBufferIsNotEmpty()
        val suggestions = if (predicting) englishPrediction.suggest(englishWordBeforeCursor()) else emptyList()
        viewBinding.apply {
            flexBoxLayoutBufferTextViews.isVisible = !predicting
            suggestionStrip.show(suggestions)
            suggestionStrip.isVisible = suggestions.isNotEmpty()
        }
    }

    private fun englishWordBeforeCursor(): String =
        currentInputConnection?.getTextBeforeCursor(PREDICTION_LOOKBEHIND, 0)
            ?.let(EnglishWords::trailingWord)
            .orEmpty()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateBufferViews(event: Events.UpdateBufferViews) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        viewBinding.apply {
            launch { textViewPreEditBuffer.update() }
            launch { textViewBopomofoBuffer.update() }
        }
        refreshSuggestions()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyboardLayoutSwitched(event: Events.KeyboardLayoutSwitched) {
        refreshSuggestions()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSuggestionSelected(event: Events.SuggestionSelected) {
        val inputConnection = currentInputConnection ?: return
        // Look the word up again rather than trust what the strip was built from, the text
        // field may have changed under us in between.
        val typedWord = englishWordBeforeCursor()
        inputConnection.beginBatchEdit()
        inputConnection.deleteSurroundingText(typedWord.length, 0)
        inputConnection.commitText("${event.word} ", 1)
        inputConnection.endBatchEdit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPosition(event: Events.UpdateCursorPosition) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        viewBinding.textViewPreEditBuffer.updateCursorPosition()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPositionToBegin(event: Events.UpdateCursorPositionToBegin) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        viewBinding.textViewPreEditBuffer.updateCursorPositionToBegin()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPositionToEnd(event: Events.UpdateCursorPositionToEnd) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        viewBinding.textViewPreEditBuffer.updateCursorPositionToEnd()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchToLayout(event: Events.SwitchToLayout) {
        viewBinding.apply {
            keyboardPanel.switchToLayout(event.layout)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRequestHideIme(event: Events.RequestHideIme) {
        if (viewBinding.keyboardPanel.currentLayout in DISMISSIBLE_LAYOUTS) {
            requestHideSelf(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExitKeyboardSubLayouts(event: Events.ExitKeyboardSubLayouts) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        viewBinding.keyboardPanel.apply {
            if (currentLayout in SUB_LAYOUTS) {
                ChewingBridge.chewing.candClose()
                // reset last cursor position
                lastChewingCursor = 0
                switchToLayout(Layout.MAIN)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommitTextInChewingCommitBuffer(event: Events.CommitTextInChewingCommitBuffer) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        currentInputConnection?.commitText(
            ChewingBridge.chewing.commitString(), 1
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchToNextInputMethod(event: Events.SwitchToNextInputMethod) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm = applicationContext.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return
            val imeToken: IBinder? = viewBinding.root.windowToken
            @Suppress("DEPRECATION") imm.switchToNextInputMethod(imeToken, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSendDownUpKeyEvents(event: Events.SendDownUpKeyEvents) {
        sendDownUpKeyEvents(event.keycode)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCandidateButtonSelected(event: Events.CandidateButtonSelected) {
        viewBinding.keyboardPanel.candidateButtonSelected(event.candidate)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPrintingKeyDown(event: Events.PrintingKeyDown) {
        event.characterKey.keyCodeString?.let { keycodeString ->
            val keyEvent = KeyEvent(
                ACTION_DOWN, KeyEvent.keyCodeFromString(keycodeString)
            )
            this@GuilelessBopomofoService.onPrintingKeyDown(keyEvent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateShiftKeyState(event: Events.UpdateShiftKeyState) {
        shiftKeyIsActive = event.isActive
        shiftKeyIsLocked = event.isLocked
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleKeyboardMainLayoutMode(event: Events.ToggleKeyboardMainLayoutMode) {
        Log.d(logTag, event::class.simpleName ?: "Event")
        // Always reset Shift state when switching main layouts.
        releaseShiftKey()
        viewBinding.keyboardPanel.toggleMainLayoutMode()
        // the compact layout stays the same layout whichever mode it is in
        refreshSuggestions()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleFullOrHalfWidthMode(event: Events.ToggleFullOrHalfWidthMode) {
        val shapeMode: String = when (ChewingBridge.chewing.getShapeMode()) {
            ShapeMode.HALF.mode -> {
                ChewingBridge.chewing.setShapeMode(ShapeMode.FULL.mode)
                getString(R.string.full_width_mode)
            }

            else -> {
                ChewingBridge.chewing.setShapeMode(ShapeMode.HALF.mode)
                getString(R.string.half_width_mode)
            }
        }

        refreshSuggestions()

        if (viewBinding.keyboardPanel.currentLayout == Layout.COMPACT) {
            viewBinding.keyboardPanel.setShapeMode(shapeMode)
        } else {
            Toast.makeText(
                applicationContext, getString(R.string.shape_mode_changed, shapeMode), Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEnterKeyDownWhenBufferIsEmpty(event: Events.EnterKeyDownWhenBufferIsEmpty) {
        Log.d(logTag, event::class.simpleName ?: "Event")

        val behavior = currentInputEditorInfo?.let(EnterKeyBehaviorResolver::resolve)
            ?: EnterKeyBehavior.NewLine

        when (behavior) {
            is EnterKeyBehavior.EditorAction -> {
                // A false return means the input connection died under us, never that the
                // text field turned the action down. The plain Enter is a last resort that
                // will quietly do nothing in that case.
                if (currentInputConnection?.performEditorAction(behavior.actionId) != true) {
                    sendDownUpKeyEvents(KEYCODE_ENTER)
                }
            }

            EnterKeyBehavior.NewLine -> sendDownUpKeyEvents(KEYCODE_ENTER)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDirectionKeyDown(event: Events.DirectionKeyDown) {
        if (ChewingBridge.chewing.bufferLen() > 0) {
            viewBinding.textViewPreEditBuffer.cursorMovedBy(PreEditBufferTextView.CursorMovedFrom.PHYSICAL_KEYBOARD)
        } else if (ChewingUtil.candidateWindowClosed()) {
            // Nothing of ours to move through, so let the text field move its own cursor.
            when (event.direction) {
                DirectionKey.LEFT -> sendDownUpKeyEvents(KEYCODE_DPAD_LEFT)
                DirectionKey.RIGHT -> sendDownUpKeyEvents(KEYCODE_DPAD_RIGHT)
            }
        }

        // toggle to next page of candidates
        viewBinding.keyboardPanel.apply {
            if (currentLayout == Layout.CANDIDATES && ChewingUtil.candidateWindowOpened()) {
                renderCandidatesLayout()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(logTag, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        assureViewBindingInitialized()

        // A new orientation or screen size picks its layout resources afresh, so never let
        // the panel hand back what it inflated for the previous configuration.
        viewBinding.keyboardPanel.invalidateRenderedLayout()

        if (isInputViewShown) {
            Log.d(logTag, "onConfigurationChanged(): refresh the input view.")
            // toggle main layout automatically between physical keyboard being connected and disconnected
            viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
            // there will be a short (time) window that InputMethod.hideSoftInput() will be called when user turn own physical keyboard on/off,
            // so have to call showWindow() here to make the soft input visible:
            showWindow(true)
        }

        if (physicalKeyboardPresented) {
            viewBinding.keyboardPanel.switchToCompactLayout()
        }
    }

    private fun assureViewBindingInitialized() {
        Log.d(logTag, "assureViewBindingInitialized()")
        if (!::viewBinding.isInitialized) {
            Log.d(logTag, "initialize viewBinding")
            setInputView(onCreateInputView())
        }
    }

    // triggered if any sharedPreference has been changed
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            // Reload the main layout
            USER_SOFT_KEYBOARD_LAYOUT,
            USER_PHYSICAL_KEYBOARD_LAYOUT,
            USER_DISPLAY_HSU_QWERTY_LAYOUT,
            USER_DISPLAY_ETEN26_QWERTY_LAYOUT,
            USER_KEY_BUTTON_HEIGHT,
            USER_ENABLE_IME_SWITCH,
            USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH,
                -> {
                if (::viewBinding.isInitialized) {
                    // The layouts are built from these, so they have to be inflated again.
                    viewBinding.keyboardPanel.invalidateRenderedLayout()
                    viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
                }
            }

            USER_ENABLE_SPACE_AS_SELECTION -> {
                val enabled = sharedPreferences?.getBoolean(key, true) ?: true
                ChewingBridge.chewing.setSpaceAsSelection(if (enabled) 1 else 0)
            }

            USER_PHRASE_CHOICE_REARWARD -> {
                val enabled = sharedPreferences?.getBoolean(key, false) ?: false
                ChewingBridge.chewing.setPhraseChoiceRearward(if (enabled) 1 else 0)
            }

            USER_HAPTIC_FEEDBACK_STRENGTH -> {
                userHapticFeedbackStrength =
                    sharedPreferences?.getInt(key, defaultHapticFeedbackStrength)
                        ?: defaultHapticFeedbackStrength
            }

            // No-op: handled elsewhere (onEvaluateFullscreenMode, Vibratable)
            SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS,
            USER_FULLSCREEN_WHEN_IN_LANDSCAPE,
            USER_FULLSCREEN_WHEN_IN_PORTRAIT,
                -> {}

            USER_CANDIDATE_SELECTION_KEYS_OPTION -> {
                applySelectionKeys(sharedPreferences?.getString(key, SelectionKeys.NUMBER_ROW.set))
            }

            USER_CONVERSION_ENGINE,
            USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD -> {
                applyConversionEngine()
            }

            USER_ENABLE_ENGLISH_PREDICTION -> {
                englishPrediction.isEnabled = sharedPreferences?.getBoolean(key, false) ?: false
                refreshSuggestions()
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}