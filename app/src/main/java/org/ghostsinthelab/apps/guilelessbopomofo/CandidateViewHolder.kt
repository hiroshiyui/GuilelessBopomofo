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
import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.keys.CandidateButton

class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val LOGTAG: String = "CandidateViewHolder"
    private val candidateButton: CandidateButton = itemView.findViewById(R.id.buttonCandidateItem)

    fun setData(data: Int) {
        candidateButton.text = ChewingEngine.candStringByIndexStatic(data)
        candidateButton.setCandidateButtonOnClickListener(GuilelessBopomofoServiceContext.serviceInstance, data)
    }
}
