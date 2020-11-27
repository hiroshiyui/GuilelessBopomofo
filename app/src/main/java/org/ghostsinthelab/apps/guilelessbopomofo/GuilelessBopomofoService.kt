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
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.PunctuationPopupLayoutBinding
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "Service"
    lateinit var chewingEngine: ChewingEngine
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var punctuationPopupLayoutBinding: PunctuationPopupLayoutBinding

    init {
        System.loadLibrary("chewing")
    }

    override fun onCreate() {
        super.onCreate()
        // Initializing Chewing
        try {
            val dataPath =
                packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
            setupChewingData(dataPath)
            chewingEngine = ChewingEngine(dataPath)
            chewingEngine.context.let {
                Log.v(LOGTAG, "Chewing context ptr: ${it.toString()}")
            }
            val newKeyboardType = chewingEngine.convKBStr2Num("KB_HSU")
            chewingEngine.setKBType(newKeyboardType)
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
        return null
    }

    override fun onCreateInputView(): View {
        Log.v(LOGTAG, "onCreateInputView()")
        viewBinding = KeyboardLayoutBinding.inflate(layoutInflater)
        val myKeyboardView = viewBinding.root

        // set IME switch/picker
        val keyImageButtonImeSwitch = viewBinding.keyImageButtonImeSwitch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            keyImageButtonImeSwitch.setOnClickListener(switchToNextIME())
        }
        keyImageButtonImeSwitch.setOnLongClickListener(showImePicker())

        // set punctuation picker popup
        punctuationPopupLayoutBinding = PunctuationPopupLayoutBinding.inflate(layoutInflater)
        val punctuationPopupView = punctuationPopupLayoutBinding.root
        val punctuationPopup = PopupWindow(punctuationPopupView)
        viewBinding.keyImageButtonPunc.setOnLongClickListener(showPunctuationPopup(punctuationPopup))

        return myKeyboardView
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

        syncPreEditString()
    }


    private fun handleCharacterKey(v: BehaveLikeKey<*>) {
        v.keySymbol?.let {
            chewingEngine.handleDefault(it)
        }
    }

    private fun handleControlKey(v: BehaveLikeKey<*>) {
        val ic = currentInputConnection
        v.isControlKey().let {
            when (v.keyCode()) {
                KeyEvent.KEYCODE_SPACE -> {
                    chewingEngine.handleSpace()
                }
                KeyEvent.KEYCODE_DEL -> {
                    if (chewingEngine.bufferStringStatic()
                            .isNotEmpty() || chewingEngine.bopomofoStringStatic().isNotEmpty()
                    ) {
                        chewingEngine.handleBackspace()
                    } else {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                    }
                }
                KeyEvent.KEYCODE_ENTER -> {
                    val committed: Int = chewingEngine.commitPreeditBuf()
                    if (committed == 0) { // not committed yet
                        ic.commitText(chewingEngine.commitStringStatic(), 1)
                    } else {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                    }
                }
                else -> {
                    Log.v(LOGTAG, "This key has not been implemented its handler")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun switchToNextIME() = View.OnClickListener {
        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        switchToNextInputMethod(false)
    }

    private fun showImePicker() = View.OnLongClickListener {
        it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        return@OnLongClickListener true
    }

    private fun showPunctuationPopup(punctuationPopup: PopupWindow) = View.OnLongClickListener {
        if (punctuationPopup.isShowing) {
            punctuationPopup.dismiss()
        } else {
            punctuationPopup.elevation = 30.0F
            punctuationPopup.height = ViewGroup.LayoutParams.WRAP_CONTENT
            punctuationPopup.width = ViewGroup.LayoutParams.WRAP_CONTENT
            punctuationPopup.showAtLocation(
                viewBinding.LinearLayoutKeyboard,
                Gravity.TOP,
                0,
                0
            )
        }

        Log.v(LOGTAG, "showPunctuationPopup()")
        return@OnLongClickListener true
    }

    private fun syncPreEditString() {
        viewBinding.TextViewPreEditBuffer.text = chewingEngine.bufferStringStatic()
        viewBinding.TextViewBopomofoBuffer.text = chewingEngine.bopomofoStringStatic()
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