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

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class EnterKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : KeyImageButton.GestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            performVibrate(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            action()
            return true
        }
    }

    companion object {
        fun action() {
            if (ChewingUtil.anyPreeditBufferIsNotEmpty()) { // not committed yet
                ChewingBridge.handleEnter()
                GuilelessBopomofoServiceContext.service.viewBinding.keyboardPanel.updateBuffers()
            } else {
                val editorInfo =
                    GuilelessBopomofoServiceContext.service.currentInputEditorInfo
                editorInfo?.let {
                    when (val imeAction = (it.imeOptions and EditorInfo.IME_MASK_ACTION)) {
                        EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND -> {
                            // The current EditText has a specified android:imeOptions attribute.
                            GuilelessBopomofoServiceContext.service.currentInputConnection.performEditorAction(
                                imeAction
                            )
                        }
                        else -> {
                            // The current EditText has no android:imeOptions attribute, or I don't want to make it act as is.
                            GuilelessBopomofoServiceContext.service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                        }
                    }
                }
            }
        }
    }
}