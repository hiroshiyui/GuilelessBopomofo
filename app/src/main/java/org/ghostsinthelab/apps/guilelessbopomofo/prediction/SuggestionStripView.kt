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

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SuggestionItemLayoutBinding

/**
 * A horizontally scrolling row of suggested words above the keyboard, each taking the width
 * it needs. Swipe it for the less likely ones.
 */
class SuggestionStripView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    private val suggestionsAdapter = SuggestionsAdapter()

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = suggestionsAdapter
        // the words change on every key press, animating that is only a distraction
        itemAnimator = null
    }

    /** Shows [words], most likely first, scrolled back to the start. */
    fun show(words: List<String>) {
        suggestionsAdapter.submitList(words) { scrollToPosition(0) }
    }

    private class SuggestionViewHolder(val binding: SuggestionItemLayoutBinding) : ViewHolder(binding.root)

    private class SuggestionsAdapter : ListAdapter<String, SuggestionViewHolder>(WORD_DIFF) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SuggestionViewHolder(
            SuggestionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
            holder.binding.buttonSuggestionItem.text = getItem(position)
        }
    }

    private companion object {
        val WORD_DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}
