package com.justnpcx.starveil.feature.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.justnpcx.starveil.core.kernel.TrafficStats
import com.justnpcx.starveil.core.model.NodeHealth
import com.justnpcx.starveil.core.model.displayName
import com.justnpcx.starveil.core.model.RecommendationReason
import com.justnpcx.starveil.core.ui.components.StarveilSnackbarHost
import com.justnpcx.starveil.core.ui.components.ConnectionCore
import com.justnpcx.starveil.core.ui.components.ConnectionCoreGeometry
import com.justnpcx.starveil.core.ui.components.ConnectionState
import com.justnpcx.starveil.core.ui.components.StarveilCard
import com.justnpcx.starveil.core.ui.components.StarveilDivider
import com.justnpcx.starveil.core.ui.theme.StarveilColors
import com.justnpcx.starveil.core.ui.theme.StarveilSpacing
import com.justnpcx.starveil.core.ui.theme.StarveilType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Dashboard — the cockpit. Tap the core to connect/disconnect.
 *
 * The page is a ring, two rates, and two glass cards. Nothing is boxed that
 * does not need an edge: the rates are the page's own type, and the cards hold
 * the things that are objects — the node, and the node's health.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    /**
     * Asks for VPN consent before connecting. Supplied by :app (which owns the
     * activity-result launcher); the default assumes consent is already held,
     * so previews and tests do not need the port.
     */
    requestVpnConsent: (onGranted: () -> Unit) -> Unit = { it() },
    /** The node card opens the list the node is chosen from. */
    onOpenNodes: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onScreenVisible()
                Lifecycle.Event.ON_STOP -> viewModel.onScreenHidden()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onScreenHidden()
        }
    }
    val snackbar = remember { SnackbarHostState() }
    // One-off feedback that is not a state change: the same Snackbar the
    // notices use, raised from a tap rather than from the view model.
    val scope = rememberCoroutineScope()
    // The notice whose details are open, if any. Held here rather than in the
    // view model: it is whether a dialog is showing, not what the app knows.
    var details by remember { mutableStateOf<DashboardNotice?>(null) }
    var exitOpen by remember { mutableStateOf(false) }
    val cannotProbe = stringResource(R.string.dashboard_exit_not_connected)

    // A persistent notice is a condition that lasts until something changes —
    // it gets the fixed slot. Everything else is a one-off confirmation and
    // goes to a Snackbar, so it can never push the layout around.
    val notice = state.notice
    val noticeText = notice?.text()
    val persistent = notice?.isPersistent() == true
    LaunchedEffect(notice) {
        if (noticeText != null && !persistent) {
            snackbar.showSnackbar(noticeText, duration = SnackbarDuration.Short)
        }
    }

    // Every page scrolls, in every orientation.
    //
    // "Portrait never scrolls" was the rule that made a short phone's health
    // card unreachable: the column overflowed, the overflow was clipped, and
    // there was no gesture that could bring it back. A tall screen pays
    // nothing for this — the column is shorter than the window, so the scroll
    // never engages and the first screen is the same pixels it always was.
    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // How much of the fixed stack this window can hold. The ring never
        // changes size — everything else gives a little, in three steps, so a
        // 700dp phone shows the whole page without a scroll while an 872dp one
        // keeps the 0.9.4 composition pixel for pixel.
        val metrics = homeMetricsFor(maxHeight)
        // What the status slot says: a persistent condition, and nothing else.
        // The first-connect hint that used to live here was a second copy of
        // the ring's own "Tap to connect", 24dp below it — the ring already
        // carries the instruction, so the slot now costs nothing until there
        // is something to report.
        val slotHasContent = persistent && noticeText != null
        val slotHeight = if (metrics.collapseEmptySlot && !slotHasContent) 0.dp else metrics.contextSlot
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = StarveilSpacing.pageMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Air above the ring: 32dp when the window can afford it, 12 when
            // it cannot. The ring itself never moves up or down in size.
            Spacer(modifier = Modifier.height(metrics.topGap))
            ConnectionCore(
                state = state.coreState,
                busy = state.isBusy,
                // Fixed in every state: everything below is measured from this
                // box, so a state change must not move any of it.
                diameter = ConnectionCoreGeometry.diameterDp.dp,
                hint = stringResource(
                    when (state.coreState) {
                        ConnectionState.DISCONNECTED -> R.string.dashboard_hint_connect
                        ConnectionState.CONNECTING -> R.string.dashboard_hint_connecting
                        ConnectionState.CONNECTED -> R.string.dashboard_hint_disconnect
                        ConnectionState.BLOCKED -> R.string.dashboard_hint_blocked
                        ConnectionState.WARNING -> R.string.dashboard_hint_disconnect
                    },
                ),
                label = stringResource(
                    when (state.coreState) {
                        ConnectionState.DISCONNECTED -> R.string.dashboard_state_disconnected
                        ConnectionState.CONNECTING -> R.string.dashboard_state_connecting
                        ConnectionState.CONNECTED -> R.string.dashboard_state_connected
                        ConnectionState.BLOCKED -> R.string.dashboard_state_blocked
                        ConnectionState.WARNING -> R.string.dashboard_state_warning
                    },
                ),
                onClick = { requestVpnConsent { viewModel.onCoreClick() } },
                // The core is the screen's primary control, so its description
                // carries both the state and what tapping does (§6.1).
                contentDescription = stringResource(
                    when (state.coreState) {
                        ConnectionState.DISCONNECTED -> R.string.dashboard_core_disconnected
                        ConnectionState.CONNECTING -> R.string.dashboard_core_connecting
                        ConnectionState.CONNECTED -> R.string.dashboard_core_connected
                        ConnectionState.WARNING -> R.string.dashboard_core_warning
                        ConnectionState.BLOCKED -> R.string.dashboard_core_blocked
                    },
                ),
            )

            SpeedSection(state = state, modifier = Modifier.heightIn(min = metrics.telemetry))

            // The context line sits under the group it explains, in a fixed
            // slot, so a state change cannot move anything below it. A failure
            // that names a reason is tappable: the slot shows one line, and the
            // line is the only place the kernel's own words appear.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(slotHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (persistent && noticeText != null) {
                    val expandable = notice is DashboardNotice.KernelRefused
                    Text(
                        text = noticeText,
                        style = StarveilType.label,
                        color = when (notice) {
                            DashboardNotice.KillSwitchBlocking -> StarveilColors.errText
                            else -> StarveilColors.warn
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (expandable) {
                            Modifier
                                .clip(RoundedCornerShape(StarveilSpacing.xs))
                                .clickable { details = notice }
                                .padding(horizontal = StarveilSpacing.xs)
                        } else {
                            Modifier
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(metrics.cardGap))

            // One card, two zones: the node above, the exit below. They were
            // two cards because they were two objects; they are one object now
            // — the connection — and a single edge says so without repeating
            // the card grammar twice on one screen.
            ConnectionCard(
                state = state,
                onOpenNodes = onOpenNodes,
                onOpenDetails = { exitOpen = true },
                onRetry = viewModel::retryExitProbe,
                // With no tunnel there is nothing to probe through: asking
                // would go out directly and overwrite the last session's
                // snapshot with a measurement of the phone's own connection.
                // Say so instead, and leave the snapshot alone.
                onCannotProbe = {
                    scope.launch {
                        snackbar.showSnackbar(
                            cannotProbe,
                            duration = SnackbarDuration.Short,
                        )
                    }
                },
                padding = metrics.cardPadding,
                rowPadding = metrics.rowPadding,
            )

            // The recent rows are content, not navigation: they fill the space
            // under the connection card and each one is a node the app has
            // actually used. With no history the whole group is absent — card
            // and header included — so a first launch never shows an empty
            // shelf.
            if (state.recentNodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(metrics.sectionGap))
                RecentCard(
                    nodes = state.recentNodes,
                    onSelect = viewModel::selectRecentNode,
                    rowPadding = metrics.rowPadding,
                    rowMin = metrics.recentRowMin,
                )
            }

            // Air under the last card, sized by the tier: a tall screen can
            // afford 24dp above the bar, a short one needs the height for the
            // cards instead. This replaces the old xxl + navContentInset pair,
            // which spent 44dp on nothing at the bottom of every phone.
            Spacer(modifier = Modifier.height(metrics.bottomGap))
        }
        }

        StarveilSnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        details?.let { notice ->
            FailureDetailsDialog(notice = notice, onDismiss = { details = null })
        }

        if (exitOpen) {
            state.exit?.let { exit ->
                ExitDetailsDialog(exit = exit, onDismiss = { exitOpen = false })
            }
        }
    }
}

/**
 * The whole failure, and a way to keep it.
 *
 * One line in the context slot is enough to notice something went wrong; it is
 * not enough to act on, and the reason is the part a bug report needs. Copying
 * is the point of this dialog.
 */
@Composable
private fun FailureDetailsDialog(notice: DashboardNotice, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val text = notice.text()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_failure_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm)) {
                Text(text, style = StarveilType.body)
                Text(
                    text = stringResource(R.string.dashboard_failure_hint),
                    style = StarveilType.label,
                    color = StarveilColors.ink2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Starveil", text))
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.dashboard_failure_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dashboard_failure_close))
            }
        },
    )
}

/**
 * What the exit line measured, in full.
 *
 * The number on the home screen is one line and has to stay one line; this is
 * where "which address, how long, and when" lives. The round trip includes DNS
 * and TLS, which is why it is called a round trip and not a ping.
 */
@Composable
private fun ExitDetailsDialog(
    exit: com.justnpcx.starveil.core.data.network.ExitInfo,
    onDismiss: () -> Unit,
) {
    val time = remember(exit.atEpochMs) {
        java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(java.util.Date(exit.atEpochMs))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_exit_details_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm)) {
                DetailLine(stringResource(R.string.dashboard_exit_address), exit.address)
                DetailLine(
                    stringResource(R.string.dashboard_exit_rtt),
                    stringResource(R.string.dashboard_exit_rtt_value, exit.rttMs),
                )
                DetailLine(stringResource(R.string.dashboard_exit_measured), time)
                Text(
                    text = stringResource(R.string.dashboard_exit_note),
                    style = StarveilType.tech,
                    color = StarveilColors.ink3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dashboard_failure_close))
            }
        },
    )
}

/** Which of the exit card's five states is in force. */
internal enum class ExitCardMode { LIVE, NOT_MEASURED, FAILED, LAST, NONE }

/**
 * The matrix, as one function.
 *
 * A live measurement is always the answer. A running tunnel with no answer is
 * either still asking or has failed — two different verdicts. A stopped tunnel
 * shows the last snapshot; with no snapshot it says so.
 */
internal fun exitCardMode(
    connected: Boolean,
    hasLive: Boolean,
    probeFailed: Boolean,
    hasLast: Boolean,
): ExitCardMode = when {
    connected && hasLive -> ExitCardMode.LIVE
    connected && probeFailed -> ExitCardMode.FAILED
    connected -> ExitCardMode.NOT_MEASURED
    hasLast -> ExitCardMode.LAST
    else -> ExitCardMode.NONE
}

/**
 * The exit, in both states, at one height.
 *
 * Three rows always: the address, the round trip, and the session clock. The
 * disconnected card is the last session's snapshot in secondary ink, not a
 * dimmed card. A connected session with no answer yet is its own row text —
 * "not measured" and "probe failed" are different verdicts, and neither one
 * may borrow the previous session's value.
 */
@Composable
private fun ExitZone(state: DashboardUiState, padding: Dp, rowPadding: Dp) {
    val connected = state.connectionState == ConnectionState.CONNECTED ||
        state.connectionState == ConnectionState.WARNING
    val live = state.exit
    val last = state.lastExit
    val mode = exitCardMode(
        connected = connected,
        hasLive = live != null,
        probeFailed = state.exitProbeFailed,
        hasLast = last != null,
    )
    val muted = mode == ExitCardMode.LAST || mode == ExitCardMode.NONE
    val noRecord = stringResource(R.string.dashboard_exit_no_record)
    val ip = when (mode) {
        ExitCardMode.LIVE -> live?.address ?: noRecord
        ExitCardMode.NOT_MEASURED -> stringResource(R.string.dashboard_exit_unmeasured)
        ExitCardMode.FAILED -> stringResource(R.string.dashboard_exit_probe_failed)
        ExitCardMode.LAST -> last?.address ?: noRecord
        ExitCardMode.NONE -> noRecord
    }
    val rtt = when (mode) {
        ExitCardMode.LIVE -> stringResource(
            R.string.dashboard_exit_rtt_value,
            state.exitSmoothedRttMs ?: live?.rttMs ?: 0L,
        )
        ExitCardMode.NOT_MEASURED -> stringResource(R.string.dashboard_exit_unmeasured)
        ExitCardMode.FAILED -> stringResource(R.string.dashboard_exit_probe_failed)
        ExitCardMode.LAST -> stringResource(
            R.string.dashboard_exit_rtt_value,
            last?.rttMs ?: 0L,
        )
        ExitCardMode.NONE -> noRecord
    }
    // The disconnected card's clock belongs to the snapshot, not to the most
    // recent session: a session that never reached an exit must not date an
    // older address.
    val startedAt = if (muted) {
        last?.sessionStartedAt?.takeIf { it > 0 } ?: state.sessionStartedAt
    } else {
        state.sessionStartedAt
    }
    val time = when {
        mode == ExitCardMode.LIVE ||
            mode == ExitCardMode.NOT_MEASURED ||
            mode == ExitCardMode.FAILED ->
            if (startedAt != null) sessionStartedText(startedAt) else noRecord
        startedAt != null -> lastSessionValue(startedAt)
        else -> noRecord
    }
    // A previous session says so in words as well as in grey: colour on its
    // own is not a signal a colour-blind reader can use, and the palette has
    // one secondary grade, so the label carries the state.
    val stale = mode == ExitCardMode.LAST
    Column(modifier = Modifier.fillMaxWidth()) {
        ExitRow(
            label = stringResource(
                if (stale) R.string.dashboard_exit_last_address
                else R.string.dashboard_exit_address,
            ),
            value = ip,
            muted = muted,
            padding = padding,
            rowPadding = rowPadding,
        )
        StarveilDivider(startInset = StarveilSpacing.lg)
        ExitRow(
            label = stringResource(
                if (stale) R.string.dashboard_exit_last_latency
                else R.string.dashboard_exit_rtt_smoothed,
            ),
            value = rtt,
            muted = muted,
            padding = padding,
            rowPadding = rowPadding,
        )
        StarveilDivider(startInset = StarveilSpacing.lg)
        ExitRow(
            label = stringResource(
                if (stale) R.string.dashboard_exit_last_session
                else R.string.dashboard_exit_time,
            ),
            value = time,
            muted = muted,
            padding = padding,
            rowPadding = rowPadding,
        )
    }
}

@Composable
private fun sessionStartedText(atEpochMs: Long): String {
    val clock = sessionClock(atEpochMs)
    return stringResource(
        if (clock.today) {
            R.string.dashboard_exit_session_today
        } else {
            R.string.dashboard_exit_session_date
        },
        clock.text,
    )
}

@Composable
private fun lastSessionValue(atEpochMs: Long): String {
    val clock = sessionClock(atEpochMs)
    return stringResource(
        if (clock.today) {
            R.string.dashboard_exit_last_today_value
        } else {
            R.string.dashboard_exit_last_date_value
        },
        clock.text,
    )
}

private data class SessionClock(val today: Boolean, val text: String)

/** Today is "HH:mm"; any other day keeps the date. */
private fun sessionClock(atEpochMs: Long): SessionClock {
    val zone = ZoneId.systemDefault()
    val time = Instant.ofEpochMilli(atEpochMs).atZone(zone)
    val today = time.toLocalDate() == LocalDate.now(zone)
    val pattern = if (today) "HH:mm" else "MM-dd HH:mm"
    return SessionClock(
        today = today,
        text = time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault())),
    )
}

/** One labelled fact on the exit card: the name on the left, the value right. */
@Composable
private fun ExitRow(
    label: String,
    value: String,
    muted: Boolean = false,
    padding: Dp = StarveilSpacing.lg,
    rowPadding: Dp = StarveilSpacing.md,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding, vertical = rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = StarveilType.label,
            color = StarveilColors.ink2,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(StarveilSpacing.md))
        Text(
            text = value,
            style = StarveilType.tech,
            // Grey is the text's colour, not the card's opacity: a 45% card
            // fade made the latency unreadable on device.
            color = if (muted) StarveilColors.ink2 else StarveilColors.ink,
            // Two lines only when a large font makes one impossible: an
            // ellipsised clock is a clipped clock, and the session time is the
            // row's whole point. At the standard size this never wraps.
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = StarveilType.label, color = StarveilColors.ink2)
        Text(text = value, style = StarveilType.tech, color = StarveilColors.ink)
    }
}

/**
 * A notice as the user reads it.
 *
 * The one place the dashboard's copy lives, so the view model can stay free of
 * it (C1) and the wording can be reviewed with the rest of the app's.
 */
@Composable
private fun DashboardNotice.text(): String = when (this) {
    DashboardNotice.KillSwitchBlocking -> stringResource(R.string.dashboard_notice_kill_switch)
    DashboardNotice.KillSwitchFailed -> stringResource(R.string.dashboard_notice_kill_switch_failed)
    DashboardNotice.TunnelDropped -> stringResource(R.string.dashboard_notice_tunnel_dropped)
    DashboardNotice.TunnelReconnecting -> stringResource(R.string.dashboard_notice_tunnel_reconnecting)
    DashboardNotice.TunnelKeepsDropping ->
        stringResource(R.string.dashboard_notice_tunnel_keeps_dropping)
    DashboardNotice.NoTarget -> stringResource(R.string.dashboard_notice_no_target)
    DashboardNotice.ConfigUnbuildable -> stringResource(R.string.dashboard_notice_config)
    // The kernel's own reason is the useful part; without one, say only that it
    // refused rather than inventing a detail.
    is DashboardNotice.KernelRefused -> reason
        ?.let { stringResource(R.string.dashboard_notice_kernel_refused_reason, it) }
        ?: stringResource(R.string.dashboard_notice_kernel_refused)
    DashboardNotice.SwitchingNode ->
        stringResource(R.string.dashboard_notice_switching_node)
    DashboardNotice.TunnelSilent ->
        stringResource(R.string.dashboard_notice_tunnel_silent)
}

/**
 * The picker's reason, as a clause.
 *
 * A clause rather than a sentence because it is used as the second half of the
 * "auto-selected X — Y" notice.
 */
@Composable
private fun RecommendationReason.text(): String = when (this) {
    is RecommendationReason.Healthiest -> healthClause(availabilityPct, latencyMs)
    is RecommendationReason.TiedOnScore -> region
        ?.let { stringResource(R.string.dashboard_reason_tied_region, healthClause(availabilityPct, latencyMs), it.displayName) }
        ?: stringResource(R.string.dashboard_reason_tied, healthClause(availabilityPct, latencyMs))
    is RecommendationReason.AllCoolingDown ->
        stringResource(R.string.dashboard_reason_cooling_down, minutesLeft)
    RecommendationReason.NothingMeasured ->
        stringResource(R.string.dashboard_reason_unmeasured)
}

/**
 * What the picker saw, in the units the user measures in.
 *
 * The score is a 0–10099 internal number. It is deliberately not shown:
 * "score 10099" is precise about the wrong thing, and the two numbers it is
 * made of are the ones a person can check against the health chart above.
 */
@Composable
private fun healthClause(availabilityPct: Int, latencyMs: Long?): String =
    latencyMs?.let { stringResource(R.string.dashboard_reason_health, availabilityPct, it) }
        ?: stringResource(R.string.dashboard_reason_health_no_latency, availabilityPct)

/**
 * The telemetry block: two columns, mirrored about the screen's centre.
 *
 * Each column is label / number / total, top to bottom, and the three rows line
 * up across the gap: both labels on one baseline, both numbers on the next, both
 * totals on the third. That is what makes the pair read as one instrument
 * rather than as two stacks that happen to be side by side.
 *
 * The unit sits on the number's baseline, not above it.
 *
 * Dimmed as a whole when there is no tunnel: three zeros say "no traffic", a
 * third of the ink says "nothing is connected", and the second is the truth.
 */
@Composable
private fun SpeedSection(state: DashboardUiState, modifier: Modifier = Modifier) {
    val dim = state.coreState == ConnectionState.DISCONNECTED
    val down = formatRateParts(state.stats.downlinkRate)
    val up = formatRateParts(state.stats.uplinkRate)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (dim) 0.38f else 1f),
        verticalAlignment = Alignment.Top,
    ) {
        SpeedColumn(
            label = stringResource(R.string.dashboard_speed_down),
            parts = down,
            total = state.stats.downlinkBytes,
            modifier = Modifier.weight(1f),
        )
        SpeedColumn(
            label = stringResource(R.string.dashboard_speed_up),
            parts = up,
            total = state.stats.uplinkBytes,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SpeedColumn(
    label: String,
    parts: RateParts,
    total: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = StarveilType.tech,
            color = StarveilColors.ink2,
        )
        Row(
            // alignByBaseline on both children, so the unit sits on the digits'
            // baseline whatever size either of them is.
            modifier = Modifier.padding(top = StarveilSpacing.sm),
        ) {
            Text(
                text = parts.value,
                // Tabular figures: a rate that ticks every second must not
                // change width, or the whole row twitches with it.
                style = StarveilType.display.copy(fontFeatureSettings = "tnum"),
                color = StarveilColors.ink,
                // One line, always. A rate that wraps is no longer a rate.
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = parts.unit,
                style = StarveilType.tech.copy(fontSize = 13.sp),
                color = StarveilColors.ink2,
                // "B/s" was breaking after the slash on a narrow column: the
                // unit is one token, not a word the layout may split.
                softWrap = false,
                maxLines = 1,
                modifier = Modifier
                    .alignByBaseline()
                    .padding(start = 6.dp),
            )
        }
        Text(
            text = stringResource(R.string.dashboard_speed_total, formatBytes(total)),
            style = StarveilType.label,
            color = StarveilColors.ink2,
            modifier = Modifier.padding(top = StarveilSpacing.xs),
        )
    }
}

/** A rate split so the number and its unit can be sized independently. */
internal data class RateParts(val value: String, val unit: String)

internal fun formatRateParts(bytesPerSecond: Long): RateParts =
    formatScaledParts(bytesPerSecond.toDouble(), RATE_UNITS)

/**
 * [value] in the largest unit that keeps it readable, with the unit separate.
 *
 * The mantissa never exceeds four characters: the unit is chosen from the
 * **rounded** value, so 999.6 KB becomes 1.0 MB rather than "1000 KB". That is
 * what stops a long number from overlapping its neighbour in a 50% column.
 */
private fun formatScaledParts(value: Double, units: List<String>): RateParts {
    var scaled = value
    for (index in units.indices) {
        val decimals = if (index <= 1) 0 else 1
        val rounded = roundTo(scaled, decimals)
        if (index == units.lastIndex || rounded < SCALE) {
            val text = if (decimals == 0) {
                rounded.toLong().toString()
            } else {
                String.format(Locale.US, "%.1f", rounded)
            }
            return RateParts(text, units[index])
        }
        scaled /= SCALE
    }
    return RateParts(value.toLong().toString(), units.first())
}

/**
 * The connection, as one card with two targets.
 *
 * Above: the node the next connect will use, one line. Below: the exit, three
 * lines, in both states. They are separate buttons because they lead to
 * different places — the node list, and the exit — and each draws a focus ring
 * so a keyboard or a D-pad can see where a press would land.
 */
@Composable
private fun ConnectionCard(
    state: DashboardUiState,
    onOpenNodes: () -> Unit,
    onOpenDetails: () -> Unit,
    onRetry: () -> Unit,
    onCannotProbe: () -> Unit,
    padding: Dp = StarveilSpacing.lg,
    rowPadding: Dp = StarveilSpacing.md,
) {
    val connected = state.connectionState == ConnectionState.CONNECTED ||
        state.connectionState == ConnectionState.WARNING
    StarveilCard(modifier = Modifier.fillMaxWidth()) {
        CardZone(onClick = onOpenNodes) {
            NodeZone(
                name = state.selectedNodeName ?: stringResource(R.string.dashboard_no_node),
                health = state.health,
                padding = padding,
                rowPadding = rowPadding,
            )
        }
        StarveilDivider(startInset = StarveilSpacing.lg)
        // A tap on the exit is about the exit: while the tunnel is up it opens
        // the measurement, or retries one that has not answered. While it is
        // down it says there is nothing to probe — the snapshot below is the
        // last session's, and a probe without a tunnel would replace it with a
        // direct-connection reading. It never promotes the stored snapshot to
        // a live value.
        CardZone(
            onClick = when {
                connected && state.exit == null -> onRetry
                connected -> onOpenDetails
                else -> onCannotProbe
            },
        ) {
            ExitZone(state = state, padding = padding, rowPadding = rowPadding)
        }
    }
}

/**
 * One of the card's two targets.
 *
 * A real button: [clickable] gives it the button role and a focus target, and
 * the ring is drawn only while it holds focus. A box with a click would have
 * neither, and on a D-pad the zone would be invisible.
 */
@Composable
private fun CardZone(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val accent = StarveilColors.accent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .clickable(role = Role.Button, onClick = onClick)
            .drawWithContent {
                drawContent()
                if (focused) {
                    val stroke = 2.dp.toPx()
                    val inset = 3.dp.toPx()
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(stroke),
                    )
                }
            },
        content = content,
    )
}

/**
 * The node, on one line: flag and name left, latency and availability right.
 *
 * The name gives up its width first — it is the only part with an ellipsis —
 * and the numbers never wrap, so a long node name cannot push them off.
 */
@Composable
private fun NodeZone(name: String, health: NodeHealth?, padding: Dp, rowPadding: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding, vertical = rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NodeName(name = name, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(StarveilSpacing.md))
        Text(
            text = healthLine(health),
            style = StarveilType.tech,
            color = StarveilColors.ink2,
            maxLines = 1,
        )
    }
}

/**
 * The recent rows, without a header.
 *
 * The card had a clock glyph and a "Recent" caption, which cost about 28dp of
 * the one screen the page must fit on. The rows are the card's whole content
 * and each one is a node the app has used; the caption named what the rows
 * already are, so it went and the first row now meets the card's top edge.
 */
@Composable
private fun RecentCard(
    nodes: List<RecentNodeUi>,
    onSelect: (Long) -> Unit,
    rowPadding: Dp,
    rowMin: Dp,
) {
    StarveilCard(modifier = Modifier.fillMaxWidth()) {
        nodes.forEachIndexed { index, node ->
            RecentNodeRow(
                node = node,
                onClick = { onSelect(node.profile.id) },
                rowPadding = rowPadding,
                rowMin = rowMin,
            )
            if (index != nodes.lastIndex) {
                StarveilDivider(startInset = StarveilSpacing.lg)
            }
        }
    }
}

/**
 * One recent node, on one line: flag and name left, latency and availability
 * right. The row selects; it does not navigate, so there is no chevron to
 * promise a page that does not exist.
 */
@Composable
private fun RecentNodeRow(
    node: RecentNodeUi,
    onClick: () -> Unit,
    rowPadding: Dp,
    rowMin: Dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = rowMin)
            .padding(horizontal = StarveilSpacing.lg, vertical = rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NodeName(name = node.profile.displayName(), modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(StarveilSpacing.md))
        Text(
            text = healthLine(node.health),
            style = StarveilType.tech,
            color = StarveilColors.ink2,
            maxLines = 1,
        )
    }
}

@Composable
private fun healthLine(health: NodeHealth?): String {
    val ema = health?.emaMs
    return if (ema == null) {
        stringResource(R.string.dashboard_recent_untested)
    } else {
        stringResource(
            R.string.dashboard_recent_health,
            ema,
            health.availabilityPct.roundToInt(),
        )
    }
}

/**
 * A node's name, with a leading flag at 22sp.
 *
 * `Profile.displayName()` puts the flag in the string, and a flag at 17sp reads
 * as punctuation. Splitting it out is a display rule, not a data one: nothing
 * outside this function knows the flag was ever separate.
 */
@Composable
private fun NodeName(name: String, modifier: Modifier = Modifier) {
    val (flag, rest) = splitLeadingFlag(name)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (flag != null) {
            Text(
                text = flag,
                fontSize = 22.sp,
                color = StarveilColors.ink,
            )
            Spacer(modifier = Modifier.width(StarveilSpacing.sm))
        }
        Text(
            text = rest,
            style = StarveilType.headline,
            color = StarveilColors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // The name is the only part of a row allowed to give up width:
            // the numbers beside it must keep theirs.
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

/**
 * Splits a leading pair of regional-indicator symbols off a name.
 *
 * Returns (flag, rest), or (null, name) when the name does not start with one.
 * The pair is two code points; a name that begins with a single one is not a
 * flag and is left alone.
 */
internal fun splitLeadingFlag(name: String): Pair<String?, String> {
    val points = name.codePoints().toArray()
    if (points.size < 3) return null to name
    val first = points[0]
    val second = points[1]
    val isIndicator = { cp: Int -> cp in 0x1F1E6..0x1F1FF }
    if (!isIndicator(first) || !isIndicator(second)) return null to name
    val flag = String(points, 0, 2)
    val rest = String(points, 2, points.size - 2).trim()
    return flag to rest
}

/** One line of state explanation, occupied or not. */
/**
 * What the home page's stack can afford at a given window height.
 *
 * The tiers are stated against the space the page actually gets — the window
 * minus the 56dp top bar and the 80dp navigation bar (and the system insets
 * the shell adds around them). One number is sacred: the ring stays 204dp
 * everywhere, because it is the page's anchor and resizing it would move
 * everything else. The rest — the air above it, the traffic block, the gaps,
 * the cards' padding and their row heights — gives a little at each step.
 *
 * The 360x800 phone lands in COMPACT: five blocks, three recent rows and a
 * 204dp ring do not fit at the comfortable spacing, and the book says to take
 * the difference out of padding and row height rather than out of the ring.
 */
private enum class HomeTier { STANDARD, COMPACT, SHORT }

private data class HomeMetrics(
    val topGap: Dp,
    val telemetry: Dp,
    /** The status slot's height when it has something to say. */
    val contextSlot: Dp,
    /** The gap between the traffic block and the connection card: the largest. */
    val cardGap: Dp,
    /** The gap between the connection card and the recent card. */
    val sectionGap: Dp,
    val cardPadding: Dp,
    /** Air under the last card, before the navigation bar. */
    val bottomGap: Dp,
    /** Vertical padding of a row inside either card. */
    val rowPadding: Dp,
    /** The recent rows' floor, so a row stays a touch target. */
    val recentRowMin: Dp,
    /**
     * Whether an empty status slot collapses to nothing.
     *
     * True below the standard tier: the slot exists so a notice cannot move
     * the ring, and on a screen short of height a 24dp hole with nothing in it
     * is height spent on nothing.
     */
    val collapseEmptySlot: Boolean,
)

private fun homeMetricsFor(contentHeight: Dp): HomeMetrics = when {
    contentHeight >= 640.dp -> HomeMetrics(
        topGap = StarveilSpacing.md,
        telemetry = 84.dp,
        contextSlot = CONTEXT_SLOT_HEIGHT,
        cardGap = 20.dp,
        sectionGap = StarveilSpacing.md,
        cardPadding = StarveilSpacing.lg,
        bottomGap = StarveilSpacing.md,
        rowPadding = 10.dp,
        recentRowMin = 40.dp,
        collapseEmptySlot = true,
    )
    contentHeight >= 560.dp -> HomeMetrics(
        topGap = StarveilSpacing.sm,
        telemetry = 80.dp,
        contextSlot = 20.dp,
        cardGap = 20.dp,
        sectionGap = StarveilSpacing.md,
        cardPadding = StarveilSpacing.lg,
        bottomGap = StarveilSpacing.sm,
        rowPadding = StarveilSpacing.sm,
        recentRowMin = 36.dp,
        collapseEmptySlot = true,
    )
    else -> HomeMetrics(
        topGap = StarveilSpacing.xs,
        telemetry = 76.dp,
        contextSlot = 20.dp,
        cardGap = StarveilSpacing.md,
        sectionGap = StarveilSpacing.xs,
        cardPadding = StarveilSpacing.md,
        bottomGap = StarveilSpacing.xs,
        rowPadding = 6.dp,
        recentRowMin = 34.dp,
        collapseEmptySlot = true,
    )
}

private val CONTEXT_SLOT_HEIGHT = 24.dp

/**
 * A condition that lasts, as opposed to a one-off confirmation.
 *
 * Only these get the slot; everything else is a Snackbar.
 */
private fun DashboardNotice.isPersistent(): Boolean = when (this) {
    // Conditions that last until something changes. A refused start is one:
    // the reason stays true until the next attempt, and it is the only place
    // the kernel's own words appear.
    DashboardNotice.KillSwitchBlocking,
    DashboardNotice.KillSwitchFailed,
    DashboardNotice.NoTarget,
    DashboardNotice.ConfigUnbuildable,
    // Up but carrying nothing: true until traffic arrives or the tunnel goes
    // down, and the whole point is that it must not scroll past.
    DashboardNotice.TunnelSilent,
    is DashboardNotice.KernelRefused,
    -> true

    // One-off: it describes something that is happening, and the state change
    // that ends it clears the notice.
    DashboardNotice.TunnelDropped,
    DashboardNotice.TunnelReconnecting,
    DashboardNotice.TunnelKeepsDropping,
    DashboardNotice.SwitchingNode,
    -> false
}

private val RATE_UNITS = listOf("B/s", "KB/s", "MB/s", "GB/s")
private val BYTE_UNITS = listOf("B", "KB", "MB", "GB")

/** One kibibyte, the step between every pair of units above. */
private const val SCALE = 1024.0

/** [value] rounded to [decimals] places. */
private fun roundTo(value: Double, decimals: Int): Double {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    return round(value * factor) / factor
}

/** A rate with its unit, for copy and for tests. */
internal fun formatRate(bytesPerSecond: Long): String {
    val parts = formatRateParts(bytesPerSecond)
    return parts.value + " " + parts.unit
}

internal fun formatBytes(bytes: Long): String {
    val parts = formatScaledParts(bytes.toDouble(), BYTE_UNITS)
    return "${parts.value} ${parts.unit}"
}
