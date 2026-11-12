package com.justnpcx.starveil.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The recent-node set is the only place a use is written; its codec is pure. */
class RecentNodeUsesTest {

    private val a = "VLESS|a.example|443|A"
    private val b = "VLESS|b.example|443|B"

    @Test
    fun `a use round-trips through its set entry`() {
        val use = RecentNodeUse(a, 1_700_000_000_000L)
        assertEquals(listOf(use), decodeRecentNodeUses(setOf(encodeRecentNodeUse(use))))
    }

    @Test
    fun `a node key with pipes survives`() {
        // nodeKey is type|server|port|name; only the first pipe belongs to the
        // encoding, and the name may contain the rest.
        val use = RecentNodeUse("VLESS|a.example|443|name|with|pipes", 7L)
        assertEquals(listOf(use), decodeRecentNodeUses(setOf(encodeRecentNodeUse(use))))
    }

    @Test
    fun `recording the same node moves it to the front, once`() {
        val merged = mergeRecentNodeUse(
            current = listOf(RecentNodeUse(a, 300), RecentNodeUse(b, 200)),
            nodeKey = b,
            atEpochMs = 400,
        )
        assertEquals(listOf(b, a), merged.map { it.nodeKey })
    }

    @Test
    fun `the history is bounded`() {
        var uses = emptyList<RecentNodeUse>()
        repeat(25) { index ->
            uses = mergeRecentNodeUse(uses, "key-$index", index.toLong(), limit = 20)
        }
        assertEquals(20, uses.size)
        assertEquals("key-24", uses.first().nodeKey)
        assertTrue(uses.none { it.nodeKey == "key-4" })
    }

    @Test
    fun `a malformed entry is dropped, not thrown`() {
        assertEquals(emptyList<RecentNodeUse>(), decodeRecentNodeUses(setOf("nonsense", "123|")))
    }
}
