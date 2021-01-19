/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.events.*
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ShiftKey
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService() {
    val LOGTAG = "GuilelessBopomofoSvc"
    var userHapticFeedbackStrength: Int = HapticFeedbackConstants.KEYBOARD_TAP
    var physicalKeyboardPresent: Boolean = false
    var physicalKeyboardEnabled: Boolean = false
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var inputView: Keyboard
    private val chewingDataFiles =
        listOf("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")

    companion object {
        const val defaultHapticFeedbackStrength: Int = HapticFeedbackConstants.KEYBOARD_TAP
        const val defaultKeyboardLayout: String = "KB_DEFAULT"
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)
        Log.v(LOGTAG, "onCreate()")
        // Initializing Chewing
        try {
            val dataPath =
                packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
            setupChewingData(dataPath)
            ChewingBridge.connect(dataPath)
            ChewingBridge.context.let {
                Log.v(LOGTAG, "Chewing context ptr: $it")
            }

            if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                ChewingBridge.setSpaceAsSelection(1)
            }

            if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                ChewingBridge.setPhraseChoiceRearward(true)
            }

            ChewingBridge.setChiEngMode(CHINESE_MODE)
            ChewingBridge.setCandPerPage(10)

            val selKeys: IntArray = ChewingUtil.SelectionKeysOption.NUMBER_ROW.keys
            ChewingBridge.setSelKey(selKeys, 10)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, R.string.libchewing_init_fail, Toast.LENGTH_LONG)
                .show()
            e.message?.let {
                Log.e(LOGTAG, it)
            }
        }

        userHapticFeedbackStrength =
            sharedPreferences.getInt("user_haptic_feedback_strength", defaultHapticFeedbackStrength)

        GuilelessBopomofoServiceContext.bindGuilelessBopomofoService(this)
        EventBus.getDefault().register(this)
    }

    override fun onCreateCandidatesView(): View? {
        // I want to implement my own candidate selection UI
        Log.v(LOGTAG, "onCreateCandidatesView()")
        return null
    }

    // Disable fullscreen mode when device's orientation is landscape
    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()

        if (sharedPreferences.getBoolean(
                "user_fullscreen_when_in_landscape",
                true
            ) && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            return true
        }

        if (sharedPreferences.getBoolean(
                "user_fullscreen_when_in_portrait",
                false
            ) && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            return true
        }

        return false
    }

    override fun onCreateInputView(): View {
        Log.v(LOGTAG, "onCreateInputView()")
        viewBinding = KeyboardLayoutBinding.inflate(layoutInflater)
        viewBinding.keyboardPanel.switchToMainLayout()

        inputView = viewBinding.root
        return inputView
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        Log.v(LOGTAG, "onInitializeInterface()")
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        Log.v(LOGTAG, "onEvaluateInputViewShown()")
        physicalKeyboardPresent =
            (resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY)
        physicalKeyboardEnabled = physicalKeyboardEnabled()
        return true
    }

    override fun onBindInput() {
        super.onBindInput()
        Log.v(LOGTAG, "onBindInput()")
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.v(LOGTAG, "onStartInput()")
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.v(LOGTAG, "onStartInputView()")
        EventBus.getDefault().post(BufferUpdatedEvent())
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.v(LOGTAG, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(LOGTAG, "onDestroy()")
        ChewingBridge.delete()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            Log.v(LOGTAG, "${it}")
            // keep default behavior of Back key
            if (it.keyCode == KEYCODE_BACK) {
                return super.onKeyDown(keyCode, event)
            }
            // for onKeyLongPress()...
            it.startTracking()

            if (it.isPrintingKey) {
                EventBus.getDefault().post(PrintingKeyDownEvent(it))
            } else {
                EventBus.getDefault().post(NotPrintingKeyDownEvent(it))
            }
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            if (it.isPrintingKey) {
                EventBus.getDefault().post(PrintingKeyUpEvent(it))
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            Log.v(LOGTAG, "${it}")
            when (it.keyCode) {
                KEYCODE_SHIFT_RIGHT -> {
                    ChewingUtil.openPuncCandidates()
                    EventBus.getDefault().post(CandidatesWindowOpendEvent())
                }
                KEYCODE_ALT_LEFT -> {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                }
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPrintingKeyDown(event: PrintingKeyDownEvent) {
        // don't repeat character keys if they are long-pressed by user,
        // let onKeyLongPress() do it own responsibility.
        if (event.keyEvent.repeatCount > 0) {
            return
        }

        if (event.keyEvent.keyCode == KEYCODE_GRAVE && ChewingBridge.getChiEngMode() == CHINESE_MODE && !event.keyEvent.isShiftPressed) {
            EventBus.getDefault().post(SymbolPickerOpenedEvent())
            return
        }

        var keyPressed: Char = event.keyEvent.unicodeChar.toChar()

        val shiftKeyImageButton =
            viewBinding.keyboardPanel.findViewById<ShiftKey>(
                R.id.keyImageButtonShift
            )

        shiftKeyImageButton?.let {
            if (shiftKeyImageButton.isActive) {
                Log.v(LOGTAG, "Shift is active")
                currentInputConnection.sendKeyEvent(
                    KeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT)
                )
                keyPressed = event.keyEvent.getUnicodeChar(META_SHIFT_ON).toChar()
            }
        }

        ChewingBridge.handleDefault(keyPressed)
        EventBus.getDefault().post(BufferUpdatedEvent())

        shiftKeyImageButton?.let {
            if (shiftKeyImageButton.isActive && !shiftKeyImageButton.isLocked) {
                Log.v(LOGTAG, "Release shift key")
                shiftKeyImageButton.switchToState(ShiftKey.ShiftKeyState.RELEASED)
                currentInputConnection.sendKeyEvent(
                    KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT)
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIsNotPrintingKeyDown(event: NotPrintingKeyDownEvent) {
        Log.v(LOGTAG, keyCodeToString(event.keyEvent.keyCode))
        when (event.keyEvent.keyCode) {
            KEYCODE_SPACE -> {
                EventBus.getDefault().post(SpaceKeyDownEvent.Physical(event.keyEvent))
            }
            KEYCODE_DEL -> {
                EventBus.getDefault().post(BackspaceKeyDownEvent())
            }
            KEYCODE_ENTER -> {
                EventBus.getDefault().post(EnterKeyDownEvent())
            }
            KEYCODE_ESCAPE -> {
                EventBus.getDefault().post(EscKeyDownEvent())
            }
            KEYCODE_DPAD_LEFT -> {
                EventBus.getDefault().post(LeftKeyDownEvent())
            }
            KEYCODE_DPAD_RIGHT -> {
                EventBus.getDefault().post(RightKeyDownEvent())
            }
            KEYCODE_DPAD_DOWN -> {
                EventBus.getDefault().post(DownKeyDownEvent())
            }
            else -> {
                // passthru other keyevents, or we will make some physical keys like volume keys invalid
                currentInputConnection.sendKeyEvent(event.keyEvent)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBufferUpdatedEvent(event: BufferUpdatedEvent) {
        // chewingEngine.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingBridge.commitCheck() == 1) {
            currentInputConnection.commitText(ChewingBridge.commitString(), 1)
            // dirty hack (?) - 讓 chewingEngine.commitCheck() 歸 0
            // 研究 chewing_commit_Check() 之後想到的，並不是亂碰運氣
            ChewingBridge.handleEnd()
        }
    }

    private fun physicalKeyboardEnabled(): Boolean {
        if (physicalKeyboardPresent
            && sharedPreferences.getBoolean(
                "user_enable_physical_keyboard",
                false
            )
        ) {
            return true
        }
        return false
    }

    private fun setupChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        if (!checkChewingData(dataPath)) {
            Log.v(LOGTAG, "Install Chewing data files.")
            installChewingData(dataPath)
        }

        // Save app version
        val chewingAppVersion =
            packageManager.getPackageInfo(this.packageName, 0).versionName.toByteArray()
        val chewingDataAppVersionTxt =
            File(String.format("%s/%s", chewingDataDir.absolutePath, "data_appversion.txt"))

        // update Chewing data files by version
        if (!chewingDataAppVersionTxt.exists()) {
            chewingDataAppVersionTxt.appendBytes(chewingAppVersion)
        }

        if (!chewingDataAppVersionTxt.readBytes().contentEquals(chewingAppVersion)) {
            Log.v(LOGTAG, "Here comes a new version.")
            installChewingData(dataPath)

            // refresh app version
            val chewingDataAppVersionTxtOutputStream = FileOutputStream(chewingDataAppVersionTxt)
            chewingDataAppVersionTxtOutputStream.write(chewingAppVersion)
            chewingDataAppVersionTxtOutputStream.close()
        }
    }

    private fun installChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        // Copying data files
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.v(LOGTAG, "Copying ${file}...")
            val dataInputStream = assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e(LOGTAG, it)
                }
            } finally {
                Log.v(LOGTAG, "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }
    }

    private fun checkChewingData(dataPath: String): Boolean {
        Log.v(LOGTAG, "Checking Chewing data files...")
        val chewingDataDir = File(dataPath)

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }
}