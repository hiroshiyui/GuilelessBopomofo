package org.ghostsinthelab.apps.guilelessbopomofo

import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.junit.Assert.assertEquals
import org.junit.Test

class LayoutEnumsTest {

    // Layout tests

    @Test
    fun layout_hasExactlyFiveValues() {
        assertEquals(5, Layout.entries.size)
    }

    @Test
    fun layout_containsExpectedValues() {
        val names = Layout.entries.map { it.name }
        assertEquals(listOf("MAIN", "SYMBOLS", "CANDIDATES", "QWERTY", "COMPACT"), names)
    }

    // DirectionKey tests

    @Test
    fun directionKey_hasExactlyTwoValues() {
        assertEquals(2, DirectionKey.entries.size)
    }

    @Test
    fun directionKey_containsExpectedValues() {
        val names = DirectionKey.entries.map { it.name }
        assertEquals(listOf("RIGHT", "LEFT"), names)
    }

    // SelectionKeys tests

    @Test
    fun selectionKeys_eachEntryHasExactlyTenKeys() {
        SelectionKeys.entries.forEach { entry ->
            assertEquals(
                "${entry.name} should have exactly 10 keys",
                10,
                entry.keys.size
            )
        }
    }

    @Test
    fun selectionKeys_setStringMatchesEnumName() {
        SelectionKeys.entries.forEach { entry ->
            assertEquals(
                "set string for ${entry.name} should match its enum name",
                entry.name,
                entry.set
            )
        }
    }

    @Test
    fun selectionKeys_numberRow_hasCorrectKeys() {
        val expected = "1234567890".map { it.code }.toIntArray()
        assertEquals(expected.toList(), SelectionKeys.NUMBER_ROW.keys.toList())
    }

    @Test
    fun selectionKeys_tabRow_hasCorrectKeys() {
        val expected = "qwertyuiop".map { it.code }.toIntArray()
        assertEquals(expected.toList(), SelectionKeys.TAB_ROW.keys.toList())
    }

    @Test
    fun selectionKeys_homeRow_hasCorrectKeys() {
        val expected = "asdfghjkl;".map { it.code }.toIntArray()
        assertEquals(expected.toList(), SelectionKeys.HOME_ROW.keys.toList())
    }

    @Test
    fun selectionKeys_dvorakHomeRow_hasCorrectKeys() {
        val expected = "aoeuiddhtns".substring(0, 10).map { it.code }.toIntArray()
        // More precisely:
        val dvorakExpected = charArrayOf('a', 'o', 'e', 'u', 'i', 'd', 'h', 't', 'n', 's')
            .map { it.code }.toIntArray()
        assertEquals(dvorakExpected.toList(), SelectionKeys.DVORAK_HOME_ROW.keys.toList())
    }

    @Test
    fun selectionKeys_noDuplicateKeysWithinEntry() {
        SelectionKeys.entries.forEach { entry ->
            val keysList = entry.keys.toList()
            assertEquals(
                "${entry.name} should have no duplicate keys",
                keysList.size,
                keysList.toSet().size
            )
        }
    }

    @Test
    fun selectionKeys_hasExpectedEntryCount() {
        assertEquals(6, SelectionKeys.entries.size)
    }
}
