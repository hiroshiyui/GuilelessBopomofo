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
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.widget.LinearLayout
import org.ghostsinthelab.apps.guilelessbopomofo.events.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Keyboard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val LOGTAG: String = "KeyboardView"
    var backspacePressed: Boolean = false
    var lastBackspaceClickTime: Long = 0

    init {
        this.orientation = VERTICAL
    }

    override fun onAttachedToWindow() {
        EventBus.getDefault().register(this)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEnterKeyDownEvent(event: EnterKeyDownEvent) {
        if (ChewingUtil.anyPreeditBufferIsNotEmpty()) { // not committed yet
            ChewingBridge.handleEnter()
            EventBus.getDefault().post(BufferUpdatedEvent())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackspaceKeyDown(event: BackspaceKeyDownEvent) {
        // avoids too fast repeat clicks
        if (SystemClock.elapsedRealtime() - lastBackspaceClickTime < 250) {
            return
        }
        lastBackspaceClickTime = SystemClock.elapsedRealtime()

        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        if (ChewingUtil.anyPreeditBufferIsNotEmpty()) {
            ChewingBridge.handleBackspace()
            EventBus.getDefault().post(BufferUpdatedEvent())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLeftKeyDownEvent(event: LeftKeyDownEvent) {
        ChewingBridge.handleLeft()
        if (ChewingBridge.bufferLen() > 0) {
            EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnKeyboard())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRightKeyDownEvent(event: RightKeyDownEvent) {
        ChewingBridge.handleRight()
        if (ChewingBridge.bufferLen() > 0) {
            EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnKeyboard())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownKeyDownEvent(event: DownKeyDownEvent) {
        if (ChewingBridge.bufferLen() > 0) {
            ChewingBridge.candClose()
            ChewingBridge.candOpen()
            EventBus.getDefault()
                .post(CandidatesWindowOpendEvent.Offset(ChewingBridge.cursorCurrent()))
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSpaceKeyDown(event: SpaceKeyDownEvent) {
        spaceKeyDown()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSpaceKeyDown(event: SpaceKeyDownEvent.Physical) {
        if (event.keyEvent.isShiftPressed) {
            EventBus.getDefault().post(MainLayoutChangedEvent())
            return
        }
        spaceKeyDown()
    }

    private fun spaceKeyDown() {
        if (ChewingUtil.anyPreeditBufferIsNotEmpty()) {
            ChewingBridge.handleSpace()
            EventBus.getDefault().post(BufferUpdatedEvent())
            // 空白鍵是否為選字鍵？
            if (ChewingBridge.getSpaceAsSelection() == 1 && ChewingBridge.candTotalChoice() > 0) {
                EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnKeyboard())
                EventBus.getDefault().post(CandidatesWindowOpendEvent())
            }
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE)
        }
    }
}