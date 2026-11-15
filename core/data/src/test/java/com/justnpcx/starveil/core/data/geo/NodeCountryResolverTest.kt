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
    fun `a host with no answer at all is cooled down and retried`() = runBlocking {
        // A null country is no longer a failure on its own: it falls back to
        // the device's region. The failure path is what is left when the
        // device has no region to give either.
        Locale.setDefault(Locale.forLanguageTag("zh"))
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
    fun `an address the database cannot place takes the device's country`() = runBlocking {
        // TEST-NET-3 has no geo entry. Rather than no flag at all, the node
        // takes the device's region — a guess, and the one this app makes.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "203.0.113.9"
        country = null

        assertEquals("CN", resolver.countryOf("unplaced.example"))
    }

    @Test
    fun `an IPv6 address takes it too`() = runBlocking {
        // The database is IPv4-only, so every IPv6 node lands here. This is the
        // real address a Cloudflare Tunnel home node resolves to.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "2408:8256:2284:127:56d1:96ff:fed5:d317"
        country = null

        assertEquals("CN", resolver.countryOf("home.example"))
    }

    @Test
    fun `a private address takes it as well`() = runBlocking {
        // The original case: a node on the LAN, which GeoIP cannot place by
        // design.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "192.168.1.10"
        country = null

        assertEquals("CN", resolver.countryOf("lan.example"))
    }

    @Test
    fun `a GeoIP answer wins over the device`() = runBlocking {
        // The lookup is still first: a node that can be placed is placed by the
        // database, not by the phone's region.
        Locale.setDefault(Locale.forLanguageTag("zh-CN"))
        address = "8.8.8.8"
        country = "US"

        assertEquals("US", resolver.countryOf("public.example"))
    }

    @Test
    fun `a locale without a country leaves the node unplaced`() = runBlocking {
        // No region to guess with: the node keeps no flag rather than getting a
        // placeholder one.
        Locale.setDefault(Locale.forLanguageTag("zh"))
        address = "203.0.113.9"
        country = null

        assertNull(resolver.countryOf("language-only.example"))
    }

    @Test
    fun `a three-letter region is not a country code`() = runBlocking {
        // A malformed region must not be written into the profile: the field is
        // an ISO alpha-2 code and the flag is built from it.
        Locale.setDefault(Locale("zh", "CHN"))
        address = "203.0.113.9"
        country = null

        assertNull(resolver.countryOf("bad-region.example"))
    }
}
