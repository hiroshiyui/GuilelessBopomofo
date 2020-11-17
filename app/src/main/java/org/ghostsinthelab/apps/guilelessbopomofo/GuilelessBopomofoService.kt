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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "Service"
    lateinit var chewingEngine: ChewingEngine

    init {
        System.loadLibrary("chewing")
    }

    override fun onCreate() {
        super.onCreate()
        val dataPath = packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
        chewingEngine = ChewingEngine(dataPath)
        chewingEngine.context.let {
            Log.d(LOGTAG, "Chewing context ptr: ${it.toString()}")
        }
        val newKeyboardType = chewingEngine.convKBStr2Num("KB_HSU")
        chewingEngine.setKBType(newKeyboardType)
    }

    override fun onCreateCandidatesView(): View {
        val myCandidatesView: View = layoutInflater.inflate(R.layout.candidates_layout, null)
        return myCandidatesView
    }

    override fun onCreateInputView(): View {
        Log.d(LOGTAG, "onCreateInputView()")
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
        Log.d(LOGTAG, "onStartInputView()")
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(LOGTAG, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOGTAG, "onDestroy()")
        chewingEngine.delete()
    }

    override fun onClick(v: View?) {
        val ic = currentInputConnection
        Log.d(LOGTAG, "onClick")
        when(v?.id) {
            R.id.imageKeyboardButton -> {
                chewingEngine.commitPreeditBuf()
                ic.commitText(chewingEngine.commitString(), 1)
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
        switchToNextInputMethod(false)
    }

    private fun showImePicker() = View.OnLongClickListener {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        return@OnLongClickListener true
    }

    private fun syncPreEditString() {
        val preEditTextView: TextView = this.window.findViewById(R.id.preEditTextView)
        val preEditBuffer: String = chewingEngine.bufferString()
        val bopomofoBuffer: String = chewingEngine.bopomofoStringStatic()
        preEditTextView.text = "${preEditBuffer}${bopomofoBuffer}"
    }
}