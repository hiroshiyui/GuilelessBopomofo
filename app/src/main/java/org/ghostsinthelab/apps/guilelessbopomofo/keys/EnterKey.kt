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
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.SendSingleDownUpKeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.UpdateBuffersEvent
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class EnterKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            performVibrate(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            action()
            return true
        }
    }

    companion object {
        fun action() {
            if (ChewingUtil.anyPreeditBufferIsNotEmpty()) { // not committed yet
                ChewingBridge.handleEnter()
                EventBus.getDefault().post(UpdateBuffersEvent())
            } else {
                val editorInfo =
                    GuilelessBopomofoServiceContext.service.currentInputEditorInfo
                var multiLineEditText = false

                editorInfo?.let {
                    // Is it a multiple line text field?
                    if ((it.inputType and InputType.TYPE_MASK_CLASS and InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
                        if ((it.inputType and InputType.TYPE_MASK_FLAGS and InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
                            multiLineEditText = true
                        }
                    }

                    // Just do as press Enter, never care about the defined action if we are now in a multiple line text field
                    if (multiLineEditText) {
                        EventBus.getDefault().post(SendSingleDownUpKeyEvent(KeyEvent.KEYCODE_ENTER))
                        return
                    }

                    when (val imeAction = (it.imeOptions and EditorInfo.IME_MASK_ACTION)) {
                        EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND -> {
                            // The current EditText has a specified android:imeOptions attribute.
                            GuilelessBopomofoServiceContext.service.currentInputConnection.performEditorAction(
                                imeAction
                            )
                        }
                        else -> {
                            // The current EditText has no android:imeOptions attribute, or I don't want to make it act as is.
                            EventBus.getDefault().post(SendSingleDownUpKeyEvent(KeyEvent.KEYCODE_ENTER))
                        }
                    }
                }
            }
        }
    }
}