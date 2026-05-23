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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import org.ghostsinthelab.apps.guilelessbopomofo.R

class AnnouncementsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_announcements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewAnnouncements)
        emptyView = view.findViewById(R.id.textViewEmptyState)

        adapter = AnnouncementAdapter(emptyList()) { announcement ->
            AnnouncementDialogFragment.newInstance(announcement.id)
                .showSequential(parentFragmentManager, "announcement_${announcement.id}") {
                    reload()
                }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val ctx = requireContext()
        val all = AnnouncementRepository.loadAll(ctx)
            .sortedByDescending { it.date }
        val items = all.map { it to AnnouncementRepository.isRead(ctx, it.id) }
        adapter.setData(items)
        val isEmpty = items.isEmpty()
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
