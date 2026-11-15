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
            // GeoIP has no entry for a private address. That is not a failed
            // lookup, and the honest label for a node on this network is the
            // region this device is set to — not the United Nations flag.
            val local = localCountryOf(address)
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
     * The device's own country, for an address that is not a public one.
     *
     * Only private, loopback and link-local addresses qualify: a public address
     * the database happens not to know is still unknown, and must not be
     * labelled with the phone's region.
     *
     * The value is the system's region setting, not a location — the phone
     * knows the country it was set up for, not where it is. That is why this
     * is a fallback for the one case where "no country" is clearly wrong (the
     * node is on this network) and not a general answer.
     */
    private fun localCountryOf(address: String): String? {
        // The argument is a resolved address, so this is a parse, not a DNS
        // query — and a malformed one simply has no local country.
        val ip = runCatching { InetAddress.getByName(address) }.getOrNull() ?: return null
        val local = ip.isSiteLocalAddress || ip.isLoopbackAddress || ip.isLinkLocalAddress
        if (!local) return null
        return Locale.getDefault().country
            ?.takeIf { code -> code.length == 2 && code.all { it in 'A'..'Z' } }
    }

    private companion object {
        /** How long a failed lookup keeps a host out of the queue. */
        const val FAILURE_RETRY_MS = 60_000L
    }
}
