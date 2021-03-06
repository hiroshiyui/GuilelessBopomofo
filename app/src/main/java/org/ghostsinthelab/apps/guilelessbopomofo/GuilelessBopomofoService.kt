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
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.keys.*
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService() {
    val LOGTAG = "GuilelessBopomofoSvc"
    var userHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength.toInt()
    var physicalKeyboardPresent: Boolean = false
    var physicalKeyboardEnabled: Boolean = false
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var inputView: Keyboard
    private val chewingDataFiles =
        listOf("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")

    companion object {
        val defaultHapticFeedbackStrength: Int =
            Vibratable.VibrationStrength.NORMAL.strength.toInt()
        const val defaultKeyboardLayout: String = "KB_DEFAULT"
    }

    override fun onCreate() {
        super.onCreate()

        val emojiCompatConfig = BundledEmojiCompatConfig(this)
        EmojiCompat.init(emojiCompatConfig)

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

            sharedPreferences.getString("user_candidate_selection_keys_option", "NUMBER_ROW")?.let {
                ChewingBridge.setSelKey(ChewingUtil.SelectionKeysOption.valueOf(it).keys, 10)
            }
        } catch (exception: Exception) {
            val exceptionDescription: String =
                getString(R.string.libchewing_init_fail, exception.message)
            Toast.makeText(applicationContext, exceptionDescription, Toast.LENGTH_LONG)
                .show()
            exception.let {
                it.printStackTrace()
                it.message?.let {
                    Log.e(LOGTAG, it)
                }
            }
        }

        userHapticFeedbackStrength =
            sharedPreferences.getInt("user_haptic_feedback_strength", defaultHapticFeedbackStrength)

        GuilelessBopomofoServiceContext.bindServiceInstance(this)
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
        GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.updateBuffers()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.v(LOGTAG, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(LOGTAG, "onDestroy()")
        ChewingBridge.delete()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            if (it.isPrintingKey) {
                onPrintingKeyDown(it)
            } else {
                when (it.keyCode) {
                    KEYCODE_BACK -> {
                        // keep default behavior of Back key
                        return super.onKeyDown(keyCode, event)
                    }
                    KEYCODE_ALT_LEFT, KEYCODE_SHIFT_RIGHT -> {
                        // for onKeyLongPress()...
                        it.startTracking()
                        return true
                    }
                    KEYCODE_SPACE -> {
                        SpaceKey.action(it)
                    }
                    KEYCODE_DEL -> {
                        BackspaceKey.action()
                    }
                    KEYCODE_ENTER -> {
                        EnterKey.action()
                    }
                    KEYCODE_ESCAPE -> {
                        EscapeKey.action()
                    }
                    KEYCODE_DPAD_LEFT -> {
                        LeftKey.action()
                    }
                    KEYCODE_DPAD_RIGHT -> {
                        RightKey.action()
                    }
                    KEYCODE_DPAD_DOWN -> {
                        DownKey.action()
                    }
                    else -> {
                        return super.onKeyDown(keyCode, event)
                    }
                }
            }
        }

        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            if (it.isPrintingKey) {
                onPrintingKeyUp(it)
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            when (it.keyCode) {
                KEYCODE_SHIFT_RIGHT -> {
                    ChewingUtil.openPuncCandidates()
                    GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToCandidatesLayout()
                }
                KEYCODE_ALT_LEFT -> {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                }
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    fun onPrintingKeyDown(event: KeyEvent) {
        if (event.keyCode == KEYCODE_GRAVE && ChewingBridge.getChiEngMode() == CHINESE_MODE && !event.isShiftPressed) {
            viewBinding.keyboardPanel.switchToSymbolPicker()
            return
        }

        var keyPressed: Char = event.unicodeChar.toChar()

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
                keyPressed = event.getUnicodeChar(META_SHIFT_ON).toChar()
            }
        }

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
        } else {
            ChewingBridge.handleDefault(keyPressed)
        }

        GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.updateBuffers()

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

    private fun onPrintingKeyUp(event: KeyEvent) {
        // Detect if a candidate had been chosen by user
        val keyboardPanel =
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel
        if (keyboardPanel.currentKeyboardLayout == KeyboardPanel.KeyboardLayout.CANDIDATES) {
            if (ChewingUtil.candWindowClosed()) {
                keyboardPanel.candidateSelectionDone()
            } else {
                keyboardPanel.renderCandidatesLayout()
            }
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