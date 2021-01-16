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
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesItemPagedLayoutBinding

class PagedCandidatesAdapter(page: Int) : BaseAdapter() {
    private var candidatesInPage: List<Candidate> = ChewingEngine.getCandidatesByPage(ChewingEngine.candCurrentPage())

    override fun getCount(): Int {
        return candidatesInPage.size
    }

    override fun getItem(position: Int): Candidate {
        return candidatesInPage[position]
    }

    override fun getItemId(position: Int): Long {
        return candidatesInPage[position].index.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val candidatesItemPagedLayoutBinding: CandidatesItemPagedLayoutBinding =
            CandidatesItemPagedLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        candidatesItemPagedLayoutBinding.textViewSelectionKey.text =
            candidatesInPage.get(position).selectionKey.toString()
        candidatesItemPagedLayoutBinding.textViewCandidate.text =
            candidatesInPage.get(position).candidateString

        return candidatesItemPagedLayoutBinding.root
    }
}