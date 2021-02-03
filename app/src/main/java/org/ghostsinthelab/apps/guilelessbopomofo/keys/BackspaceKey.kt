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
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.BackspaceKeyDownEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.fixedRateTimer

@SuppressLint("ClickableViewAccessibility")
class BackspaceKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {

    init {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardView.backspacePressed =
                        false
                }
            }
            return@setOnTouchListener false
        }

        this.setOnClickListener {
            EventBus.getDefault().post(BackspaceKeyDownEvent())
        }

        this.setOnLongClickListener {
            runBlocking {
                launch {
                    GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardView.backspacePressed =
                        true
                    repeatBackspace()
                }
            }

            return@setOnLongClickListener true
        }
    }

    private suspend fun repeatBackspace() {
        fixedRateTimer("repeatBackspace", true, 50L, 125L) {
            if (GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardView.backspacePressed) {
                action()
            } else {
                this.cancel()
            }
        }
        delay(50L)
    }

    companion object {
        fun action() {
            if (ChewingUtil.anyPreeditBufferIsNotEmpty()) {
                ChewingBridge.handleBackspace()
                EventBus.getDefault().post(BufferUpdatedEvent())
            } else {
                GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }
        }
    }
}