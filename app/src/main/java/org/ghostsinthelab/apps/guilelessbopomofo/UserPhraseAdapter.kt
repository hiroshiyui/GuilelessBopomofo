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
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.UserPhraseItemLayoutBinding

class UserPhraseAdapter(
    private val allPhrases: MutableList<UserPhrase>,
    private val onDeleteClick: (UserPhrase) -> Unit
) : RecyclerView.Adapter<UserPhraseViewHolder>() {

    private var filteredPhrases: List<UserPhrase> = allPhrases

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPhraseViewHolder {
        val binding = UserPhraseItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserPhraseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserPhraseViewHolder, position: Int) {
        holder.bind(filteredPhrases[position], onDeleteClick)
    }

    override fun getItemCount(): Int = filteredPhrases.size

    fun setData(newPhrases: List<UserPhrase>) {
        allPhrases.clear()
        allPhrases.addAll(newPhrases)
        filteredPhrases = allPhrases
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredPhrases = if (query.isEmpty()) {
            allPhrases
        } else {
            allPhrases.filter { it.phrase.contains(query) || it.bopomofo.contains(query) }
        }
        notifyDataSetChanged()
    }
}
