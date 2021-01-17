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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesItemPagedLayoutBinding

class PagedCandidatesAdapter(candCurrentPage: Int) :
    RecyclerView.Adapter<PagedCandidateViewHolder>() {
    private var candidatesInPage: List<Candidate> =
        ChewingBridge.getCandidatesByPage(candCurrentPage)

    override fun getItemId(position: Int): Long {
        return candidatesInPage[position].index.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagedCandidateViewHolder {
        val itemView =
            CandidatesItemPagedLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return PagedCandidateViewHolder(itemView.root)
    }

    override fun onBindViewHolder(holder: PagedCandidateViewHolder, position: Int) {
        val candidate: Candidate = candidatesInPage[position]
        holder.setData(candidate)
    }

    override fun getItemCount(): Int {
        return candidatesInPage.size
    }
}