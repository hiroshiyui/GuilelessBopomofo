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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.*
import org.ghostsinthelab.apps.guilelessbopomofo.events.MainLayoutChangedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs) {
    private val LOGTAG: String = "KeyboardPanel"
    var currentCandidatesList: Int = 0
    private var currentOffset: Int = 0
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    private lateinit var keyboardEt26LayoutBinding: KeyboardEt26LayoutBinding
    private lateinit var keyboardDachenLayoutBinding: KeyboardDachenLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    private lateinit var keyboardQwertyLayoutBinding: KeyboardQwertyLayoutBinding

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES, QWERTY }

    lateinit var currentKeyboardLayout: KeyboardLayout

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMainLayoutChangedEvent(event: MainLayoutChangedEvent) {
        when(ChewingEngine.getChiEngMode()) {
            SYMBOL_MODE ->
                switchToQwertyLayout()
            CHINESE_MODE ->
                switchToBopomofoLayout()
        }
    }

    fun switchToMainLayout() {
        if (ChewingEngine.getChiEngMode() == CHINESE_MODE) {
            switchToBopomofoLayout()
        } else {
            switchToQwertyLayout()
        }
    }

    fun switchToBopomofoLayout() {
        Log.v(LOGTAG, "switchToMainLayout")
        currentKeyboardLayout = KeyboardLayout.MAIN

        v = GuilelessBopomofoServiceContext.serviceInstance.viewBinding
        v.keyboardPanel.removeAllViews()

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        val userKeyboardLayoutPreference = GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getString(
            "user_keyboard_layout",
            GuilelessBopomofoService.defaultKeyboardLayout
        )

        when (userKeyboardLayoutPreference) {
            "KB_HSU" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_HSU")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardHsuLayoutBinding =
                    KeyboardHsuLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                v.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
            }
            "KB_ET26" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_ET26")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardEt26LayoutBinding =
                    KeyboardEt26LayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                v.keyboardPanel.addView(keyboardEt26LayoutBinding.root)
            }
            "KB_DEFAULT" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_DEFAULT")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardDachenLayoutBinding =
                    KeyboardDachenLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                v.keyboardPanel.addView(keyboardDachenLayoutBinding.root)
            }
        }
    }

    fun switchToQwertyLayout() {
        Log.v(LOGTAG, "switchToQwertyLayout")
        currentKeyboardLayout = KeyboardLayout.QWERTY

        keyboardQwertyLayoutBinding =
            KeyboardQwertyLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
        v = GuilelessBopomofoServiceContext.serviceInstance.viewBinding
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(keyboardQwertyLayoutBinding.root)
    }

    // list current offset's candidates in the candidate window
    fun switchToCandidatesLayout(offset: Int) {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        // reset candidates list if offset has been changed
        if (currentOffset != offset) {
            currentOffset = offset
            currentCandidatesList = 0
        }

        // switch to next candidates list
        repeat(currentCandidatesList) {
            ChewingEngine.candListNext()
        }

        // circulate candidates list cursor
        if (ChewingEngine.candListHasNext()) {
            currentCandidatesList += 1
        } else {
            currentCandidatesList = 0
        }

        renderCandidatesLayout()
    }

    // just list current candidate window
    fun switchToCandidatesLayout() {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        renderCandidatesLayout()
    }

    private fun renderCandidatesLayout() {
        Log.v(LOGTAG, "renderCandidatesLayout")
        currentKeyboardLayout = KeyboardLayout.CANDIDATES

        candidatesLayoutBinding =
            CandidatesLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
        v = GuilelessBopomofoServiceContext.serviceInstance.viewBinding
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(candidatesLayoutBinding.root)

        val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView
        candidatesRecyclerView.adapter = CandidatesAdapter()
        candidatesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL)
    }
}