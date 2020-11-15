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
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
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
    }

    override fun onCreateCandidatesView(): View {
        val myCandidatesView: View = layoutInflater.inflate(R.layout.candidates_layout, null)
        return myCandidatesView
    }

    override fun onCreateInputView(): View {
        Log.d(LOGTAG, "onCreateInputView()")
        val myKeyboardView: View = layoutInflater.inflate(R.layout.keyboard_layout, null)
        val imeSwitchButton: ImageButton = myKeyboardView.findViewById(R.id.imageImeSwitchButton)
        imeSwitchButton.setOnClickListener(this)
        imeSwitchButton.setOnLongClickListener(showImePicker())
        return myKeyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        chewingEngine.delete()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {
        val ic = currentInputConnection
        when(v?.id) {
            R.id.imageImeSwitchButton ->
                switchToNextInputMethod(false)
        }
    }

    private fun showImePicker() = View.OnLongClickListener {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        return@OnLongClickListener true
    }
}