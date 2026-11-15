package com.justnpcx.starveil.core.data.geo

import kotlinx.coroutines.runBlocking
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * How long a failed lookup is remembered.
 *
 * The old rule was "forever": a host that failed once was never asked again, so
 * a node whose first lookup happened while the phone was offline kept no flag
 * until the app was restarted. The clock is injected here, so the boundary is
 * pinned rather than slept through.
 */
class NodeCountryResolverTest {

    private var now = 1_000_000L
    private var resolves = 0
    private var address: String? = null
    private var country: String? = "US"

    /** The JVM's default locale, restored after every test that changes it. */
    private val originalLocale: Locale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    private val resolver = NodeCountryResolver(
        resolveHost = { resolves++; address },
        lookupCountry = { country },
        nowMs = { now },
    )

    @Test
    fun `a failure is not retried inside the cooldown`() = runBlocking {
        address = null

        assertNull(resolver.countryOf("a.example"))
        now += 59_000
        assertNull(resolver.countryOf("a.example"))

        assertEquals("the lookup was repeated inside the cooldown", 1, resolves)
    }

    @Test
    fun `a failure is retried once the cooldown has passed`() = runBlocking {
        address = null
        resolver.countryOf("a.example")

        now += 60_000
        address = "203.0.113.9"
        country = "JP"

        assertEquals("JP", resolver.countryOf("a.example"))
        assertEquals(2, resolves)
    }

    @Test
    fun `an address with no country is treated as a failure`() = runBlocking {
        address = "203.0.113.9"
        country = null

        assertNull(resolver.countryOf("a.example"))
        now += 59_000
        assertNull(resolver.countryOf("a.example"))
        assertEquals(1, resolves)

        now += 1_000
        country = "SG"
        assertEquals("SG", resolver.countryOf("a.example"))
        assertEquals(2, resolves)
    }

    @Test
    fun `a success is cached for the process, not for the cooldown`() = runBlocking {
        address = "203.0.113.9"
        country = "US"

        assertEquals("US", resolver.countryOf("a.example"))
        now += 10_000_000
        assertEquals("US", resolver.countryOf("a.example"))

        assertEquals(1, resolves)
    }

    @Test
    fun `hosts are cached independently`() = runBlocking {
        address = "203.0.113.9"
        country = "US"
        assertEquals("US", resolver.countryOf("a.example"))

        country = "DE"
        assertEquals("DE", resolver.countryOf("b.example"))
        assertEquals(2, resolves)
    }

    @Test
    fun `a private address takes the device's country`() = runBlocking {
        // GeoIP has nothing to say about 192.168/16 by design. A node there is
        // on this network, so the phone's own region is the honest answer —
        // and the United Nations flag would be a lie about a machine in the
        // next room.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "192.168.1.10"
        country = null

        assertEquals("CN", resolver.countryOf("lan.example"))
    }

    @Test
    fun `loopback and link-local addresses take it too`() = runBlocking {
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        country = null

        address = "127.0.0.1"
        assertEquals("CN", resolver.countryOf("loopback.example"))
        address = "169.254.1.1"
        assertEquals("CN", resolver.countryOf("link-local.example"))
    }

    @Test
    fun `a public address the database does not know stays unknown`() = runBlocking {
        // TEST-NET-3 is public address space with no geo entry. Falling back to
        // the phone's region here would put a foreign node in the user's own
        // country.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "203.0.113.9"
        country = null

        assertNull(resolver.countryOf("unknown.example"))
    }

    @Test
    fun `a locale without a country has no answer to give`() = runBlocking {
        Locale.setDefault(Locale.forLanguageTag("zh"))
        address = "192.168.1.10"
        country = null

        assertNull(resolver.countryOf("language-only.example"))
    }

    @Test
    fun `a three-letter country is not a country code`() = runBlocking {
        // A malformed region must not be written into the profile: the field is
        // an ISO alpha-2 code and the flag is built from it.
        Locale.setDefault(Locale("zh", "CHN"))
        address = "192.168.1.10"
        country = null

        assertNull(resolver.countryOf("bad-region.example"))
    }
}
