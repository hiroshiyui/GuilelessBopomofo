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
import androidx.appcompat.widget.AppCompatButton
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.events.CandidateSelectionDoneEvent
import org.greenrobot.eventbus.EventBus

class CandidateButton(context: Context, attrs: AttributeSet) :
    AppCompatButton(context, attrs, R.attr.buttonStyle) {
    var dataIndex: Int = 0

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MaterialButton, 0, 0).apply {
            try {
                isHapticFeedbackEnabled = true
            } finally {
                recycle()
            }
        }

        this.setOnClickListener {
            performHapticFeedback(GuilelessBopomofoServiceContext.serviceInstance.userHapticFeedbackStrength)
            EventBus.getDefault().post(CandidateSelectionDoneEvent.Indexed(dataIndex))
        }
    }
}