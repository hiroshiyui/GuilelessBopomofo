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

import java.io.Reader

/**
 * Reads and writes the CSV file the user phrase manager backs up to and restores from.
 *
 * A restore feeds whatever the user picked from their storage straight into libchewing, so
 * everything here is deliberately strict: an unexpected file is turned away rather than
 * partly swallowed.
 */
object UserPhraseCsv {
    const val HEADER: String = "\"phrase\",\"bopomofo\""

    // Sanity limits, so that a file which is not really a phrase list cannot keep us busy.
    private const val MAX_TOTAL_LENGTH = 1_000_000
    private const val MAX_PHRASE_LENGTH = 100
    private const val MAX_BOPOMOFO_LENGTH = 200

    private const val FIELDS_PER_ROW = 2
    private const val READ_CHUNK_SIZE = 8 * 1024

    /** What came out of a file the user asked us to restore from. */
    sealed interface ParseResult {
        data class Success(val phrases: List<UserPhrase>) : ParseResult

        /** Not a phrase list at all: empty, or far too big to be one. */
        data object UnreadableFile : ParseResult

        /** The first line is not the header this app writes. */
        data object UnexpectedHeader : ParseResult

        /** [lineNumber] is 1-based, as a text editor would show it. */
        data class MalformedRow(val lineNumber: Int) : ParseResult

        /** [lineNumber] is 1-based, as a text editor would show it. */
        data class InvalidBopomofo(val lineNumber: Int) : ParseResult
    }

    fun format(phrases: List<UserPhrase>): String = buildString {
        append(HEADER).append('\n')
        phrases.forEach {
            append('"').append(escapeField(it.phrase)).append("\",\"")
            append(escapeField(it.bopomofo)).append("\"\n")
        }
    }

    /**
     * Parses what [reader] holds, reading no more than a phrase list could be: a file picked
     * by mistake, gigabytes long or a single endless line, is turned away after the first
     * [MAX_TOTAL_LENGTH] characters instead of being read into memory whole.
     */
    fun parse(reader: Reader): ParseResult {
        val text = StringBuilder()
        val chunk = CharArray(READ_CHUNK_SIZE)
        while (true) {
            val read = reader.read(chunk)
            if (read < 0) break
            text.appendRange(chunk, 0, read)
            if (text.length > MAX_TOTAL_LENGTH) return ParseResult.UnreadableFile
        }
        return parse(text.lines())
    }

    fun parse(lines: List<String>): ParseResult {
        if (lines.isEmpty() || lines.sumOf { it.length } > MAX_TOTAL_LENGTH) {
            return ParseResult.UnreadableFile
        }

        if (lines.first().trim() != HEADER) {
            return ParseResult.UnexpectedHeader
        }

        val phrases = mutableListOf<UserPhrase>()
        lines.drop(1).forEachIndexed { index, line ->
            val lineNumber = index + 2 // the header is line 1
            if (line.isBlank()) return@forEachIndexed

            val fields = parseLine(line)
            if (fields.size != FIELDS_PER_ROW) return ParseResult.MalformedRow(lineNumber)

            val (phrase, bopomofo) = fields
            if (!isValidPhrase(phrase) || !isValidField(bopomofo, MAX_BOPOMOFO_LENGTH)) {
                return ParseResult.MalformedRow(lineNumber)
            }
            if (!isValidBopomofo(bopomofo)) {
                return ParseResult.InvalidBopomofo(lineNumber)
            }

            phrases.add(UserPhrase(phrase, bopomofo))
        }

        return ParseResult.Success(phrases)
    }

    private fun isValidPhrase(phrase: String): Boolean = isValidField(phrase, MAX_PHRASE_LENGTH)

    // Non-empty, of a sane length, and free of the control characters (null bytes and such)
    // that have no business being in a phrase.
    private fun isValidField(field: String, maxLength: Int): Boolean =
        field.isNotEmpty() && field.length <= maxLength && field.none { it.isISOControl() }

    fun isValidBopomofo(bopomofo: String): Boolean = bopomofo.all { char ->
        char == ' '
                || char in '\u3100'..'\u312F' // Bopomofo block
                || char in '\u31A0'..'\u31BF' // Bopomofo Extended block
                || char == 'ˊ' || char == 'ˇ' || char == 'ˋ' || char == '˙' // tone marks
    }

    private fun escapeField(field: String): String = field.replace("\"", "\"\"")

    private fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }

                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }

                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
