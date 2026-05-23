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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import org.ghostsinthelab.apps.guilelessbopomofo.R

class AnnouncementDialogFragment : DialogFragment() {

    private var onDismissCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val id = requireArguments().getString(ARG_ID)
            ?: error("AnnouncementDialogFragment requires $ARG_ID")
        val ctx = requireContext()

        val announcement = AnnouncementRepository.loadAll(ctx).firstOrNull { it.id == id }
            ?: error("Unknown announcement id: $id")
        val body = AnnouncementRepository.loadMarkdown(ctx, announcement)

        val container = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_announcement, null) as ScrollView
        val bodyView = container.findViewById<TextView>(R.id.textViewAnnouncementBody)
        Markwon.create(ctx).setMarkdown(bodyView, body)

        // Explicit dismiss only — back button does not mark as read.
        isCancelable = false

        return MaterialAlertDialogBuilder(ctx)
            .setTitle(announcement.title)
            .setView(container)
            .setPositiveButton(R.string.announcement_got_it) { _, _ ->
                AnnouncementRepository.markRead(ctx, announcement.id)
                onDismissCallback?.invoke()
            }
            .create()
    }

    fun showSequential(fm: FragmentManager, tag: String, onDismissed: () -> Unit) {
        onDismissCallback = onDismissed
        show(fm, tag)
    }

    companion object {
        private const val ARG_ID = "announcement_id"

        fun newInstance(id: String): AnnouncementDialogFragment {
            return AnnouncementDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_ID, id) }
            }
        }
    }
}
