/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyImageButton
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.fixedRateTimer
import kotlin.coroutines.CoroutineContext

class BackspaceKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs), CoroutineScope {
    private var backspacePressed: Boolean = false
    var lastBackspaceClickTime: Long = 0
    override var mDetector: GestureDetector

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            // avoids too fast repeat clicks
            if (SystemClock.elapsedRealtime() - lastBackspaceClickTime < 100) {
                return false
            }
            lastBackspaceClickTime = SystemClock.elapsedRealtime()

            performVibration(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            performKeyStroke()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            launch { repeatBackspace() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    backspacePressed = true
                }

                MotionEvent.ACTION_MOVE -> {
                    backspacePressed = true
                }

                MotionEvent.ACTION_UP -> {
                    backspacePressed = false
                }

                MotionEvent.ACTION_CANCEL -> {
                    backspacePressed = false
                }
            }
        }
        return true
    }

    private suspend fun repeatBackspace() {
        fixedRateTimer("repeatBackspace", true, 50L, 100L) {
            if (backspacePressed) {
                performKeyStroke()
            } else {
                this@fixedRateTimer.cancel()
            }
        }
        delay(50L)
    }

    private fun performKeyStroke() {
        if (ChewingUtil.Companion.anyPreEditBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleBackspace()
            EventBus.getDefault().post(Events.UpdateBuffers())
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL))
        }
    }
}