package com.justnpcx.starveil.feature.dashboard

import com.justnpcx.starveil.core.model.CountryFlags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The leading region mark, split off the name it decorates.
 *
 * The dashboard draws the mark as a 14dp badge and the rest as the name, so
 * the split has to survive both kinds of mark the model can produce: a flag
 * (two regional-indicator code points) and the globe fallback (one).
 */
class RegionMarkTest {

    @Test
    fun `a flag is split off with its name`() {
        assertEquals("🇺🇸" to "HY2+Reality", splitRegionMark("🇺🇸 HY2+Reality"))
    }

    @Test
    fun `the globe fallback is a mark too`() {
        assertEquals("🌐" to "npc-home", splitRegionMark("${CountryFlags.UNKNOWN} npc-home"))
    }

    @Test
    fun `a name without a mark is left whole`() {
        assertEquals(null to "npc-home", splitRegionMark("npc-home"))
        // One regional indicator on its own is a letter, not a flag.
        assertEquals(null to "🇺 HY2", splitRegionMark("🇺 HY2"))
    }

    @Test
    fun `a lone globe is not split into an empty name`() {
        val (mark, rest) = splitRegionMark("🌐")
        assertNull(mark)
        assertEquals("🌐", rest)
    }
}
