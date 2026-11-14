package com.justnpcx.starveil.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryFlagsTest {

    @Test
    fun `a code becomes its flag`() {
        assertEquals("🇺🇸", CountryFlags.flag("US"))
        assertEquals("🇭🇰", CountryFlags.flag("hk"))
        assertEquals("🇯🇵", CountryFlags.flag("JP"))
    }

    @Test
    fun `anything that is not a two letter code has no flag`() {
        assertNull(CountryFlags.flag(null))
        assertNull(CountryFlags.flag(""))
        assertNull(CountryFlags.flag("USA"))
        assertNull(CountryFlags.flag("U1"))
    }

    @Test
    fun `the display name carries a flag, and the UN flag when the country is unknown`() {
        val known = Profile(
            name = "HY2+Reality",
            type = ProfileType.VLESS,
            server = "api.example.com",
            port = 443,
            country = "US",
        )
        assertEquals("🇺🇸 HY2+Reality", known.displayName())

        // A node the GeoIP database cannot place gets the United Nations
        // flag, not a bare name: every row starts with a region mark of the
        // same shape and size as the real ones.
        assertEquals("🇺🇳 HY2+Reality", known.copy(country = null).displayName())
        assertEquals("🇺🇳 HY2+Reality", known.copy(country = "not a code").displayName())
    }

    @Test
    fun `the unknown mark is the flag of UN`() {
        // The fallback is a real regional-indicator flag, not a special glyph:
        // this is what keeps it the same shape and size as every country flag.
        assertEquals(CountryFlags.flag("UN"), CountryFlags.UNKNOWN)
    }
}
