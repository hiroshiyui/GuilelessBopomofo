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

package org.ghostsinthelab.apps.guilelessbopomofo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The libchewing context is shared by the input method and the settings screens. */
@RunWith(AndroidJUnit4::class)
class ChewingLifecycleInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath = appContext.dataDir.absolutePath
    private var reconnections = 0

    @Subscribe
    fun onChewingReconnected(event: Events.ChewingReconnected) {
        reconnections += 1
    }

    @Before
    fun setUp() {
        ChewingUtil.setupChewingData(appContext, dataPath)
        ChewingBridge.chewing.connect(dataPath)
        EventBus.getDefault().register(this)
    }

    @After
    fun tearDown() {
        EventBus.getDefault().unregister(this)
    }

    @Test
    fun delete_forgetsTheContext_andIsSafeTwice() {
        ChewingBridge.chewing.delete()
        assertEquals(0L, ChewingBridge.chewing.context)
        ChewingBridge.chewing.delete()
        assertEquals(0L, ChewingBridge.chewing.context)
        // and nothing touches freed memory: a getter now answers as for no context
        assertEquals("", ChewingBridge.chewing.bufferStringStatic())
    }

    @Test
    fun connectingAgain_opensAWorkingContext() {
        ChewingBridge.chewing.connect(dataPath)
        assertNotEquals(0L, ChewingBridge.chewing.context)
        assertEquals("", ChewingBridge.chewing.bufferStringStatic())
    }

    @Test
    fun flushingUserPhrases_keepsTheModes_andTellsTheInputMethod() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
            ChewingBridge.chewing.setShapeMode(ShapeMode.FULL.mode)
            ChewingUtil.flushContext(appContext)
        }
        assertNotEquals(0L, ChewingBridge.chewing.context)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(ShapeMode.FULL.mode, ChewingBridge.chewing.getShapeMode())
        assertEquals(1, reconnections)
        // restore
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setShapeMode(ShapeMode.HALF.mode)
    }
}
