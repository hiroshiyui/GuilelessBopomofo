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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPhraseCsvTest {

    private fun linesOf(vararg rows: String): List<String> =
        listOf(UserPhraseCsv.HEADER) + rows.toList()

    // --- writing ---

    @Test
    fun format_writesAHeaderAndOneRowPerPhrase() {
        val csv = UserPhraseCsv.format(
            listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"), UserPhrase("鍵盤", "ㄐㄧㄢˋ ㄆㄢˊ"))
        )

        assertEquals(
            """
            "phrase","bopomofo"
            "測試","ㄘㄜˋ ㄕˋ"
            "鍵盤","ㄐㄧㄢˋ ㄆㄢˊ"
            """.trimIndent() + "\n",
            csv
        )
    }

    @Test
    fun format_doublesTheQuotesInsideAField() {
        val csv = UserPhraseCsv.format(listOf(UserPhrase("a\"b", "ㄅ")))
        assertTrue(csv.contains(""""a""b","ㄅ""""))
    }

    @Test
    fun formatThenParse_roundTrips() {
        val phrases = listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"), UserPhrase("注音", "ㄓㄨˋ ㄧㄣ"))
        val parsed = UserPhraseCsv.parse(UserPhraseCsv.format(phrases).trimEnd('\n').split("\n"))
        assertEquals(UserPhraseCsv.ParseResult.Success(phrases), parsed)
    }

    // --- reading ---

    @Test
    fun parse_readsEveryRow() {
        val result = UserPhraseCsv.parse(linesOf(""""測試","ㄘㄜˋ ㄕˋ"""", """"注音","ㄓㄨˋ ㄧㄣ""""))
        assertEquals(
            UserPhraseCsv.ParseResult.Success(
                listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"), UserPhrase("注音", "ㄓㄨˋ ㄧㄣ"))
            ),
            result
        )
    }

    @Test
    fun parse_skipsBlankRows() {
        val result = UserPhraseCsv.parse(linesOf("", """"測試","ㄘㄜˋ ㄕˋ"""", "   "))
        assertEquals(
            UserPhraseCsv.ParseResult.Success(listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"))), result
        )
    }

    @Test
    fun parse_readsADoubledQuoteAsOne() {
        val result = UserPhraseCsv.parse(linesOf(""""a""b","ㄅ""""))
        assertEquals(UserPhraseCsv.ParseResult.Success(listOf(UserPhrase("a\"b", "ㄅ"))), result)
    }

    @Test
    fun parse_headerOnlyIsAnEmptyList() {
        assertEquals(UserPhraseCsv.ParseResult.Success(emptyList()), UserPhraseCsv.parse(linesOf()))
    }

    @Test
    fun parse_rejectsAnEmptyFile() {
        assertEquals(UserPhraseCsv.ParseResult.UnreadableFile, UserPhraseCsv.parse(emptyList()))
    }

    @Test
    fun parse_rejectsAnOversizedFile() {
        val hugeRow = """"${"字".repeat(2000)}","ㄗˋ""""
        val lines = linesOf(*Array(1000) { hugeRow })
        assertEquals(UserPhraseCsv.ParseResult.UnreadableFile, UserPhraseCsv.parse(lines))
    }

    @Test
    fun parse_rejectsAForeignHeader() {
        assertEquals(
            UserPhraseCsv.ParseResult.UnexpectedHeader,
            UserPhraseCsv.parse(listOf("word,reading", """"測試","ㄘㄜˋ ㄕˋ""""))
        )
    }

    @Test
    fun parse_reportsTheLineOfARowWithTheWrongFieldCount() {
        val result = UserPhraseCsv.parse(linesOf(""""測試","ㄘㄜˋ ㄕˋ"""", """"注音","ㄓㄨˋ","extra""""))
        assertEquals(UserPhraseCsv.ParseResult.MalformedRow(3), result)
    }

    @Test
    fun parse_reportsARowWithAnEmptyField() {
        assertEquals(
            UserPhraseCsv.ParseResult.MalformedRow(2), UserPhraseCsv.parse(linesOf(""""","ㄘㄜˋ""""))
        )
    }

    @Test
    fun parse_reportsARowHoldingAControlCharacter() {
        assertEquals(
            UserPhraseCsv.ParseResult.MalformedRow(2),
            UserPhraseCsv.parse(linesOf("\"測\u0000試\",\"ㄘㄜˋ ㄕˋ\""))
        )
    }

    @Test
    fun parse_reportsAnOverlongPhrase() {
        assertEquals(
            UserPhraseCsv.ParseResult.MalformedRow(2),
            UserPhraseCsv.parse(linesOf(""""${"字".repeat(101)}","ㄗˋ""""))
        )
    }

    @Test
    fun parse_reportsSomethingThatIsNotBopomofo() {
        assertEquals(
            UserPhraseCsv.ParseResult.InvalidBopomofo(2),
            UserPhraseCsv.parse(linesOf(""""測試","ce shi""""))
        )
    }

    // --- the bopomofo check itself ---

    @Test
    fun isValidBopomofo_acceptsSymbolsTonesAndSpaces() {
        assertTrue(UserPhraseCsv.isValidBopomofo("ㄘㄜˋ ㄕˋ"))
        assertTrue(UserPhraseCsv.isValidBopomofo("ㄓㄨˋ ㄧㄣ"))
        assertTrue(UserPhraseCsv.isValidBopomofo("ㄅㄆㄇ˙ˊˇˋ"))
    }

    @Test
    fun isValidBopomofo_rejectsAnythingElse() {
        assertFalse(UserPhraseCsv.isValidBopomofo("ce shi"))
        assertFalse(UserPhraseCsv.isValidBopomofo("測試"))
        assertFalse(UserPhraseCsv.isValidBopomofo("ㄘㄜˋ!"))
    }

    @Test
    fun parseFromReader_readsAPhraseList() {
        val csv = UserPhraseCsv.format(listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ")))
        assertEquals(
            UserPhraseCsv.ParseResult.Success(listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"))),
            UserPhraseCsv.parse(java.io.StringReader(csv)),
        )
    }

    @Test
    fun parseFromReader_stopsReadingAtTheSizeLimit() {
        // An endless line: the reader must give up rather than read it all.
        var served = 0L
        val endless = object : java.io.Reader() {
            override fun read(buffer: CharArray, offset: Int, length: Int): Int {
                java.util.Arrays.fill(buffer, offset, offset + length, 'x')
                served += length
                return length
            }

            override fun close() {}
        }
        assertEquals(UserPhraseCsv.ParseResult.UnreadableFile, UserPhraseCsv.parse(endless))
        assertTrue("read $served characters", served < 2_000_000)
    }

    @Test
    fun aByteOrderMarkBeforeTheHeader_isAccepted() {
        val csv = "\uFEFF" + UserPhraseCsv.format(listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ")))
        assertEquals(
            UserPhraseCsv.ParseResult.Success(listOf(UserPhrase("測試", "ㄘㄜˋ ㄕˋ"))),
            UserPhraseCsv.parse(java.io.StringReader(csv)),
        )
    }
}
