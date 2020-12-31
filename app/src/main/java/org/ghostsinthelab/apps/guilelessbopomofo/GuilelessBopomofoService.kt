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

import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.ghostsinthelab.apps.guilelessbopomofo.keys.BehaveLikeKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ShiftKeyImageButton
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import kotlin.properties.Delegates


class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "GuilelessBopomofoSvc"
    var userHapticFeedbackStrength: Int = HapticFeedbackConstants.KEYBOARD_TAP
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var inputView: KeyboardView
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
            ChewingEngine.start(dataPath)
            ChewingEngine.context.let {
                Log.v(LOGTAG, "Chewing context ptr: $it")
            }

            if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                ChewingEngine.setSpaceAsSelection(1)
            }

            if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                ChewingEngine.setPhraseChoiceRearward(true)
            }

            ChewingEngine.setChiEngMode(CHINESE_MODE)
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
        Log.v(LOGTAG, "onEvaluateInputViewShown()")
        return super.onEvaluateInputViewShown()
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
        ChewingEngine.delete()
    }

    override fun onClick(v: View?) {
        v?.performHapticFeedback(userHapticFeedbackStrength)
        Log.v(LOGTAG, "onClick")

        if (v is BehaveLikeKey<*>) {
            if (v.isCharacterKey()) {
                handleCharacterKey(v)
            }
            if (v.isControlKey()) {
                handleControlKey(v)
            }
        }

        EventBus.getDefault().post(BufferUpdatedEvent())
    }

    fun doneCandidateChoice() {
        viewBinding.apply {
            keyboardPanel.currentCandidatesList = 0
            keyboardPanel.switchToMainLayout()
        }
        EventBus.getDefault().post(BufferUpdatedEvent())
    }

    private fun handleCharacterKey(v: BehaveLikeKey<*>) {
        var sendCharacter by Delegates.notNull<Char>()

        v.keySymbol?.let {
            sendCharacter = it.get(0)
        }

        val shiftKeyImageButton =
            viewBinding.keyboardPanel.findViewById<ShiftKeyImageButton>(R.id.keyImageButtonShift)
        shiftKeyImageButton?.let {
            if (shiftKeyImageButton.isActive) {
                Log.v(LOGTAG, "Shift is active")
                if (v.keyShiftSymbol?.isNotEmpty() == true) {
                    sendCharacter = v.keyShiftSymbol.toString().get(0)
                } else {
                    sendCharacter = v.keySymbol.toString().get(0).toUpperCase()
                }
            }
        }

        ChewingEngine.handleDefault(sendCharacter)

        shiftKeyImageButton?.let {
            if (shiftKeyImageButton.isLocked == false) {
                Log.v(LOGTAG, "Release shift key")
                shiftKeyImageButton.switchToState(ShiftKeyImageButton.ShiftKeyState.RELEASED)
            }
        }
    }

    private fun handleControlKey(v: BehaveLikeKey<*>) {
        when (v.keyCode()) {
            KeyEvent.KEYCODE_SPACE -> {
                if (ChewingEngine.anyPreeditBufferIsNotEmpty()) {
                    ChewingEngine.handleSpace()
                    // 空白鍵是否為選字鍵？
                    if (ChewingEngine.getSpaceAsSelection() == 1 && ChewingEngine.candTotalChoice() > 0) {
                        viewBinding.keyboardPanel.switchToCandidatesLayout()
                    }
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE)
                }
            }
            KeyEvent.KEYCODE_ENTER -> {
                if (ChewingEngine.anyPreeditBufferIsNotEmpty()) { // not committed yet
                    ChewingEngine.handleEnter()
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
            else -> {
                Log.v(LOGTAG, "This key has not been implemented its handler")
            }
        }
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