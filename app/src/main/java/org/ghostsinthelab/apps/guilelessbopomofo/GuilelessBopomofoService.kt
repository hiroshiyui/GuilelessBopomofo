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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
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
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EdgeToEdge
import org.ghostsinthelab.apps.guilelessbopomofo.utils.KeyEventExtension
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class GuilelessBopomofoService : InputMethodService(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener,
    KeyEventExtension, EdgeToEdge {
    private val logTag = "GuilelessBopomofoSvc"
    private var shiftKeyIsLocked: Boolean = false
    private var shiftKeyIsActive: Boolean = false

    private lateinit var viewBinding: ImeLayoutBinding
    private lateinit var physicalKeyDispatcher: Map<Int, PhysicalKeyHandler>
    private lateinit var sharedPreferences: SharedPreferences
    private val chewingDataFiles = ChewingUtil.listOfDataFiles()

    companion object {
        val defaultHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength
        var userHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength
    }

    override fun onCreate() {
        Log.d(logTag, "onCreate()")
        super.onCreate()

        if (Build.MANUFACTURER == "Google" && Build.BOARD.startsWith("goldfish_")) {
            deviceIsEmulator = true
        }

        EventBus.getDefault().register(this)

        // register physical key handlers
        initializePhysicalKeyDispatcher()

        // set Back key disposition
        backDisposition = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BACK_DISPOSITION_ADJUST_NOTHING
        } else {
            BACK_DISPOSITION_DEFAULT
        }

        sharedPreferences = getSharedPreferences(APP_SHARED_PREFERENCES, MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // Initializing Chewing
        try {
            val dataPath = applicationInfo.dataDir
            setupChewingData(dataPath)
            ChewingBridge.chewing.connect(dataPath)
            ChewingBridge.chewing.context.let {
                Log.d(logTag, "Chewing context ptr: $it")
            }

            if (sharedPreferences.getBoolean(USER_ENABLE_SPACE_AS_SELECTION, true)) {
                ChewingBridge.chewing.setSpaceAsSelection(1)
            }

            if (sharedPreferences.getBoolean(USER_PHRASE_CHOICE_REARWARD, false)) {
                ChewingBridge.chewing.setPhraseChoiceRearward(1)
            }

            // set conversion engine (traditional, fuzzy or default (chewing))
            sharedPreferences.getInt(
                USER_CONVERSION_ENGINE, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
            ).let { ChewingBridge.chewing.configSetInt("chewing.conversion_engine", it) }

            ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
            ChewingBridge.chewing.setCandPerPage(10)

            sharedPreferences.getString(
                USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set
            )?.let {
                ChewingBridge.chewing.setSelKey(SelectionKeys.valueOf(it).keys, 10)
            }
        } catch (exception: Exception) {
            val exceptionDescription: String = getString(R.string.libchewing_init_fail, exception.message)
            Toast.makeText(applicationContext, exceptionDescription, Toast.LENGTH_LONG).show()
            exception.let { e ->
                e.printStackTrace()
                e.message?.let { msg ->
                    Log.e(logTag, msg)
                }
            }
        }

        userHapticFeedbackStrength =
            sharedPreferences.getInt(USER_HAPTIC_FEEDBACK_STRENGTH, defaultHapticFeedbackStrength)
    }


    override fun onCreateCandidatesView(): View? {
        // I want to implement my own candidate selection UI
        Log.d(logTag, "onCreateCandidatesView()")
        return null
    }

    // Disable fullscreen mode when device's orientation is landscape
    override fun onEvaluateFullscreenMode(): Boolean {
        Log.d(logTag, "onEvaluateFullscreenMode()")

        if (sharedPreferences.getBoolean(
                USER_FULLSCREEN_WHEN_IN_LANDSCAPE, true
            ) && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            Log.d(logTag, "Now on landscape orientation.")
            return true
        }

        if (sharedPreferences.getBoolean(
                USER_FULLSCREEN_WHEN_IN_PORTRAIT, false
            ) && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            Log.d(logTag, "Now on portrait orientation.")
            return true
        }

        return false
    }

    override fun onCreateInputView(): View {
        Log.d(logTag, "onCreateInputView()")
        viewBinding = ImeLayoutBinding.inflate(this.layoutInflater)

        applyInputViewBottomEdgeWithGradient(viewBinding.root, viewBinding.imeBottomGradientSpacer)

        return viewBinding.root
    }

    override fun onInitializeInterface() {
        Log.d(logTag, "onInitializeInterface()")
        super.onInitializeInterface()
    }

    override fun onEvaluateInputViewShown(): Boolean {
        Log.d(logTag, "onEvaluateInputViewShown()")
        super.onEvaluateInputViewShown()
        // always show the input view whether physical keyboard is connected or not
        return true
    }

    override fun onBindInput() {
        Log.d(logTag, "onBindInput()")
        super.onBindInput()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        Log.d(logTag, "onStartInput()")
        super.onStartInput(attribute, restarting)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(logTag, "onStartInputView()")

        // detect if physical keyboard is presented
        physicalKeyboardPresented =
            (resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY) && (resources.configuration.hardKeyboardHidden ==
                    Configuration.HARDKEYBOARDHIDDEN_NO) && (!deviceIsEmulator)

        val inputType = info?.inputType?.and(InputType.TYPE_MASK_CLASS)
        // if the input type is phone or number, switch to symbol (alphanumeric) mode
        when (inputType) {
            InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER -> {
                ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
            }
        }

        viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
        EventBus.getDefault().post(Events.UpdateBufferViews())
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(logTag, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy()")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        ChewingBridge.chewing.delete()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyDown()")

        if (!isInputViewShown) {
            return super.onKeyDown(keyCode, event)
        }

        // have to make Back key work as is at very first, or some back operations will be blocked
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(logTag, "Back key pressed")
            this@GuilelessBopomofoService.requestHideSelf(0)
            return super.onKeyDown(keyCode, event)
        }

        assureViewBindingInitialized()

        // handles physical functional keys
        val handler = physicalKeyDispatcher[keyCode]
        if (handler != null) {
            if (handler.onKeyDown(this, keyCode, event)) {
                return true // Event was handled by our specific class
            }
        }

        // handles printing (character) keys
        if (event != null && event.isPrintingKey) {
            // if a printing key has been pressed, assume that user have a physical keyboard connected anyway...
            physicalKeyboardPresented = true
            onPrintingKeyDown(event)
            return true
        }

        // pass-through other KeyEvent, or we will make some physical keys like volume keys invalid
        currentInputConnection.sendKeyEvent(event)

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
        val handler = physicalKeyDispatcher[keyCode]
        if (handler != null) {
            if (handler.onKeyUp(this, keyCode, event)) {
                return true // Event was handled by our specific class
            }
        }

        if (event?.isPrintingKey == true) {
            // Detect if a candidate had been chosen by user
            viewBinding.keyboardPanel.let {
                if (it.currentLayout == Layout.CANDIDATES) {
                    if (ChewingUtil.candidateWindowClosed()) {
                        it.candidateKeySelected(event)
                    } else {
                        it.renderCandidatesLayout()
                    }
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
        val handler = physicalKeyDispatcher[keyCode]
        if (handler != null) {
            if (handler.onKeyLongPress(this, keyCode, event)) {
                return true // Event was handled by our specific class
            }
        }

        return super.onKeyLongPress(keyCode, event)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        Log.d(logTag, "onWindowHidden()")
    }

    override fun onWindowShown() {
        super.onWindowShown()
        Log.d(logTag, "onWindowShown()")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        physicalKeyboardPresented = false
        Log.d(logTag, "onFinishInputView()")
    }

    private fun initializePhysicalKeyDispatcher() {
        physicalKeyDispatcher = mapOf(
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
            // Add more mappings here for each physical key you want to handle separately
        )
    }

    // handles both physical and virtual printing key-down events, routes to chewing.handleDefault()
    private fun onPrintingKeyDown(event: KeyEvent) {
        Log.d(logTag, "onPrintingKeyDown()")

        // Switch to compact layout if physical keyboard is present and current layout is not compact
        if (physicalKeyboardPresented && viewBinding.keyboardPanel.currentLayout != Layout.COMPACT) {
            viewBinding.keyboardPanel.switchToCompactLayout()
        }

        // Consider keys in NumPad
        if (event.isNumPadKey()) {
            currentInputConnection.sendKeyEvent(event)
            EventBus.getDefault().post(Events.UpdateBufferViews())
            return
        }

        // when user press '`', switch to symbols layout
        if (event.keyCode == KEYCODE_GRAVE && ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode && !event.isShiftPressed) {
            viewBinding.keyboardPanel.switchToLayout(Layout.SYMBOLS)
            return
        }

        // when user press Alt + I, then show IME picker
        if (event.keyCode == KEYCODE_I && event.isAltPressed) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return
        }

        var keyPressed: Char = event.unicodeChar.toChar()

        if (shiftKeyIsActive) {
            currentInputConnection.sendKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT))
            keyPressed = event.getUnicodeChar(META_SHIFT_ON).toChar()
        }

        // common Ctrl-key handling
        if (event.isCtrlPressed) {
            currentInputConnection.apply {
                when (event.keyCode) {
                    KEYCODE_A -> {
                        performContextMenuAction(android.R.id.selectAll)
                    }

                    KEYCODE_Z -> {
                        performContextMenuAction(android.R.id.undo)
                    }

                    KEYCODE_X -> {
                        performContextMenuAction(android.R.id.cut)
                    }

                    KEYCODE_C -> {
                        performContextMenuAction(android.R.id.copy)
                    }

                    KEYCODE_V -> {
                        performContextMenuAction(android.R.id.paste)
                    }

                    KEYCODE_R -> {
                        performContextMenuAction(android.R.id.redo)
                    }
                }
            }
            return
        }

        // If in candidate selection window, the selection keys have to be mapped to DVORAK layout
        // ** This dirty hack should be resolved in the future, in libchewing. **
        if (ChewingBridge.chewing.getKBString() == "KB_DVORAK_HSU" && viewBinding.keyboardPanel.currentLayout == Layout.CANDIDATES) {
            keyPressed = ChewingUtil.qwertyToDvorakKeyMapping(keyPressed)
        }

        ChewingBridge.chewing.handleDefault(keyPressed)
        EventBus.getDefault().post(Events.UpdateBufferViews())
        EventBus.getDefault().post(Events.UpdateCursorPosition())

        // release Shift key and make the button background color back to normal
        if (shiftKeyIsActive && !shiftKeyIsLocked) {
            Log.d(logTag, "Release Shift key")
            viewBinding.keyboardPanel.releaseShiftKey()
            currentInputConnection.sendKeyEvent(KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT))
        }
        return
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateBufferViews(event: Events.UpdateBufferViews) {
        Log.d(logTag, event.toString())
        viewBinding.apply {
            launch { textViewPreEditBuffer.update() }
            launch { textViewBopomofoBuffer.update() }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPosition(event: Events.UpdateCursorPosition) {
        Log.d(logTag, event.toString())
        viewBinding.textViewPreEditBuffer.updateCursorPosition()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPositionToBegin(event: Events.UpdateCursorPositionToBegin) {
        Log.d(logTag, event.toString())
        viewBinding.textViewPreEditBuffer.updateCursorPositionToBegin()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCursorPositionToEnd(event: Events.UpdateCursorPositionToEnd) {
        Log.d(logTag, event.toString())
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
        viewBinding.keyboardPanel.apply {
            if (this.currentLayout in listOf(
                    Layout.MAIN, Layout.COMPACT, Layout.QWERTY, Layout.DVORAK
                )
            ) {
                this@GuilelessBopomofoService.requestHideSelf(0)
            }
        }
        return
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExitKeyboardSubLayouts(event: Events.ExitKeyboardSubLayouts) {
        Log.d(logTag, event.toString())
        viewBinding.keyboardPanel.apply {
            if (this.currentLayout in listOf(
                    Layout.SYMBOLS, Layout.CANDIDATES
                )
            ) {
                ChewingBridge.chewing.candClose()
                // reset last cursor position
                this.lastChewingCursor = 0
                this.switchToLayout(Layout.MAIN)

            }
        }
        return
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommitTextInChewingCommitBuffer(event: Events.CommitTextInChewingCommitBuffer) {
        Log.d(logTag, event.toString())
        currentInputConnection.commitText(
            ChewingBridge.chewing.commitString(), 1
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchToNextInputMethod(event: Events.SwitchToNextInputMethod) {
        Log.d(logTag, event.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        Log.d(logTag, event.toString())
        // Always reset Shift state when switching main layouts.
        viewBinding.keyboardPanel.releaseShiftKey()
        currentInputConnection.sendKeyEvent(KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT))
        viewBinding.keyboardPanel.toggleMainLayoutMode()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleFullOrHalfWidthMode(event: Events.ToggleFullOrHalfWidthMode) {
        var shapeMode: String = ""
        when (ChewingBridge.chewing.getShapeMode()) {
            ShapeMode.HALF.mode -> {
                ChewingBridge.chewing.setShapeMode(ShapeMode.FULL.mode)
                shapeMode = getString(R.string.full_width_mode)
            }

            ShapeMode.FULL.mode -> {
                ChewingBridge.chewing.setShapeMode(ShapeMode.HALF.mode)
                shapeMode = getString(R.string.half_width_mode)
            }
        }

        if (viewBinding.keyboardPanel.currentLayout == Layout.COMPACT) {
            viewBinding.keyboardPanel.setShapeMode(shapeMode)
        } else {
            Toast.makeText(
                applicationContext, getString(R.string.shape_mode_changed, shapeMode), Toast.LENGTH_SHORT
            ).show()
        }

        return
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEnterKeyDownWhenBufferIsEmpty(event: Events.EnterKeyDownWhenBufferIsEmpty) {
        Log.d(logTag, event.toString())

        var multiLineEditText = false

        currentInputEditorInfo?.apply {
            // Is it a multiple line text field?
            if ((this.inputType and InputType.TYPE_MASK_CLASS and InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
                if ((this.inputType and InputType.TYPE_MASK_FLAGS and InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
                    multiLineEditText = true
                }
            }

            // Just do as press Enter, never care about the defined action if we are now in a multiple line text field
            if (multiLineEditText) {
                this@GuilelessBopomofoService.sendDownUpKeyEvents(KEYCODE_ENTER)
                return
            }

            when (val imeAction = (this.imeOptions and EditorInfo.IME_MASK_ACTION)) {
                EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND -> {
                    // The current EditText has a specified android:imeOptions attribute.
                    this@GuilelessBopomofoService.currentInputConnection.performEditorAction(
                        imeAction
                    )
                }

                else -> {
                    // The current EditText has no android:imeOptions attribute, or I don't want to make it act as is.
                    this@GuilelessBopomofoService.sendDownUpKeyEvents(KEYCODE_ENTER)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDirectionKeyDown(event: Events.DirectionKeyDown) {
        if (ChewingBridge.chewing.bufferLen() > 0) {
            viewBinding.textViewPreEditBuffer.cursorMovedBy(PreEditBufferTextView.CursorMovedFrom.PHYSICAL_KEYBOARD)
        } else {
            if (ChewingUtil.candidateWindowClosed()) {
                when (event.direction) {
                    DirectionKey.LEFT -> {
                        sendDownUpKeyEvents(KEYCODE_DPAD_LEFT)
                    }

                    DirectionKey.RIGHT -> {
                        sendDownUpKeyEvents(KEYCODE_DPAD_RIGHT)
                    }
                }
            }
        }

        // toggle to next page of candidates
        viewBinding.keyboardPanel.apply {
            if (this.currentLayout == Layout.CANDIDATES && ChewingUtil.candidateWindowOpened()) {
                this.renderCandidatesLayout()
            }
        }
    }

    private fun setupChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        if (!checkChewingData(dataPath)) {
            Log.d(logTag, "Install Chewing data files.")
            installChewingData(dataPath)
        }

        // Save app version
        val appVersion = BuildConfig.VERSION_NAME.toByteArray()

        val chewingDataAppVersionTxt = File(String.format("%s/%s", chewingDataDir.absolutePath, "data_appversion.txt"))

        // update Chewing data files by version
        if (!chewingDataAppVersionTxt.exists()) {
            chewingDataAppVersionTxt.appendBytes(appVersion)
        }

        if (!chewingDataAppVersionTxt.readBytes().contentEquals(appVersion)) {
            Log.d(logTag, "Here comes a new version.")
            installChewingData(dataPath)

            // refresh app version
            val chewingDataAppVersionTxtOutputStream = FileOutputStream(chewingDataAppVersionTxt)
            chewingDataAppVersionTxtOutputStream.write(appVersion)
            chewingDataAppVersionTxtOutputStream.close()
        }
    }

    private fun installChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        // Copying data files
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d(logTag, "Copying ${file}...")
            val dataInputStream = assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e(logTag, it)
                }
            } finally {
                Log.d(logTag, "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }
    }

    private fun checkChewingData(dataPath: String): Boolean {
        Log.d(logTag, "Checking Chewing data files...")
        val chewingDataDir = File(dataPath)

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(logTag, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        assureViewBindingInitialized()

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
        if (!this@GuilelessBopomofoService::viewBinding.isInitialized) {
            Log.d(logTag, "initialize viewBinding")
            setInputView(onCreateInputView())
        }
    }

    // triggered if any sharedPreference has been changed
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            USER_KEYBOARD_LAYOUT,
            USER_DISPLAY_HSU_QWERTY_LAYOUT,
            USER_DISPLAY_ETEN26_QWERTY_LAYOUT,
            USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT,
                -> {
                // just 'reload' the main layout
                if (this@GuilelessBopomofoService::viewBinding.isInitialized) {
                    viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
                }
            }

            USER_ENABLE_SPACE_AS_SELECTION -> {
                ChewingBridge.chewing.setSpaceAsSelection(0)
                sharedPreferences?.apply {
                    if (this.getBoolean(key, true)) {
                        ChewingBridge.chewing.setSpaceAsSelection(1)
                    }
                }
            }

            USER_PHRASE_CHOICE_REARWARD -> {
                ChewingBridge.chewing.setPhraseChoiceRearward(0)
                sharedPreferences?.apply {
                    if (this.getBoolean(key, false)) {
                        ChewingBridge.chewing.setPhraseChoiceRearward(1)
                    }
                }
            }

            USER_HAPTIC_FEEDBACK_STRENGTH -> {
                // reload the value
                sharedPreferences?.apply {
                    userHapticFeedbackStrength = this.getInt(
                        key, defaultHapticFeedbackStrength
                    )
                }
            }

            SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS -> {
                // do nothing
            }

            USER_FULLSCREEN_WHEN_IN_LANDSCAPE,
            USER_FULLSCREEN_WHEN_IN_PORTRAIT,
                -> {
                // do nothing (onEvaluateFullscreenMode() will handle it well)
            }

            USER_KEY_BUTTON_HEIGHT,
            USER_ENABLE_IME_SWITCH,
            USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH,
                -> {
                // just 'reload' the main layout
                if (this@GuilelessBopomofoService::viewBinding.isInitialized) {
                    viewBinding.keyboardPanel.switchToLayout(Layout.MAIN)
                }
            }

            USER_CANDIDATE_SELECTION_KEYS_OPTION -> {
                sharedPreferences?.apply {
                    this.getString(
                        key, SelectionKeys.NUMBER_ROW.set
                    )?.let {
                        ChewingBridge.chewing.setSelKey(
                            SelectionKeys.valueOf(it).keys, 10
                        )
                    }
                }
            }

            USER_CONVERSION_ENGINE -> {
                sharedPreferences?.apply {
                    this.getInt(
                        key, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode
                    ).let {
                        ChewingBridge.chewing.configSetInt("chewing.conversion_engine", it)
                    }
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}