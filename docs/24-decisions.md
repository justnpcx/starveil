# Decisions

Short records of things deliberately **not** built, and of gaps that were
checked and turned out to be closed. Each entry is one conclusion and one
reason, so a future audit does not reopen a question that already has an
answer.

## 2026-11-04 — Competitive gap review

### No fallback or load-balance outbound groups

**Conclusion:** not built, and not planned.

**Why:** sing-box's outbound groups are selector and urltest only; there is no
fallback or load-balance type in the kernel, and the client does not invent
protocol the core cannot execute. Availability is already handled above the
core: urltest picks a healthy node inside a group, and `DropRecovery` switches
after a drop.

### Rule dry run — already present

**Conclusion:** no work needed; the diagnostics page has it.

**Why:** `RouteEvaluator` walks the exact rule list `ConfigBuilder` emits (via
`RuleDryRun`) and reports the first match with its rule text and outbound.

### Subscription override — already present

**Conclusion:** no work needed.

**Why:** per-group subscription fetch options (proxy vs direct, user agent) are
in the group editor, and the config builder reads them at connect time.

### Quick-settings tile and home-screen widget — already present

**Conclusion:** no work needed.

**Why:** `StarveilTileService` and `StarveilWidgetReceiver` are shipped, both
driving `ConnectionController` rather than a second connect path.

### Wi-Fi SSID scenes — already present

**Conclusion:** no work needed.

**Why:** `SceneController` evaluates SSID and mobile-data conditions and applies
a scene's route preset and target; the location permission it needs is declared
and documented.

## 2026-11-01 — Chain proxy (front node) and health-state rulings

### The chain is a one-hop attribute of the exit node

**Conclusion:** built as `Profile.frontNodeKey`. The emitter puts `detour` on the
exit outbound and emits the front under its own tag; a front is never itself
chained.

**Why:** node identity, health and auto-selection are already one system, so an
attribute attaches without inventing a parallel concept. One hop covers the case
and matches the ecosystem norm (chains stay ≤2 hops). `detour` is sing-box's own
semantics for "who dials this outbound"; recursion is a real cost — mihomo ships
a dialer-cycle check for exactly that. Routing, DNS and presets are untouched:
only the proxy outbound's dial path changes. `chainFrontCandidates` refuses a
candidate that is itself chained (`ALREADY_CHAINED`), so the one-hop rule lives
in the model rather than the UI.

### A self or dangling front emits the single-hop config

**Conclusion:** built. `ConfigBuilder` drops a front that equals the exit or no
longer resolves; tests pin the unchanged single-hop output.

**Why:** the emitter is the last line of defence and must never hand the kernel a
config it will reject. A self-detour would also be a loop.

### The node row shows the front on the existing secondary line

**Conclusion:** built as `经 <front> ›`, appended to `group · protocol ·
host:port`.

**Why:** rows are minimum-height. A new line would either create two row heights
in one column (the alignment rule) or raise the whole list for the minority of
chained nodes. Truncation order is fixed: the host segment shortens first and the
"经" segment is kept; an overlong front name is truncated in place
(`FRONT_NAME_MAX`).

### One order everywhere: front → exit

**Conclusion:** built. The node row uses the compact `经 <front>`; the chain sheet
and the home connection card use `<front> → <exit>`.

**Why:** two orders for one relationship would be two mental models.
`DashboardViewModel` pins "front then exit, here and everywhere". Activating a
chain moves neither the ring nor the connection state machine.

### Front references survive refresh by business key

**Conclusion:** built. The reference is `frontNodeKey` (the composite node key);
`preserveFronts` rebinds after a subscription refresh or group rebuild.

**Why:** an index does not survive a refresh, a key does. Without rebinding, every
refresh would silently drop the user's chain.

### A dangling front is cleared with two visible signals

**Conclusion:** built. `ChainReferenceIntegrity.repair()` clears the reference,
writes `WARN chain front invalidated for <exit>` to the log, and emits one
snackbar naming the exit; the node then connects directly.

**Why:** silently changing the dial path would hide a real behaviour change, and a
dangling reference must never reach the emitter. The event is buffered, so a
refresh that happens while the node page is closed still produces its one notice.

### Diagnostics gains one chain line; no other row changes

**Conclusion:** built. With a front, one extra line reports each segment —
`链路：<front> 可达 · <exit> 经前置可达` — and names the failing segment when
one fails. Without a front the line is absent.

**Why:** a chain has two failure points; a single "unreachable" would not say
which. Leaving the rest of the page untouched makes the line additive evidence,
not a redesign.

### Connection details keep reporting the exit

**Conclusion:** unchanged. `outbound` remains the exit; the front is not surfaced
as the active outbound.

**Why:** the front changes only the dial path. Traffic still lands at the exit, and
naming the front as `outbound` would misstate where it egresses.

### Chain acceptance: exit IP unchanged, measurable hop cost, single-hop zero regression

**Conclusion:** the feature is accepted only when all three hold: (1) the exit-IP
check reports the exit's IP and carrier; (2) for the same exit, adding a front
raises measured latency by roughly the front's own hop while the exit IP stays
identical; (3) nodes without a front connect, report latency and auto-select
exactly as in 0.9.16.

**Why:** (1) proves the front changes path, not landing; (2) proves `detour` is
actually in effect rather than silently ignored; (3) proves the feature is
additive.

### FIX-H1 — the status dot reports the latest sample only

**Conclusion:** built. `NodeHealth.status` is `UNTESTED` with no samples, `DOWN`
when the latest sample is unreachable, `UP` otherwise. `band` no longer forces
`DOWN`.

**Why:** the dot mixed "this moment" with "30-minute window reputation". A fresh
30 ms success could sit beside a red "unreachable" because ten failures (weight
100) against one manual success (weight 5) put availability at ~5%; the dot only
cleared when the failures aged out of the window. Availability and band stay on
the health card, where a percentage is honest. A flapping node's dot now follows
the newest sample — which is what "status" claims to mean.

### FIX-H2 — probe failures are split by cause

**Conclusion:** built. `ProbeResult` is `Reached / TimedOut / ResolutionFailed /
ConnectionFailed / Unsupported`. The health store still maps every failure to
`LATENCY_UNREACHABLE`.

**Why:** "DNS gave no address" and "address existed but the node refused" need
different wording and different remediation; collapsing them told the user nothing
actionable. The store only needs "did not answer", so the split stops at the probe
boundary and the storage contract is unchanged.

### The presentation trio shares one source

**Conclusion:** the latency number, the "selected but unreachable" marker and the
timeout grouping all derive from the newest sample. One success clears the marker
and the grouping immediately.

**Why:** after a network switch, a refreshed 30 ms value once sat beside a stale
red marker because the number and the marker had different sources. Three elements
describing one node must not disagree; a success is evidence and must clear the
claim at once rather than wait for the window to expire.
