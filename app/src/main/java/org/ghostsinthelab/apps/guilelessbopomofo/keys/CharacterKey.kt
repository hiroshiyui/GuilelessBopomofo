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
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext

class CharacterKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : KeyImageButton.GestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            performVibrate(context, GuilelessBopomofoServiceContext.service.userHapticFeedbackStrength.toLong())
            val keyButtonLocation = IntArray(2)
            getLocationInWindow(keyButtonLocation)

            GuilelessBopomofoServiceContext.keyboardPanel.let {
                it.keyButtonPopupLayoutBinding.keyButtonPopupImageView.setImageDrawable(drawable)
                it.keyButtonPopup.let { popup ->
                    popup.height = this@CharacterKey.height
                    popup.width = this@CharacterKey.width
                    popup.showAtLocation(
                        rootView,
                        Gravity.NO_GRAVITY,
                        keyButtonLocation[0],
                        keyButtonLocation[1] - this@CharacterKey.height
                    )
                }
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            keyCodeString?.let { keycodeString ->
                val keyEvent =
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.keyCodeFromString(keycodeString)
                    )
                GuilelessBopomofoServiceContext.service.onPrintingKeyDown(keyEvent)
            }
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_UP -> {
                    GuilelessBopomofoServiceContext.keyboardPanel.keyButtonPopup.dismiss()
                }
            }
        }
        return true
    }
}