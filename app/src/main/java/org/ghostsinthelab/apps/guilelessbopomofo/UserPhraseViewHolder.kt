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

import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.UserPhraseItemLayoutBinding

class UserPhraseViewHolder(private val binding: UserPhraseItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(userPhrase: UserPhrase, onDeleteClick: (UserPhrase) -> Unit) {
        binding.textViewPhrase.text = userPhrase.phrase
        binding.textViewBopomofo.text = userPhrase.bopomofo
        binding.buttonDelete.setOnClickListener { onDeleteClick(userPhrase) }
    }
}
