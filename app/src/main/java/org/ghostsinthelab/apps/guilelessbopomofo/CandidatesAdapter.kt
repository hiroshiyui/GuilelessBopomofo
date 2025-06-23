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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidateItemLayoutBinding

class CandidatesAdapter : RecyclerView.Adapter<CandidateViewHolder>() {
    private val itemCount: Int = ChewingBridge.chewing.candTotalChoice()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val itemView =
            CandidateItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CandidateViewHolder(itemView.root)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.setData(position)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

}