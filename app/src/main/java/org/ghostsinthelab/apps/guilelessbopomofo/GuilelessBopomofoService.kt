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
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDachenLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "GuilelessBopomofoSvc"
    lateinit var chewingEngine: ChewingEngine
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    lateinit var keyboardEt26LayoutBinding: KeyboardEt26LayoutBinding
    lateinit var keyboardDachenLayoutBinding: KeyboardDachenLayoutBinding
    lateinit var inputView: KeyboardView
    private lateinit var sharedPreferences: SharedPreferences

    init {
        System.loadLibrary("chewing")
    }

    companion object {
        const val defaultKeyboardLayout = "KB_DEFAULT"
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
            chewingEngine = ChewingEngine(dataPath)
            chewingEngine.context.let {
                Log.v(LOGTAG, "Chewing context ptr: $it")
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, R.string.libchewing_init_fail, Toast.LENGTH_LONG)
                .show()
            e.message?.let {
                Log.e(LOGTAG, it)
            }
        }
    }

    override fun onCreateCandidatesView(): View? {
        // I want to implement my own candidate selection UI
        Log.v(LOGTAG, "onCreateCandidatesView()")
        return null
    }

    // Disable fullscreen mode when device's orientation is landscape
    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        if (sharedPreferences.getBoolean("user_fullscreen_when_in_landscape", true) && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true
        }
        return false
    }

    override fun onCreateInputView(): View {
        Log.v(LOGTAG, "onCreateInputView()")
        viewBinding = KeyboardLayoutBinding.inflate(layoutInflater)

        viewBinding.apply {
            keyboardView.setServiceContext(this@GuilelessBopomofoService)
            keyboardPanel.setServiceContext(this@GuilelessBopomofoService)
            keyboardView.setOnClickPreEditCharListener(this@GuilelessBopomofoService)
        }

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        setMainLayout()
        viewBinding.keyboardPanel.currentKeyboardLayout = KeyboardPanel.KeyboardLayout.MAIN

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
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.v(LOGTAG, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(LOGTAG, "onDestroy()")
        chewingEngine.delete()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.v(LOGTAG, "onKeyDown()")
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (viewBinding.keyboardPanel.currentKeyboardLayout != KeyboardPanel.KeyboardLayout.MAIN) {
                    // close if any been opened candidate window first
                    chewingEngine.candClose()
                    viewBinding.keyboardPanel.switchToMainLayout(this)
                    // intercept general back key action
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(v: View?) {
        v?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        Log.v(LOGTAG, "onClick")

        if (v is BehaveLikeKey<*>) {
            if (v.isCharacterKey()) {
                handleCharacterKey(v)
            }
            if (v.isControlKey()) {
                handleControlKey(v)
            }
        }

        inputView.syncPreEditBuffers()
    }

    fun setMainLayout() {
        Log.v(LOGTAG, "setMainLayout()")
        viewBinding.keyboardPanel.removeAllViews()

        val keyboardSetup: (Keyboard) -> Unit = { keyboard ->
            keyboard.setupImeSwitch(this)
            keyboard.setupPuncSwitch(this)
            keyboard.setupSymbolSwitch(this)
            keyboard.setupBackspace(this)
        }

        when (getUserKeyboardLayoutPreference()) {
            "KB_HSU" -> {
                val newKeyboardType = chewingEngine.convKBStr2Num("KB_HSU")
                chewingEngine.setKBType(newKeyboardType)
                keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(layoutInflater)
                viewBinding.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
                keyboardSetup(keyboardHsuLayoutBinding.root)
            }
            "KB_ET26" -> {
                val newKeyboardType = chewingEngine.convKBStr2Num("KB_ET26")
                chewingEngine.setKBType(newKeyboardType)
                keyboardEt26LayoutBinding = KeyboardEt26LayoutBinding.inflate(layoutInflater)
                viewBinding.keyboardPanel.addView(keyboardEt26LayoutBinding.root)
                keyboardSetup(keyboardEt26LayoutBinding.root)
            }
            "KB_DEFAULT" -> {
                val newKeyboardType = chewingEngine.convKBStr2Num("KB_DEFAULT")
                chewingEngine.setKBType(newKeyboardType)
                keyboardDachenLayoutBinding = KeyboardDachenLayoutBinding.inflate(layoutInflater)
                viewBinding.keyboardPanel.addView(keyboardDachenLayoutBinding.root)
                keyboardSetup(keyboardDachenLayoutBinding.root)
            }
        }
    }

    private fun getUserKeyboardLayoutPreference(): String? {
        return sharedPreferences.getString("user_keyboard_layout", defaultKeyboardLayout)
    }

    private fun handleCharacterKey(v: BehaveLikeKey<*>) {
        v.keySymbol?.let {
            chewingEngine.handleDefault(it.get(0))
        }
    }

    private fun handleControlKey(v: BehaveLikeKey<*>) {
        val ic = currentInputConnection
        when (v.keyCode()) {
            KeyEvent.KEYCODE_SPACE -> {
                chewingEngine.handleSpace()
            }
            KeyEvent.KEYCODE_ENTER -> {
                if (chewingEngine.commitPreeditBuf() == 0) { // not committed yet
                    ic.commitText(chewingEngine.commitStringStatic(), 1)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
            // 在大千鍵盤下，標準的逗號鍵會對映到「ㄝ」，這裡的逗號鍵要另外當成特別的「常用符號」功能鍵，
            // 短觸會輸出全形逗號，長按交給 setupPuncSwitch() 處理
            KeyEvent.KEYCODE_COMMA -> {
                // simulates [Shift] + [,]
                chewingEngine.apply {
                    setEasySymbolInput(1)
                    handleDefault(',')
                    setEasySymbolInput(0)
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

        // Copying data files
        val chewingDataFiles =
            listOf("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
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
    }
}