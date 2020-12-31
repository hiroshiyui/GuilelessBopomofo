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
import android.widget.LinearLayout
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class KeyboardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val LOGTAG: String = "KeyboardView"
    private lateinit var v: KeyboardLayoutBinding

    init {
        this.orientation = VERTICAL
        Log.v(LOGTAG, "Building KeyboardView.")
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBufferUpdatedEvent(event: BufferUpdatedEvent) {
        // chewingEngine.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingEngine.commitCheck() == 1) {
            GuilelessBopomofoServiceContext.serviceInstance.currentInputConnection.commitText(
                ChewingEngine.commitString(),
                1
            )
            // dirty hack (?) - 讓 chewingEngine.commitCheck() 歸 0
            // 研究 chewing_commit_Check() 之後想到的，並不是亂碰運氣
            ChewingEngine.handleEnd()
        }
    }
}