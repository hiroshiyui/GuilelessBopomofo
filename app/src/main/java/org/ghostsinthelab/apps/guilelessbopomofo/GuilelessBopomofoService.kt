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
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "Service"
    lateinit var chewingEngine: ChewingEngine

    init {
        System.loadLibrary("chewing")
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val dataPath = packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
            setupChewingData(dataPath)
            chewingEngine = ChewingEngine(dataPath)
            chewingEngine.context.let {
                Log.v(LOGTAG, "Chewing context ptr: ${it.toString()}")
            }
            val newKeyboardType = chewingEngine.convKBStr2Num("KB_HSU")
            chewingEngine.setKBType(newKeyboardType)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, R.string.libchewing_init_fail, Toast.LENGTH_LONG).show()
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
        val myKeyboardView: View = layoutInflater.inflate(R.layout.keyboard_layout, null)

        // set IME switch/picker
        val imeSwitchButton: ImageButton = myKeyboardView.findViewById(R.id.imageImeSwitchButton)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            imeSwitchButton.setOnClickListener(switchToNextIME())
        }
        imeSwitchButton.setOnLongClickListener(showImePicker())

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
        val ic = currentInputConnection
        v?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        Log.v(LOGTAG, "onClick")
        if (v is BehaveLikeKey<*>) {
            v.keyCodeString?.let {
                Log.v(LOGTAG, it)
                Log.v(LOGTAG, "KEYCODE: ${v.keyCode(it)}")
            }
            if (v.isCharacterKey()) {
                Log.d(LOGTAG, "Yes, I am a character key!")
            }
            if (v.isControlKey()) {
                Log.d(LOGTAG, "Yes, I am a control key!")
            }
        }

        when (v?.id) {
            R.id.imageEnterButton -> {
                val committed: Int = chewingEngine.commitPreeditBuf()
                if (committed == 0) { // not committed yet
                    ic.commitText(chewingEngine.commitStringStatic(), 1)
                }
            }
            R.id.imageBackspaceButton -> {
                if (chewingEngine.bufferStringStatic().isNotEmpty() || chewingEngine.bopomofoStringStatic().isNotEmpty()) {
                    chewingEngine.handleBackspace()
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                }
            }
            R.id.button1 -> {
                chewingEngine.handleDefault('l')
            }
            R.id.button2 -> {
                chewingEngine.handleDefault('f')
            }
        }
        syncPreEditString()
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

    private fun syncPreEditString() {
        val preEditBufferTextView: TextView = this.window.findViewById(R.id.preEditBufferTextView)
        val bopomofoBufferTextView: TextView = this.window.findViewById(R.id.bopomofoBufferTextView)
        preEditBufferTextView.text = chewingEngine.bufferStringStatic()
        bopomofoBufferTextView.text = chewingEngine.bopomofoStringStatic()
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