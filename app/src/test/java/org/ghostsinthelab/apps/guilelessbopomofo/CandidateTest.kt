package org.ghostsinthelab.apps.guilelessbopomofo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CandidateTest {

    @Test
    fun construction_withAllParameters() {
        val candidate = Candidate(0, "test", 'a')
        assertEquals(0, candidate.index)
        assertEquals("test", candidate.candidateString)
        assertEquals('a', candidate.selectionKey)
    }

    @Test
    fun defaultSelectionKey_isNullChar() {
        val candidate = Candidate(1, "hello")
        assertEquals('\u0000', candidate.selectionKey)
    }

    @Test
    fun selectionKey_isMutable() {
        val candidate = Candidate(0, "test")
        assertEquals('\u0000', candidate.selectionKey)
        candidate.selectionKey = '5'
        assertEquals('5', candidate.selectionKey)
    }

    @Test
    fun equals_sameValues() {
        val a = Candidate(0, "test", 'a')
        val b = Candidate(0, "test", 'a')
        assertEquals(a, b)
    }

    @Test
    fun equals_differentValues() {
        val a = Candidate(0, "test", 'a')
        val b = Candidate(1, "test", 'a')
        assertNotEquals(a, b)

        val c = Candidate(0, "other", 'a')
        assertNotEquals(a, c)

        val d = Candidate(0, "test", 'b')
        assertNotEquals(a, d)
    }

    @Test
    fun hashCode_consistentWithEquals() {
        val a = Candidate(0, "test", 'a')
        val b = Candidate(0, "test", 'a')
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun copy_preservesFields() {
        val original = Candidate(0, "test", 'a')
        val copied = original.copy()
        assertEquals(original, copied)
    }

    @Test
    fun copy_overridesFields() {
        val original = Candidate(0, "test", 'a')
        val modified = original.copy(index = 5, candidateString = "new", selectionKey = 'z')
        assertEquals(5, modified.index)
        assertEquals("new", modified.candidateString)
        assertEquals('z', modified.selectionKey)
    }

    @Test
    fun toString_includesAllFields() {
        val candidate = Candidate(0, "test", 'a')
        val str = candidate.toString()
        assert(str.contains("0")) { "toString should contain index" }
        assert(str.contains("test")) { "toString should contain candidateString" }
        assert(str.contains("a")) { "toString should contain selectionKey" }
    }
}
