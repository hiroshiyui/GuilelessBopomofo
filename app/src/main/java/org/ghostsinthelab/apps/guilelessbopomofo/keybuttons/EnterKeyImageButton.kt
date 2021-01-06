/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingEngine
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.EnterKeyDownEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EnterKeyImageButton(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    init {
        this.setOnClickListener {
            EventBus.getDefault().post(EnterKeyDownEvent())
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
    fun onEnterKeyDownEvent(event: EnterKeyDownEvent) {
        if (ChewingEngine.anyPreeditBufferIsNotEmpty()) { // not committed yet
            ChewingEngine.handleEnter()
            EventBus.getDefault().post(BufferUpdatedEvent())
        } else {
            GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }
}