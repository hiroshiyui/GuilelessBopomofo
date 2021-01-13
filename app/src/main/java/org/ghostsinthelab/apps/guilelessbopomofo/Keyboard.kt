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
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.widget.LinearLayout
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.events.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Keyboard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val LOGTAG: String = "KeyboardView"

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
        if (ChewingEngine.anyPreeditBufferIsNotEmpty()) { // not committed yet
            ChewingEngine.handleEnter()
            EventBus.getDefault().post(BufferUpdatedEvent())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLeftKeyDownEvent(event: LeftKeyDownEvent) {
        ChewingEngine.handleLeft()
        if (ChewingEngine.bufferLen() > 0) {
            EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnKeyboard())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRightKeyDownEvent(event: RightKeyDownEvent) {
        ChewingEngine.handleRight()
        if (ChewingEngine.bufferLen() > 0) {
            EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnKeyboard())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownKeyDownEvent(event: DownKeyDownEvent) {
        if (ChewingEngine.bufferLen() > 0) {
            ChewingEngine.candClose()
            ChewingEngine.candOpen()
            EventBus.getDefault()
                .post(CandidatesWindowOpendEvent.Offset(ChewingEngine.cursorCurrent()))
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
        if (ChewingEngine.anyPreeditBufferIsNotEmpty()) {
            ChewingEngine.handleSpace()
            EventBus.getDefault().post(BufferUpdatedEvent())
            // 空白鍵是否為選字鍵？
            if (ChewingEngine.getSpaceAsSelection() == 1 && ChewingEngine.candTotalChoice() > 0) {
                EventBus.getDefault().post(CandidatesWindowOpendEvent())
            }
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE)
        }
    }
}