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

import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class CandidateViewHolder(imeService: GuilelessBopomofoService, itemView: View) :
    RecyclerView.ViewHolder(itemView), GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "CandidateViewHolder"
    private val button: Button = itemView.findViewById(R.id.buttonCandidateItem)
    private var inputConnection: InputConnection
    override var serviceContext: GuilelessBopomofoService = imeService

    init {
        inputConnection = serviceContext.currentInputConnection
    }

    fun setData(data: Int) {
        button.text = serviceContext.chewingEngine.candStringByIndexStatic(data)
        button.setOnClickListener {
            serviceContext.chewingEngine.apply {
                candChooseByIndex(data)
                candClose()
                handleEnd()
            }
            serviceContext.viewBinding.apply {
                keyboardPanel.currentCandidatesList = 0
                keyboardView.syncPreEditBuffers(serviceContext)
                keyboardPanel.switchToMainLayout(serviceContext)
            }
        }
    }
}
