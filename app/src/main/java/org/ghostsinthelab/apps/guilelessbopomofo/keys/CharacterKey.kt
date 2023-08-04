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
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class CharacterKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            performVibrate(
                context,
                GuilelessBopomofoService.userHapticFeedbackStrength
            )
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            EventBus.getDefault().post(Events.PrintingKeyDown(this@CharacterKey))
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    val keyButtonLocation = IntArray(2)
                    this@CharacterKey.getLocationInWindow(keyButtonLocation)

                    GuilelessBopomofoServiceContext.service.viewBinding.keyboardPanel.apply {
                        keyButtonPopupLayoutBinding.keyButtonPopupImageView.setImageDrawable(
                            this@CharacterKey.drawable
                        )
                        keyButtonPopup.let { popup ->
                            popup.height = this@CharacterKey.height
                            popup.width = this@CharacterKey.width
                            popup.showAtLocation(
                                this@CharacterKey.rootView,
                                Gravity.NO_GRAVITY,
                                keyButtonLocation[0],
                                keyButtonLocation[1] - this@CharacterKey.height
                            )
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    GuilelessBopomofoServiceContext.service.viewBinding.keyboardPanel.keyButtonPopup.dismiss()
                }
                else -> {}
            }
        }
        return true
    }
}