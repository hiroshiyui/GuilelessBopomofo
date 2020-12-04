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
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        this.orientation = VERTICAL
        Log.v(LOGTAG, "Building KeyboardView.")
    }

    fun syncPreEditString(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        v.textViewPreEditBuffer.text = imeService.chewingEngine.bufferStringStatic()
        v.textViewBopomofoBuffer.text = imeService.chewingEngine.bopomofoStringStatic()
    }

    fun setOnClickPreEditCharListener(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        v.textViewPreEditBuffer.setOnClickListener {
            val offset = v.textViewPreEditBuffer.offset

            // move to first character
            imeService.chewingEngine.handleHome()

            // move to clicked character
            repeat(offset) {
                imeService.chewingEngine.handleRight()
            }

            // list candidates
            imeService.chewingEngine.candOpen()
            v.keyboardPanel.switchToCandidatesLayout(imeService)
        }
    }
}