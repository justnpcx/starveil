package com.justnpcx.starveil.core.data.geo

import java.net.InetAddress
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Which country a node's server is in.
 *
 * Two steps that can each fail, and both failures are the same answer: no flag.
 * A node whose hostname does not resolve, or whose address is not in the
 * database, is a node the app simply does not label — it still connects.
 *
 * A successful lookup is cached per hostname for the process lifetime, because a
 * subscription of several hundred nodes often points at a handful of hosts. A
 * failure is cached for [FAILURE_RETRY_MS] and then retried: the phone that ran
 * its first lookup offline should not be stuck with unlabelled nodes until the
 * process dies.
 *
 * A private address is not a failure. A node on the LAN — 192.168.1.10, or a
 * home server's name that resolves into the same subnet — is in the room the
 * phone is in, and GeoIP has nothing to say about it by design. Those get the
 * device's own region instead of no answer at all; see [localCountryOf].
 *
 * The caller persists whatever this returns, and the country is then frozen:
 * the field is treated as a GeoIP fact by the country filter, the region
 * grouping and the filter sheet's country list. For a LAN node that fact is
 * really the device's region setting — the honest label for a machine in the
 * same room, but not a lookup result. The alternative (a result type that
 * separates "looked up" from "derived", with only the former persisted) would
 * mean changing the resolver's contract and the view model's write path; this
 * keeps one function and one field, and the trade is written down here.
 */
@Singleton
class NodeCountryResolver(
    private val resolveHost: suspend (String) -> String?,
    private val lookupCountry: (String) -> String?,
    private val nowMs: () -> Long,
    /** Counted as a DNS sample so the cache-hit share is visible next to p50. */
    private val onCacheHit: () -> Unit = {},
) : CountryResolver {

    @Inject
    constructor(geo: GeoIpLookup, dns: DohResolver, metrics: DnsMetrics) : this(
        resolveHost = { host -> dns.resolve(host) },
        lookupCountry = { address -> geo.countryCode(address) },
        nowMs = System::currentTimeMillis,
        onCacheHit = { metrics.record(0L, DnsOutcome.CACHE) },
    )

    private val resolved = ConcurrentHashMap<String, String>()

    /**
     * When a host last failed, so the next attempt can be delayed.
     *
     * A permanent set was the old rule, and it meant a node whose first lookup
     * happened while the phone was offline never got a flag again for the life
     * of the process. The cooldown is long enough that a failing host is not
     * retried on every emission and short enough that coming back online fixes
     * the list.
     */
    private val failedAt = ConcurrentHashMap<String, Long>()

    /** Hosts with a lookup in flight, so concurrent callers share one. */
    private val inFlight = ConcurrentHashMap.newKeySet<String>()

    override suspend fun countryOf(host: String): String? = withContext(Dispatchers.IO) {
        resolved[host]?.let {
            onCacheHit()
            return@withContext it
        }

        val failed = failedAt[host]
        if (failed != null && nowMs() - failed < FAILURE_RETRY_MS) return@withContext null

        // One lookup at a time per host: the node list asks for the same host
        // once per node, and a subscription can hold hundreds of them.
        if (!inFlight.add(host)) return@withContext null
        try {
            // Over DoH, not the system resolver: the hostname is the interesting
            // part of a node, and plain DNS announces it to the local network.
            val address = resolveHost(host)
            if (address == null) {
                failedAt[host] = nowMs()
                return@withContext null
            }
            val country = lookupCountry(address)
            if (country != null) {
                resolved[host] = country
                failedAt.remove(host)
                return@withContext country
            }
            // GeoIP has no entry for an address that cannot be routed. That is
            // not a failed lookup, and the honest label for such a node is the
            // region this device is set to — not the United Nations flag.
            val local = nonRoutableCountryOf(address)
            if (local != null) {
                resolved[host] = local
                failedAt.remove(host)
                return@withContext local
            }
            failedAt[host] = nowMs()
            null
        } finally {
            inFlight.remove(host)
        }
    }

    /**
     * The device's own country, for an address no public route can reach.
     *
     * The covered ranges:
     *  - private, loopback and link-local — RFC 1918, 127/8, 169.254/16, IPv6
     *    ULA, fe80::/10 and ::1 (the JDK's own predicates);
     *  - 100.64.0.0/10 — RFC 6598 shared address space, the range carrier-grade
     *    NAT and Cloudflare Tunnel's older token IPs live in;
     *  - 198.18.0.0/15 — RFC 2544 benchmarking, which proxies also use as the
     *    placeholder answer in fake-IP mode, and which Cloudflare Tunnel's
     *    default token IP comes from.
     *
     * A proxy node whose name resolves into one of these is either a tunnel or
     * a private build: a commercial node has to have a real public address to
     * be reachable at all. A private build is most likely in the same country
     * as the phone, so the device's region is the best guess available — the
     * same inference as the private-address case, with the range completed.
     *
     * The value is the system's region setting, not a location: the phone knows
     * the country it was set up for, not where it is. A public address the
     * database does not know is still unknown and must not be labelled this
     * way, which is why the check is on the address and not on the lookup.
     */
    private fun nonRoutableCountryOf(address: String): String? {
        // The argument is a resolved address, so this is a parse, not a DNS
        // query — and a malformed one simply has no local country.
        val ip = runCatching { InetAddress.getByName(address) }.getOrNull() ?: return null
        if (!ip.isNonRoutable()) return null
        return Locale.getDefault().country
            ?.takeIf { code -> code.length == 2 && code.all { it in 'A'..'Z' } }
    }

    /**
     * True when no public route can reach this address.
     *
     * IPv6 needs nothing beyond the JDK's three predicates: ULA, link-local and
     * loopback are all of the non-routable v6 an app will meet here. The two
     * extra ranges are IPv4-only and are read from the four bytes rather than
     * from string prefixes, so `100.64.0.1` cannot be confused with a hostname
     * that starts the same way.
     */
    private fun InetAddress.isNonRoutable(): Boolean {
        if (isSiteLocalAddress || isLoopbackAddress || isLinkLocalAddress) return true
        val bytes = address
        if (bytes.size != 4) return false
        val first = bytes[0].toInt() and 0xFF
        val second = bytes[1].toInt() and 0xFF
        // RFC 6598, shared address space: 100.64.0.0/10.
        if (first == 100 && second in 64..127) return true
        // RFC 2544, benchmarking: 198.18.0.0/15.
        return first == 198 && second in 18..19
    }

    private companion object {
        /** How long a failed lookup keeps a host out of the queue. */
        const val FAILURE_RETRY_MS = 60_000L
    }
}
