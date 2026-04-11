/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

interface EdgeToEdge {

    /**
     * Applies the combined system-bar + display-cutout insets as padding to [view].
     *
     * Prefer this over margin-based approaches: padding keeps the view's size and
     * background unchanged, so the window can still draw edge-to-edge while the
     * contents are kept clear of the status bar, navigation bar, and cutouts.
     */
    fun applyInsetsAsPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    fun applyInputViewBottomEdgeWithGradient(inputView: View, imeBottomGradientSpacer: View) {
        // If Android version is lower than 15 (ICE_CREAM_SANDWICH), we don't need to do anything
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return
        }
        // Set height of IME bottom spacer by listening to system bar insets, then make it visible
        ViewCompat.setOnApplyWindowInsetsListener(inputView) { v, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBarHeight = systemBarsInsets.bottom

            if (navigationBarHeight > 0) {
                imeBottomGradientSpacer.let {
                    it.updateLayoutParams {
                        height = navigationBarHeight
                    }
                    it.visibility = View.VISIBLE
                }
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}