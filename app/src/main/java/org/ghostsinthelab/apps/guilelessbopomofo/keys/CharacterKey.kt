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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.R

class CharacterKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    init {
        this.setOnClickListener {
            performVibrate(GuilelessBopomofoServiceContext.serviceInstance.userHapticFeedbackStrength.toLong())

            this.keyCodeString?.let { keycodeString ->
                val keyEvent =
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.keyCodeFromString(keycodeString))
                GuilelessBopomofoServiceContext.serviceInstance.onPrintingKeyDown(keyEvent)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val keyButtonLocation = IntArray(2)
                this.getLocationInWindow(keyButtonLocation)

                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.let {
                    it.keyButtonPopupLayoutBinding.keyButtonPopupImageView.setImageDrawable(this.drawable)
                    it.keyButtonPopup.let {
                        it.animationStyle = R.style.KeyButtonPopupAnimation
                        it.elevation = 8F
                        it.showAtLocation(
                            rootView,
                            Gravity.NO_GRAVITY,
                            keyButtonLocation.get(0),
                            keyButtonLocation.get(1) - this.height
                        )
                        it.update(this.width, this.height)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.keyButtonPopup.dismiss()
            }
        }
        return super.onTouchEvent(event)
    }
}