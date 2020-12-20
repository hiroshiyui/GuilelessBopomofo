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

class KeyboardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardView"
    private lateinit var v: KeyboardLayoutBinding
    override lateinit var guilelessBopomofoService: GuilelessBopomofoService

    init {
        this.orientation = VERTICAL
        Log.v(LOGTAG, "Building KeyboardView.")
    }

    fun updateBuffers(guilelessBopomofoService: GuilelessBopomofoService = this.guilelessBopomofoService) {
        v = guilelessBopomofoService.viewBinding
        v.textViewPreEditBuffer.text = guilelessBopomofoService.chewingEngine.bufferStringStatic()
        v.textViewBopomofoBuffer.text = guilelessBopomofoService.chewingEngine.bopomofoStringStatic()

        // chewingEngine.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (guilelessBopomofoService.chewingEngine.commitCheck() == 1) {
            guilelessBopomofoService.currentInputConnection.commitText(guilelessBopomofoService.chewingEngine.commitString(), 1)
            // dirty hack (?) - 讓 chewingEngine.commitCheck() 歸 0
            // 研究 chewing_commit_Check() 之後想到的，並不是亂碰運氣
            guilelessBopomofoService.chewingEngine.handleEnd()
        }
    }

    fun setOnClickPreEditCharListener(guilelessBopomofoService: GuilelessBopomofoService = this.guilelessBopomofoService) {
        v = guilelessBopomofoService.viewBinding
        v.textViewPreEditBuffer.setOnClickListener {
            val offset = v.textViewPreEditBuffer.offset
            guilelessBopomofoService.chewingEngine.moveToPreEditBufferOffset(offset)
            v.keyboardPanel.switchToCandidatesLayout(offset, guilelessBopomofoService)
        }
    }
}