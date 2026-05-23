/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.announcements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ghostsinthelab.apps.guilelessbopomofo.R

class AnnouncementAdapter(
    private var items: List<Pair<Announcement, Boolean>>,
    private val onClick: (Announcement) -> Unit,
) : RecyclerView.Adapter<AnnouncementAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textViewTitle)
        val date: TextView = view.findViewById(R.id.textViewDate)
        val unreadDot: View = view.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_item_layout, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (announcement, isRead) = items[position]
        holder.title.text = announcement.title
        holder.date.text = announcement.date
        holder.unreadDot.visibility = if (isRead) View.INVISIBLE else View.VISIBLE
        holder.itemView.setOnClickListener { onClick(announcement) }
    }

    override fun getItemCount(): Int = items.size

    fun setData(newItems: List<Pair<Announcement, Boolean>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
