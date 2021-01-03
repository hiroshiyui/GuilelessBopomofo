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

package org.ghostsinthelab.apps.guilelessbopomofo.keybuttons

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingEngine
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.BackspaceKeyDownEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.fixedRateTimer

@SuppressLint("ClickableViewAccessibility")
class BackspaceKeyImageButton(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    private var backspacePressed: Boolean = false
    private var lastClickTime: Long = 0

    init {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    backspacePressed = false
                }
            }
            return@setOnTouchListener false
        }

        this.setOnClickListener {
            Log.v(LOGTAG, "setOnClickListener")
            EventBus.getDefault().post(BackspaceKeyDownEvent())
        }

        this.setOnLongClickListener {
            runBlocking {
                launch {
                    backspacePressed = true
                    repeatBackspace()
                }
            }

            return@setOnLongClickListener true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackspaceKeyDown(event: BackspaceKeyDownEvent) {
        // avoids too fast repeat clicks
        if (SystemClock.elapsedRealtime() - lastClickTime < 400) {
            Log.v(LOGTAG, "User repeats clicking too quick...")
        }
        lastClickTime = SystemClock.elapsedRealtime()

        if (ChewingEngine.anyPreeditBufferIsNotEmpty()) {
            ChewingEngine.handleBackspace()
            EventBus.getDefault().post(BufferUpdatedEvent())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
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