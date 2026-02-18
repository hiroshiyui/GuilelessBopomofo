package org.ghostsinthelab.apps.guilelessbopomofo

import org.junit.Assert.assertEquals
import org.junit.Test

class ChewingUtilKeyMappingTest {

    // All Dvorak keys and their expected QWERTY mappings (paired positionally)
    private val dvorakKeys = listOf(
        '\'', '\"', ',', '<', '.', '>', 'p', 'P', 'y', 'Y', 'f', 'F', 'g', 'G',
        'c', 'C', 'r', 'R', 'l', 'L', '/', '?', '=', '+', '\\', '|',
        'a', 'A', 'o', 'O', 'e', 'E', 'u', 'U', 'i', 'I', 'd', 'D', 'h', 'H',
        't', 'T', 'n', 'N', 's', 'S', '-', '_',
        ';', ':', 'q', 'Q', 'j', 'J', 'k', 'K', 'x', 'X', 'b', 'B', 'm', 'M',
        'w', 'W', 'v', 'V', 'z', 'Z'
    )

    private val qwertyKeys = listOf(
        'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U',
        'i', 'I', 'o', 'O', 'p', 'P', '[', '{', ']', '}', '\\', '|',
        'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J',
        'k', 'K', 'l', 'L', ';', ':', '\'', '\"',
        'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M',
        ',', '<', '.', '>', '/', '?'
    )

    @Test
    fun dvorakToQwerty_allKeysMappedCorrectly() {
        dvorakKeys.zip(qwertyKeys).forEach { (dvorak, qwerty) ->
            assertEquals(
                "Dvorak '$dvorak' should map to QWERTY '$qwerty'",
                qwerty,
                ChewingUtil.dvorakToQwertyKeyMapping(dvorak)
            )
        }
    }

    @Test
    fun qwertyToDvorak_allKeysMappedCorrectly() {
        qwertyKeys.zip(dvorakKeys).forEach { (qwerty, dvorak) ->
            assertEquals(
                "QWERTY '$qwerty' should map to Dvorak '$dvorak'",
                dvorak,
                ChewingUtil.qwertyToDvorakKeyMapping(qwerty)
            )
        }
    }

    @Test
    fun roundTrip_dvorakToQwertyAndBack() {
        dvorakKeys.forEach { key ->
            val qwerty = ChewingUtil.dvorakToQwertyKeyMapping(key)
            val backToDvorak = ChewingUtil.qwertyToDvorakKeyMapping(qwerty)
            assertEquals(
                "Round-trip failed for Dvorak key '$key'",
                key,
                backToDvorak
            )
        }
    }

    @Test
    fun roundTrip_qwertyToDvorakAndBack() {
        qwertyKeys.forEach { key ->
            val dvorak = ChewingUtil.qwertyToDvorakKeyMapping(key)
            val backToQwerty = ChewingUtil.dvorakToQwertyKeyMapping(dvorak)
            assertEquals(
                "Round-trip failed for QWERTY key '$key'",
                key,
                backToQwerty
            )
        }
    }

    @Test
    fun unmappedKeys_returnThemselves() {
        val unmapped = listOf('0', '1', '9', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '`', '~')
        unmapped.forEach { key ->
            assertEquals(
                "Unmapped key '$key' should return itself for dvorakToQwerty",
                key,
                ChewingUtil.dvorakToQwertyKeyMapping(key)
            )
            assertEquals(
                "Unmapped key '$key' should return itself for qwertyToDvorak",
                key,
                ChewingUtil.qwertyToDvorakKeyMapping(key)
            )
        }
    }

    @Test
    fun listOfDataFiles_returnsExpectedFiles() {
        val files = ChewingUtil.listOfDataFiles()
        assertEquals(4, files.size)
        assertEquals(listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat"), files)
    }
}
