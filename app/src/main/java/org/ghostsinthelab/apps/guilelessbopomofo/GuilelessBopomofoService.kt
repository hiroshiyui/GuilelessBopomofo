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

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.PunctuationPickerLayoutBinding
import java.io.File
import java.io.FileOutputStream

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "Service"
    lateinit var chewingEngine: ChewingEngine
    lateinit var viewBinding: KeyboardLayoutBinding
    lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    lateinit var punctuationPickerLayoutBinding: PunctuationPickerLayoutBinding
    lateinit var myKeyboardView: KeyboardView

    init {
        System.loadLibrary("chewing")
    }

    override fun onCreate() {
        super.onCreate()
        Log.v(LOGTAG, "onCreate()")
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
        Log.v(LOGTAG, "onCreateCandidatesView()")
        return null
    }

    override fun onCreateInputView(): View {
        Log.v(LOGTAG, "onCreateInputView()")
        viewBinding = KeyboardLayoutBinding.inflate(layoutInflater)
        myKeyboardView = viewBinding.root

        myKeyboardView.setServiceContext(this)
        viewBinding.keyboardPanel.setServiceContext(this)
        viewBinding.keyboardView.setServiceContext(this)

        keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(layoutInflater)
        keyboardHsuLayoutBinding.root.setServiceContext(this)

        // 這邊有機會可以做不同鍵盤排列的抽換… perhaps a method called setMainLayout()
        viewBinding.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
        keyboardHsuLayoutBinding.root.setupImeSwitch()

        // 這種還是做成 addView(), removeView() 處理比較好，include 然後調 visibility 太昂貴
//        setupPunctuationPickerView()

        myKeyboardView.setOnClickPreEditCharListener(this)

        return myKeyboardView
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
        keyboardHsuLayoutBinding.root.setupImeSwitch(this)
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

            if (v.id == R.id.keyImageButtonBack) {
                viewBinding.keyboardPanel.switchToMainLayout()
            }
        }

        if (v is PreEditBufferTextView) {
            Log.v(LOGTAG, "PreEditBufferTextView has been clicked")
        }




        viewBinding.root.syncPreEditString()
    }

    fun onStarClick(v: View?) {
        v?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        Log.v(LOGTAG, "onStarClick")
        viewBinding.keyboardPanel.switchToMainLayout()
    }

//    private fun setupPunctuationPickerView(): View {
//        // set punctuation picker popup
//        punctuationPickerLayoutBinding = PunctuationPickerLayoutBinding.inflate(layoutInflater)
//        val punctuationPickerView = punctuationPickerLayoutBinding.root
//        val punctuationPopup = PopupWindow(punctuationPickerView)
//        viewBinding.keyImageButtonPunc.setOnLongClickListener(showPunctuationPopup(punctuationPopup))
//        punctuationPickerLayoutBinding.keyImageButtonClose.setOnClickListener { punctuationPopup.dismiss() }
//
//        punctuationPickerLayoutBinding.let { it ->
//            listOf(
//                it.keyButtonPeriod,
//                it.keyButtonIdeographicComma,
//                it.keyButtonQuestionMark
//            ).forEach { keyButton ->
//                keyButton.setOnClickListener {
//                    commitPunctuation(punctuationPopup, keyButton)
//                }
//            }
//        }
//
//        return punctuationPickerView
//    }

    private fun handleCharacterKey(v: BehaveLikeKey<*>) {
        v.keySymbol?.let {
            chewingEngine.handleDefault(it.get(0))
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
                    if (chewingEngine.commitPreeditBuf() == 0) { // not committed yet
                        ic.commitText(chewingEngine.commitStringStatic(), 1)
                    } else {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                    }
                }
                KeyEvent.KEYCODE_PICTSYMBOLS -> {
                    viewBinding.keyboardPanel.switchToSymbolsPicker()
                }
                else -> {
                    Log.v(LOGTAG, "This key has not been implemented its handler")
                }
            }
        }
    }

    private fun showPunctuationPopup(punctuationPopup: PopupWindow) = View.OnLongClickListener {
        if (!punctuationPopup.isShowing) {
            punctuationPopup.isOutsideTouchable = true
            punctuationPopup.elevation = 30.0F
            punctuationPopup.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this,
                    android.R.drawable.screen_background_dark_transparent
                )
            )
            punctuationPopup.height = ViewGroup.LayoutParams.WRAP_CONTENT
            punctuationPopup.width = ViewGroup.LayoutParams.WRAP_CONTENT
            punctuationPopup.showAtLocation(
                viewBinding.keyboardPanel,
                (Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL),
                0,
                0
            )
        }

        Log.v(LOGTAG, "showPunctuationPopup()")
        return@OnLongClickListener true
    }

    private fun commitPunctuation(punctuationPopup: PopupWindow, v: KeyButton) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        val ic = currentInputConnection
        if (chewingEngine.commitPreeditBuf() == 0) {
            ic.commitText(chewingEngine.commitStringStatic(), 1)
        }
        ic.commitText(v.keySymbol, 1)
        myKeyboardView.syncPreEditString()
        punctuationPopup.dismiss()
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