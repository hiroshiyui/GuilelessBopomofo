package org.ghostsinthelab.apps.guilelessbopomofo.enums

enum class SelectionKeys(val keys: IntArray, val set: String) {
    NUMBER_ROW(
        charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }.toIntArray(),
        "NUMBER_ROW"
    ),
    HOME_ROW(
        charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }.toIntArray(),
        "HOME_ROW"
    ),
    HOME_TAB_MIXED_MODE1(
        charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }.toIntArray(),
        "HOME_TAB_MIXED_MODE1"
    ),
    HOME_TAB_MIXED_MODE2(
        charArrayOf('h', 'j', 'k', 'l', ';', 'y', 'u', 'i', 'o', 'p').map { it.code }.toIntArray(),
        "HOME_TAB_MIXED_MODE2"
    ),
    DVORAK_HOME_ROW(
        charArrayOf('a', 'o', 'e', 'u', 'i', 'd', 'h', 't', 'n', 's').map { it.code }.toIntArray(),
        "DVORAK_HOME_ROW"
    ),
    DVORAK_MIXED_MODE(
        charArrayOf('a', 'o', 'e', 'u', 'i', ';', 'q', 'j', 'k', 'x').map { it.code }.toIntArray(),
        "DVORAK_MIXED_MODE"
    )
}