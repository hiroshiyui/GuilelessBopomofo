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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingEngine
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import kotlin.concurrent.fixedRateTimer

@SuppressLint("ClickableViewAccessibility")
class BackspaceKeyImageButton(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    private var backspacePressed: Boolean = false

    init {
        this.setOnTouchListener { v, event ->
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
        fixedRateTimer("repeatBackspace", true, 50L, 200L) {
            if (backspacePressed) {
                GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            } else {
                this.cancel()
            }
        }
        delay(50L)
    }
}