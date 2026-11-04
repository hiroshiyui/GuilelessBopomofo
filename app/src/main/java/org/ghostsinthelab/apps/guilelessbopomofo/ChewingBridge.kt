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

package org.ghostsinthelab.apps.guilelessbopomofo

/** Whether libchewing is typing Bopomofo ([CHINESE]) or passing letters through ([SYMBOL]). */
enum class ChiEngMode(val mode: Int) {
    SYMBOL(0), CHINESE(1)
}

/** Whether the alphanumerical keyboard types half-width (ASCII) or full-width characters. */
enum class ShapeMode(val mode: Int) {
    HALF(0), FULL(1)
}

/**
 * The on-screen keyboards one can choose. [layout] is what the preference stores;
 * [chewingLayout] is the keyboard type libchewing is told, which differs only for keyboards
 * that send another layout's key codes.
 */
enum class BopomofoSoftKeyboards(val layout: String, val chewingLayout: String = layout) {
    KB_DEFAULT("KB_DEFAULT"),
    KB_HSU("KB_HSU"),
    KB_ET("KB_ET"),
    KB_ET26("KB_ET26"),
    KB_DACHEN_CP26("KB_DACHEN_CP26"),

    // A phone keypad that types Dachen key codes.
    KB_DACHEN_COMPACT("KB_DACHEN_COMPACT", chewingLayout = "KB_DEFAULT"),
    ;

    companion object {
        /** The keyboard a stored preference names, or the default Dachen one for anything unheard of. */
        fun fromLayout(layout: String?): BopomofoSoftKeyboards = entries.find { it.layout == layout } ?: KB_DEFAULT
    }
}

/**
 * The Bopomofo arrangements a physical keyboard can be used with, named as libchewing names
 * them; [layout] is what the preference stores and what libchewing is told.
 */
enum class BopomofoPhysicalKeyboards(val layout: String) {
    KB_DEFAULT("KB_DEFAULT"),
    KB_HSU("KB_HSU"),
    KB_IBM("KB_IBM"),
    KB_GIN_YIEH("KB_GIN_YIEH"),
    KB_ET("KB_ET"),
    KB_ET26("KB_ET26"),
    KB_DVORAK("KB_DVORAK"),
    KB_DVORAK_HSU("KB_DVORAK_HSU"),
    KB_DACHEN_CP26("KB_DACHEN_CP26"),
    KB_HANYU_PINYIN("KB_HANYU_PINYIN"),
    KB_THL_PINYIN("KB_THL_PINYIN"),
    KB_MPS2_PINYIN("KB_MPS2_PINYIN"),
    KB_CARPALX("KB_CARPALX"),
    KB_COLEMAK_DH_ANSI("KB_COLEMAK_DH_ANSI"),
    KB_COLEMAK_DH_ORTH("KB_COLEMAK_DH_ORTH"),
    KB_WORKMAN("KB_WORKMAN"),
    KB_COLEMAK("KB_COLEMAK"),
}

/**
 * How libchewing turns Bopomofo into Han characters: one syllable at a time, with automatic
 * phrase segmentation, or segmentation that forgives incomplete syllables.
 */
enum class ConversionEngines(val mode: Int) {
    SIMPLE_CONVERSION_ENGINE(0),
    CHEWING_CONVERSION_ENGINE(1),
    FUZZY_CHEWING_CONVERSION_ENGINE(2)
}

// All access to ChewingBridge.chewing must be from the main thread. InputMethodService
// callbacks, and the ImeAction handling they lead to, satisfy this requirement. The
// underlying native ChewingContext is not thread-safe.
object ChewingBridge {
    val chewing: Chewing = Chewing()

    private val reconnectListeners = mutableListOf<() -> Unit>()

    /**
     * Asks to be told when libchewing has been given a fresh context, without the settings
     * the input method put on the old one. The settings screens do that, whether or not the
     * input method is running; it listens while it is. Listeners are called on the main thread.
     */
    fun addReconnectListener(listener: () -> Unit) {
        reconnectListeners += listener
    }

    /** Undoes [addReconnectListener]; a listener may remove itself while being called. */
    fun removeReconnectListener(listener: () -> Unit) {
        reconnectListeners -= listener
    }

    /** Called by [ChewingUtil] once a fresh context is connected. */
    internal fun notifyReconnected() {
        // a listener may remove itself while being called
        reconnectListeners.toList().forEach { it() }
    }
}