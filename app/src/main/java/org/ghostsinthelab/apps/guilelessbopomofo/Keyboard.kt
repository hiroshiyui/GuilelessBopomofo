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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding
import kotlin.concurrent.fixedRateTimer

class Keyboard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var backspacePressed: Boolean = false
    private val LOGTAG: String = "Keyboard"
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding

    init {
        this.orientation = VERTICAL
        this.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setupImeSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupImeSwitch")
        v = imeService.viewBinding
        val keyImageButtonImeSwitch =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonImeSwitch)

        keyImageButtonImeSwitch.setImeSwitchButtonOnClickListener(imeService)
        keyImageButtonImeSwitch.setImeSwitchButtonOnLongClickListener(imeService)
    }

    fun setupPuncSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupPuncSwitch")
        v = imeService.viewBinding
        val keyImageButtonPunc =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonPunc)
        keyImageButtonPunc.setKeyImageButtonPuncOnLongClickListener(imeService)
    }

    fun setupSymbolSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupSymbolSwitch")
        v = imeService.viewBinding
        val keyImageButtonSymbol =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonSymbol)
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)

        keyImageButtonSymbol.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            imeService.chewingEngine.handleDefault('`')
            imeService.chewingEngine.candOpen()

            v.keyboardPanel.apply {
                removeAllViews()
                addView(symbolsPickerLayoutBinding.root)
                currentKeyboardLayout = KeyboardPanel.KeyboardLayout.SYMBOLS
            }

            val totalCategories = imeService.chewingEngine.candTotalChoice()

            repeat(totalCategories) { category ->
                val button: Button = Button(context)
                button.text = imeService.chewingEngine.candStringByIndexStatic(category)
                button.id = View.generateViewId()

                button.setOnClickListener {
                    imeService.chewingEngine.candChooseByIndex(category)

                    if (imeService.chewingEngine.candStringByIndexStatic(0).isEmpty()) {
                        imeService.chewingEngine.candClose()
                        imeService.chewingEngine.handleEnd()
                        imeService.viewBinding.keyboardView.updateBuffers(imeService)
                        imeService.viewBinding.keyboardPanel.switchToMainLayout(imeService)
                    } else { // 如果候選區還有資料，代表目前進入次分類
                        imeService.viewBinding.keyboardPanel.switchToCandidatesLayout(imeService)
                    }

                    imeService.viewBinding.keyboardPanel.currentCandidatesList = 0
                }

                symbolsPickerLayoutBinding.SymbolsConstraintLayout.addView(button)
                symbolsPickerLayoutBinding.SymbolsFlow.addView(button)
            }
        }

        val keyButtonBackToMain = symbolsPickerLayoutBinding.keyButtonBackToMain
        keyButtonBackToMain.setBackMainLayoutOnClickListener(imeService)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupBackspace(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupBackspace")
        v = imeService.viewBinding
        val keyImageButtonBackspace =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonBackspace)

        keyImageButtonBackspace.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    if (imeService.chewingEngine.bufferStringStatic()
                            .isNotEmpty() || imeService.chewingEngine.bopomofoStringStatic()
                            .isNotEmpty()
                    ) {
                        imeService.chewingEngine.handleBackspace()
                        imeService.viewBinding.keyboardView.updateBuffers(imeService)
                    } else {
                        // acts as general and repeatable backspace key
                        runBlocking {
                            launch {
                                backspacePressed = true
                                repeatBackspace(imeService)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    backspacePressed = false
                }
            }
            return@setOnTouchListener true
        }
    }

    private suspend fun repeatBackspace(imeService: GuilelessBopomofoService) {
        fixedRateTimer("repeatBackspace", true, 0L, 200L) {
            if (backspacePressed) {
                imeService.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            } else {
                this.cancel()
            }
        }
        delay(50L)
    }
}