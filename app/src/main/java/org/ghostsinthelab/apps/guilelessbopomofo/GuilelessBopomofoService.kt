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

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.io.IOException
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_ENGLISH_PREDICTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.deviceIsEmulator
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.buffers.PreEditBuffer
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyInput
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.BigramPredictor
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.EnglishPrediction
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.EnglishSuggestions
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.ScowlWordPredictor
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeRoot
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehavior
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehaviorResolver
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences

class GuilelessBopomofoService : InputMethodService(), ImeHost, SharedPreferences.OnSharedPreferenceChangeListener,
    LifecycleOwner, SavedStateRegistryOwner {
    private val logTag = "GuilelessBopomofoSvc"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var chewingSettings: ChewingSettings

    // What the input view shows; it lives as long as the service, the view only reads it.
    private val uiState = ImeUiState()
    private lateinit var keyboardController: KeyboardController
    private val preEditBuffer = PreEditBuffer(uiState, this)

    // keyboardController is created in onCreate(), and the keys only arrive after that
    private val physicalKeyInput by lazy {
        PhysicalKeyInput(this, uiState, keyboardController, object : PhysicalKeyInput.Editor {
            override val isInputViewShown: Boolean get() = this@GuilelessBopomofoService.isInputViewShown
            override val inputConnection: InputConnection? get() = currentInputConnection
            override fun hideKeyboard() = requestHideSelf(0)
            override fun physicalKeyboardDetected() = chewingSettings.applyConversionEngine()
            override fun showInputMethodPicker() {
                (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.showInputMethodPicker()
            }

            override fun sendKeyEventToField(event: KeyEvent) {
                // the cursor moves on past the chosen word, if there was one
                englishSuggestions.forgetAutoSpace()
                currentInputConnection?.sendKeyEvent(event)
            }
        })
    }

    // InputMethodService is no Activity: Compose wants a lifecycle and saved state to hang
    // on to, so the service provides both itself.
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val englishSuggestions = EnglishSuggestions(
        EnglishPrediction(
            lifecycleScope,
            openWordList = { assets.open(ScowlWordPredictor.ASSET_PATH) },
            openBigramList = { assets.open(BigramPredictor.ASSET_PATH) },
        ),
        uiState,
        object : EnglishSuggestions.Host {
            override val currentLayout: Layout get() = keyboardController.currentLayout
            override val inputConnection: InputConnection? get() = currentInputConnection
            override fun typingHalfWidthEnglish(): Boolean =
                ChewingBridge.chewing.context != 0L &&
                        ChewingBridge.chewing.getChiEngMode() == ChiEngMode.SYMBOL.mode &&
                        ChewingBridge.chewing.getShapeMode() == ShapeMode.HALF.mode
            override fun buffersAreEmpty(): Boolean = !ChewingUtil.anyBufferIsNotEmpty()
        },
    )

    // Actions asked for while one is being carried out wait their turn, as they did on
    // EventBus: a handler that asks for another action sees it done after it returns, not
    // in the middle of its own work.
    private val pendingActions = ArrayDeque<ImeAction>()
    private var dispatching = false

    override fun dispatch(action: ImeAction) {
        // libchewing, and everything the handlers touch, is main-thread only
        check(Looper.getMainLooper().isCurrentThread) { "ImeAction dispatched off the main thread" }
        pendingActions.addLast(action)
        if (dispatching) return
        dispatching = true
        try {
            while (pendingActions.isNotEmpty()) carryOut(pendingActions.removeFirst())
        } finally {
            // whatever happened, the next key press starts with a clean slate
            pendingActions.clear()
            dispatching = false
        }
    }

    // A handler that fails must not take the input method down with it, nor the actions
    // queued behind it. Only the action's kind is logged: some carry the user's text.
    private fun carryOut(action: ImeAction) {
        try {
            handle(action)
        } catch (exception: RuntimeException) {
            Log.e(logTag, "${action::class.simpleName} failed", exception)
        }
    }

    private fun handle(action: ImeAction) = when (action) {
        is ImeAction.UpdateBufferViews -> onUpdateBufferViews()
        is ImeAction.KeyboardLayoutSwitched -> onKeyboardLayoutSwitched()
        is ImeAction.SuggestionSelected -> onSuggestionSelected(action)
        is ImeAction.UpdateCursorPosition -> onUpdateCursorPosition()
        is ImeAction.UpdateCursorPositionToBegin -> onUpdateCursorPositionToBegin()
        is ImeAction.UpdateCursorPositionToEnd -> onUpdateCursorPositionToEnd()
        is ImeAction.SwitchToLayout -> onSwitchToLayout(action)
        is ImeAction.RequestHideIme -> onRequestHideIme()
        is ImeAction.ExitKeyboardSubLayouts -> onExitKeyboardSubLayouts()
        is ImeAction.CommitTextInChewingCommitBuffer -> onCommitTextInChewingCommitBuffer(action)
        is ImeAction.SwitchToNextInputMethod -> onSwitchToNextInputMethod()
        is ImeAction.SendDownUpKeyEvents -> onSendDownUpKeyEvents(action)
        is ImeAction.CandidateButtonSelected -> onCandidateButtonSelected(action)
        is ImeAction.PrintingKeyDown -> onPrintingKeyDown(action)
        is ImeAction.PreEditBufferTouched -> onPreEditBufferTouched(action)
        is ImeAction.ToggleKeyboardMainLayoutMode -> onToggleKeyboardMainLayoutMode()
        is ImeAction.ToggleFullOrHalfWidthMode -> onToggleFullOrHalfWidthMode()
        is ImeAction.EnterKeyDownWhenBufferIsEmpty -> onEnterKeyDownWhenBufferIsEmpty()
        is ImeAction.DirectionKeyDown -> onDirectionKeyDown(action)
    }

    override val context: Context get() = this

    companion object {
        val defaultHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength

        @Volatile
        var userHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength
    }

    override fun onCreate() {
        Log.d(logTag, "onCreate()")
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

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
        keyboardController = KeyboardController(uiState, sharedPreferences, this)
        chewingSettings = ChewingSettings(sharedPreferences)

        try {
            chewingSettings.initialize(this)
        } catch (exception: UnsatisfiedLinkError) {
            reportChewingInitFailure("Failed to load native library", exception)
        } catch (exception: Chewing.ChewingInitException) {
            reportChewingInitFailure("Failed to initialize Chewing", exception)
        } catch (exception: IOException) {
            reportChewingInitFailure("Failed to setup Chewing data", exception)
        }

        // Listen only once initialized, so that a reconnection never finds a half-built service.
        ChewingBridge.addReconnectListener(::onChewingReconnected)

        userHapticFeedbackStrength =
            sharedPreferences.getInt(USER_HAPTIC_FEEDBACK_STRENGTH, defaultHapticFeedbackStrength)

        englishSuggestions.isEnabled = sharedPreferences.getBoolean(USER_ENABLE_ENGLISH_PREDICTION, false)
        englishSuggestions.isNextWordEnabled =
            sharedPreferences.getBoolean(USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION, false)
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
        // ComposeView looks the owners up from the window it is attached to.
        window.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                GuilelessTheme {
                    ImeRoot(uiState, this@GuilelessBopomofoService)
                }
            }
        }
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
        chewingSettings.applyConversionEngine()

        // if the input type is phone or number, switch to symbol (alphanumeric) mode
        val inputType = info?.inputType?.and(InputType.TYPE_MASK_CLASS)
        if (inputType == InputType.TYPE_CLASS_PHONE || inputType == InputType.TYPE_CLASS_NUMBER) {
            ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        }

        englishSuggestions.onStartInput(info?.inputType)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        keyboardController.switchToLayout(Layout.MAIN)
        dispatch(ImeAction.UpdateBufferViews)
    }

    // The text field tells us here about every change of its own, what we have just committed
    // included, so it is where the word being typed is looked at again.
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        // the selection moving while Shift is held, e.g. a Shift-click with a mouse, is no tap
        physicalKeyInput.onSelectionMoved()
        englishSuggestions.onSelectionUpdated(newSelStart, newSelEnd)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy()")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        // Also forgets the pointer, so the settings screens, sharing this object, never use
        // the freed context.
        try {
            ChewingBridge.chewing.delete()
        } catch (e: Exception) {
            Log.e(logTag, "Failed to cleanup Chewing context", e)
        }
        ChewingBridge.removeReconnectListener(::onChewingReconnected)
        englishSuggestions.isEnabled = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        physicalKeyInput.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
        physicalKeyInput.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event)

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean =
        physicalKeyInput.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event)

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        physicalKeyboardPresented = false
        physicalKeyInput.onInputViewFinished()
        // restore the soft-keyboard conversion engine for the next session
        if (ChewingBridge.chewing.context != 0L) {
            chewingSettings.applyConversionEngine()
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        Log.d(logTag, "onFinishInputView()")
    }

    // The user phrase manager flushes libchewing by swapping in a fresh context, which knows
    // none of the settings above; neither does it know the keyboard type the layout set.
    private fun onChewingReconnected() {
        Log.d(logTag, "onChewingReconnected()")
        chewingSettings.applyAll()
        keyboardController.returnToMainLayout()
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

    private fun onUpdateBufferViews() {
        Log.d(logTag, "onUpdateBufferViews()")
        preEditBuffer.update()
        uiState.bopomofoText = ChewingBridge.chewing.bopomofoStringStatic()
        englishSuggestions.refresh()
    }

    private fun onKeyboardLayoutSwitched() {
        englishSuggestions.refresh()
    }

    private fun onSuggestionSelected(action: ImeAction.SuggestionSelected) {
        englishSuggestions.choose(action.word)
    }

    private fun onUpdateCursorPosition() {
        Log.d(logTag, "onUpdateCursorPosition()")
        preEditBuffer.updateCursorPosition()
    }

    private fun onUpdateCursorPositionToBegin() {
        Log.d(logTag, "onUpdateCursorPositionToBegin()")
        preEditBuffer.updateCursorPositionToBegin()
    }

    private fun onUpdateCursorPositionToEnd() {
        Log.d(logTag, "onUpdateCursorPositionToEnd()")
        preEditBuffer.updateCursorPositionToEnd()
    }

    private fun onSwitchToLayout(action: ImeAction.SwitchToLayout) {
        keyboardController.switchToLayout(action.layout)
    }

    private fun onRequestHideIme() {
        if (keyboardController.isDismissible) requestHideSelf(0)
    }

    private fun onExitKeyboardSubLayouts() {
        keyboardController.exitSubLayout()
    }

    private fun onCommitTextInChewingCommitBuffer(action: ImeAction.CommitTextInChewingCommitBuffer) {
        Log.d(logTag, "onCommitTextInChewingCommitBuffer()")
        englishSuggestions.commit(action.text)
    }

    private fun onSwitchToNextInputMethod() {
        Log.d(logTag, "onSwitchToNextInputMethod()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm = applicationContext.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return
            val imeToken = window.window?.attributes?.token
            @Suppress("DEPRECATION") imm.switchToNextInputMethod(imeToken, false)
        }
    }

    private fun onSendDownUpKeyEvents(action: ImeAction.SendDownUpKeyEvents) {
        // backspace, arrows and the like: the space is no longer right behind the cursor
        englishSuggestions.forgetAutoSpace()
        sendDownUpKeyEvents(action.keycode)
    }

    private fun onCandidateButtonSelected(action: ImeAction.CandidateButtonSelected) {
        keyboardController.candidateButtonSelected(action.candidate)
    }

    private fun onPrintingKeyDown(action: ImeAction.PrintingKeyDown) {
        val keyEvent = KeyEvent(ACTION_DOWN, KeyEvent.keyCodeFromString(action.keyCodeString))
        physicalKeyInput.onPrintingKeyDown(keyEvent)
    }

    private fun onPreEditBufferTouched(action: ImeAction.PreEditBufferTouched) {
        preEditBuffer.touchedAt(action.offset)
        keyboardController.switchToLayout(Layout.CANDIDATES)
    }

    private fun onToggleKeyboardMainLayoutMode() {
        Log.d(logTag, "onToggleKeyboardMainLayoutMode()")
        // Always reset Shift state when switching main layouts.
        physicalKeyInput.releaseShiftKey()
        keyboardController.toggleMainLayoutMode()
        // the compact layout stays the same layout whichever mode it is in
        englishSuggestions.refresh()
    }

    private fun onToggleFullOrHalfWidthMode() {
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

        englishSuggestions.refresh()

        if (keyboardController.currentLayout == Layout.COMPACT) {
            keyboardController.refreshCompactIndicators()
        } else {
            Toast.makeText(
                applicationContext, getString(R.string.shape_mode_changed, shapeMode), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onEnterKeyDownWhenBufferIsEmpty() {
        Log.d(logTag, "onEnterKeyDownWhenBufferIsEmpty()")
        englishSuggestions.forgetAutoSpace()

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

    private fun onDirectionKeyDown(action: ImeAction.DirectionKeyDown) {
        if (ChewingBridge.chewing.bufferLen() > 0) {
            preEditBuffer.cursorMovedBy(PreEditBuffer.CursorMovedFrom.PHYSICAL_KEYBOARD)
        } else if (ChewingUtil.candidateWindowClosed()) {
            // Nothing of ours to move through, so let the text field move its own cursor.
            englishSuggestions.forgetAutoSpace()
            when (action.direction) {
                DirectionKey.LEFT -> sendDownUpKeyEvents(KEYCODE_DPAD_LEFT)
                DirectionKey.RIGHT -> sendDownUpKeyEvents(KEYCODE_DPAD_RIGHT)
            }
        }

        // toggle to next page of candidates
        keyboardController.apply {
            if (currentLayout == Layout.CANDIDATES && ChewingUtil.candidateWindowOpened()) {
                renderCandidatesLayout()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(logTag, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        if (isInputViewShown) {
            Log.d(logTag, "onConfigurationChanged(): refresh the input view.")
            // toggle main layout automatically between physical keyboard being connected and disconnected
            keyboardController.returnToMainLayout()
            // there will be a short (time) window that InputMethod.hideSoftInput() will be called when user turn own physical keyboard on/off,
            // so have to call showWindow() here to make the soft input visible:
            showWindow(true)
        }

        if (physicalKeyboardPresented) {
            keyboardController.switchToCompactLayout()
        }
    }

    // triggered if any sharedPreference has been changed
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (chewingSettings.onPreferenceChanged(key)) return
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
                // The keyboards are built from these, so switch again to pick them up.
                keyboardController.returnToMainLayout()
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

            USER_ENABLE_ENGLISH_PREDICTION -> {
                englishSuggestions.isEnabled = sharedPreferences?.getBoolean(key, false) ?: false
            }

            USER_ENABLE_ENGLISH_NEXT_WORD_PREDICTION -> {
                englishSuggestions.isNextWordEnabled = sharedPreferences?.getBoolean(key, false) ?: false
            }
        }
    }
}