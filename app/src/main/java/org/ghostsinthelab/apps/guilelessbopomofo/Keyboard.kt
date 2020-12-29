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
import android.widget.LinearLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyImageButton
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

    fun setupModeSwitch() {
        Log.v(LOGTAG, "setupModeSwitch")
        v = GuilelessBopomofoServiceContext.serviceInstance.viewBinding
        val keyImageButtonModeSwitch =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonModeSwitch)

        keyImageButtonModeSwitch.setModeSwitchButtonOnClickListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupBackspace() {
        Log.v(LOGTAG, "setupBackspace")
        v = GuilelessBopomofoServiceContext.serviceInstance.viewBinding
        val keyImageButtonBackspace =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonBackspace)

        keyImageButtonBackspace.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    if (ChewingEngine.anyPreeditBufferIsNotEmpty()) {
                        ChewingEngine.handleBackspace()
                        GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardView.updateBuffers()
                    } else {
                        // acts as general and repeatable backspace key
                        runBlocking {
                            launch {
                                backspacePressed = true
                                repeatBackspace()
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

    private suspend fun repeatBackspace() {
        fixedRateTimer("repeatBackspace", true, 0L, 200L) {
            if (backspacePressed) {
                GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            } else {
                this.cancel()
            }
        }
        delay(50L)
    }
}