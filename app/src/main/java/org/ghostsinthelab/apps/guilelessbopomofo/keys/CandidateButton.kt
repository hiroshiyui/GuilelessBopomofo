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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import android.util.AttributeSet
import androidx.emoji.widget.EmojiAppCompatButton
import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class CandidateButton(context: Context, attrs: AttributeSet) :
    EmojiAppCompatButton(context, attrs, R.attr.buttonStyle), Vibratable {
    lateinit var candidate: Candidate

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MaterialButton, 0, 0).apply {
            try {
                isHapticFeedbackEnabled = true
            } finally {
                recycle()
            }
        }

        this.setOnClickListener {
            performVibrate(Vibratable.VibrationStrength.LIGHT)
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.candidateSelectionDone(candidate.index)
        }
    }
}