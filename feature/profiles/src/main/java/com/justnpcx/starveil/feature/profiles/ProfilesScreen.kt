package com.justnpcx.starveil.feature.profiles

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
import com.justnpcx.starveil.core.ui.theme.StarveilBrand
import com.justnpcx.starveil.core.ui.theme.StarveilMotion
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.justnpcx.starveil.core.ui.components.ChainProxySheet
import com.justnpcx.starveil.core.ui.components.LocalTopBarActions
import com.justnpcx.starveil.core.ui.components.StarveilSnackbarHost
import com.justnpcx.starveil.core.ui.components.StarveilAddSheet
import com.justnpcx.starveil.core.ui.components.StarveilTextField
import com.justnpcx.starveil.core.subscription.DocumentFormat
import com.justnpcx.starveil.core.subscription.ImportFailure
import com.justnpcx.starveil.core.subscription.SubscriptionFailure
import com.justnpcx.starveil.core.model.CountryFlags
import com.justnpcx.starveil.core.model.NodeHealth
import com.justnpcx.starveil.core.model.NodeRegions
import com.justnpcx.starveil.core.model.NodeSelectionMode
import com.justnpcx.starveil.core.model.NodeStatus
import com.justnpcx.starveil.core.ui.components.LatencySparkline
import com.justnpcx.starveil.core.ui.components.NodeHealthChart
import com.justnpcx.starveil.core.ui.components.StarveilCard
import com.justnpcx.starveil.core.ui.components.StarveilSkeletonRow
import com.justnpcx.starveil.core.ui.components.ChipSurface
import com.justnpcx.starveil.core.ui.components.StarveilChip
import com.justnpcx.starveil.core.ui.components.StarveilChipRow
import com.justnpcx.starveil.core.ui.components.StarveilDivider
import com.justnpcx.starveil.core.ui.components.StarveilIconAction
import com.justnpcx.starveil.core.ui.components.StarveilSearchField
import com.justnpcx.starveil.core.ui.components.StarveilSwitch
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import com.justnpcx.starveil.core.model.GroupMode
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import com.justnpcx.starveil.core.ui.theme.MonoFamily
import com.justnpcx.starveil.core.ui.theme.StarveilShapes
import com.justnpcx.starveil.core.ui.theme.StarveilType
import com.justnpcx.starveil.core.ui.icons.StarveilIcons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.justnpcx.starveil.core.model.ProbeStage
import com.justnpcx.starveil.core.model.StageNote
import com.justnpcx.starveil.core.model.StageOutcome
import com.justnpcx.starveil.core.model.Profile
import com.justnpcx.starveil.core.model.displayName
import com.justnpcx.starveil.core.model.ProfileType
import com.justnpcx.starveil.core.model.ProxyGroup
import com.justnpcx.starveil.core.ui.components.AppearOnFirstShow
import com.justnpcx.starveil.core.ui.components.EmptyState
import com.justnpcx.starveil.core.ui.components.MessageBanner
import com.justnpcx.starveil.core.ui.components.rememberAppearance
import com.justnpcx.starveil.core.ui.theme.StarveilColors
import com.justnpcx.starveil.core.ui.theme.StarveilSpacing
import com.justnpcx.starveil.core.ui.theme.HubTone
import com.justnpcx.starveil.core.ui.util.readClipboardText
import com.justnpcx.starveil.core.ui.util.writeClipboard

/** How long a transient message stays on screen. */
private const val MESSAGE_VISIBLE_MS = 2_500L

/**
 * Nodes — the real list plus the import entry points. A node arrives by
 * scanning a QR code, pasting from the clipboard, or typing a share link /
 * subscription URL; all three funnel through the same importer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel = hiltViewModel(),
    /**
     * The staged diagnostic. A separate view model because it owns a probe and
     * a result that outlive one row's press, and because nothing it does may
     * touch the list's state.
     */
    probeViewModel: StagedProbeViewModel = hiltViewModel(),
    /** Opens the editor for a brand-new node of the chosen protocol. */
    onNewNode: (ProfileType) -> Unit = {},
    /** Opens the editor for an existing node. */
    onEditNode: (ProfileType, Long) -> Unit = { _, _ -> },
    /** Measures one node by id, and reports what happened. */
    onTestNode: (Long, (String) -> Unit) -> Unit = { _, _ -> },
    /**
     * Measures a batch of nodes and reports what happened.
     *
     * The bar's refresh passes every node; a group's bolt passes that group's.
     * One callback rather than two, because the sweep is the same sweep — only
     * the list handed to it differs.
     */
    onTestNodes: (ids: List<Long>, onResult: (String) -> Unit) -> Unit = { _, _ -> },
    /**
     * A link that arrived from outside the app, to import on arrival.
     *
     * Passed in rather than read here: the inbox lives in :app, and this screen
     * must stay usable without one. [onLinkHandled] is called once it has been
     * taken, so the same link cannot be imported again when the screen is
     * recomposed or revisited.
     */
    incomingLink: String? = null,
    onLinkHandled: () -> Unit = {},
    /** Opens the full-screen scanner. */
    onScan: () -> Unit = {},
    /**
     * Codes the scanner collected. A list, not a string: the scanner stays open
     * across several codes on purpose.
     */
    scannedLinks: List<String> = emptyList(),
    onScannedHandled: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val probeState by probeViewModel.state.collectAsStateWithLifecycle()
    val clipboardSuggestion by viewModel.clipboardSuggestion.collectAsStateWithLifecycle()
    val subscriptionImport by viewModel.lastSubscriptionImport.collectAsStateWithLifecycle()
    val highlightedNode by viewModel.highlightedNode.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    // The second of the two moments the clipboard is read: the page coming back
    // to the foreground. Nothing polls, and Android's own "pasted from" notice
    // is left alone.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.offerClipboard(readClipboardText(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // A share link handed to the app. Keyed on the link so a second one while
    // this screen is open is imported too, and reported through the same
    // message channel as a pasted one — there is no second code path.
    LaunchedEffect(incomingLink) {
        val link = incomingLink ?: return@LaunchedEffect
        viewModel.import(link)
        onLinkHandled()
    }
    // What the scanner collected, imported in one go so a batch reports a
    // batch ("added 3") rather than three separate messages.
    LaunchedEffect(scannedLinks) {
        if (scannedLinks.isEmpty()) return@LaunchedEffect
        viewModel.importMany(scannedLinks.joinToString("\n"))
        onScannedHandled()
    }
    // Group management sits behind a long-press on a chip, because a tap has to
    // stay the filter. Three stages, like the node delete: menu, then the
    // rename or the confirmation.
    var groupMenu by remember { mutableStateOf<ProxyGroup?>(null) }
    var groupToRename by remember { mutableStateOf<ProxyGroup?>(null) }
    var groupToDelete by remember { mutableStateOf<ProxyGroup?>(null) }
    var creatingGroup by remember { mutableStateOf(false) }
    // The node whose group is being changed.
    var movingProfile by remember { mutableStateOf<Profile?>(null) }
    var chainingProfile by remember { mutableStateOf<Profile?>(null) }
    // Long-press a node for its actions. A tap has to stay "select", because
    // that is what the dashboard connects to.
    var rowMenu by remember { mutableStateOf<Profile?>(null) }
    // Whether the timed-out nodes are back in the list. Remembered for the
    // visit, not persisted: the point of folding them is that the next visit is
    // short again.
    var showTimedOut by remember { mutableStateOf(false) }
    // The menu's delete is a tap, not a gesture: confirm it. The swipe keeps
    // its instant delete + undo, because a swipe cannot be a stray tap.
    var nodeToDelete by remember { mutableStateOf<Profile?>(null) }
    // One entry point, one panel. The same button opens the same sheet on the
    // rules and scenes pages; only the sheet's content differs. The old
    // speed-dial of pills was the wrong model: a FAB that fans out is for
    // creating siblings, and importing a node is several channels into one
    // destination.
    var addPanel by remember { mutableStateOf(false) }
    // Set by the empty state's "import a subscription": the panel opens with
    // that row already unfolded.
    var addPanelStartsWithSubscription by remember { mutableStateOf(false) }
    // Read once when the panel opens, never in the background: Android 10+
    // only hands the clipboard to a focused app, and a proxy client has no
    // business polling what the user copied.
    var clipboardText by remember { mutableStateOf<String?>(null) }
    val clipboardPreview = remember(clipboardText) {
        clipboardText?.let { viewModel.preview(it) }?.takeIf { !it.isEmpty }
    }
    var showGroupMenu by remember { mutableStateOf(false) }
    // §5.2. Hoisted out of the LazyColumn so a row scrolled away and back does
    // not replay its entrance.
    val appeared = rememberAppearance()
    // Resolved once per group list rather than per row: a subscription can hold
    // hundreds of nodes, and a linear scan per row would be hundreds of scans
    // per recomposition.
    val groupNames = remember(state.groups) { state.groups.associate { it.id to it.name } }
    // Two nodes can share a display name — a subscription that ships
    // "HY2+Rea…" twice is common — and a truncated title then identifies
    // neither. Only the colliding names get a suffix, so the ordinary row is
    // unchanged: the last label of the host plus the port is short enough to
    // sit beside the name and specific enough to tell the two apart.
    // Only groups that actually hold nodes get a chip. Computed in ViewModel
    // from the unfiltered node pool so selecting a chip does not make other chips vanish.
    // Every folder, empty or not: a group the user just made has to be
    // visible, or creating one looks like it did nothing. Region groups are
    // Two nodes can share a display name, and two more can share the first
    // dozen characters of one — which is what the list actually shows, because
    // the title is one line with an ellipsis. Both cases need a suffix, so the
    // test is on the truncated prefix, over the rows that are on screen.
    val disambiguators = remember(state.profiles) { disambiguate(state.profiles) }

    // The filter sheet, and the groups the user has folded. Both are view state:
    // which filter is in force lives in the view model, but whether a card is
    // open is not something to persist across visits.
    var filterSheet by remember { mutableStateOf(false) }
    val collapsedGroups = remember { mutableStateMapOf<Long, Boolean>() }

    // What the pinned row says about the current target. A group target has no
    // single node, so it names the group; a node target adds its last reading,
    // which is the number the row exists to make visible without opening
    // anything.
    val selectedProfile = remember(state.allProfiles, state.selectedId) {
        state.allProfiles.firstOrNull { it.id == state.selectedId }
    }
    val selectedGroupName = remember(state.groups, state.selectedGroupId) {
        state.groups.firstOrNull { it.id == state.selectedGroupId }?.name
    }
    val currentLabel = selectedGroupName ?: selectedProfile?.name
    val currentMs = selectedProfile
        ?.let { latencyReading(it, state.health[it.id]) as? LatencyReading.Value }
        ?.ms

    // Timed-out nodes are hidden by default: a subscription with hundreds of
    // dead rows is a list you scroll once and never again. The rows are not
    // deleted — a footer says how many are out of the way and brings them back.
    val timedOut = remember(state.profiles, state.health) {
        state.profiles.filter { state.health[it.id]?.status == NodeStatus.DOWN }
    }
    val shownProfiles = remember(state.profiles, state.health, showTimedOut) {
        if (showTimedOut) state.profiles else
            state.profiles.filterNot { state.health[it.id]?.status == NodeStatus.DOWN }
    }

    // The list, grouped: one card per folder or subscription, in the order the
    // groups are stored, plus a card for nodes that belong to none.
    //
    // Region groups are filters, not containers — their members are other
    // groups' nodes, so a card for one would either be empty or show every node
    // twice. They stay reachable in the filter sheet, which is where a derived
    // grouping belongs.
    val searching = state.query.isNotBlank()
    val narrowing = state.filter !is NodeFilter.All
    val sections = remember(state.groups, shownProfiles, searching, narrowing, state.filter) {
        val byGroup = shownProfiles.groupBy { it.groupId }
        val known = state.groups.mapTo(mutableSetOf()) { it.id }
        val owned = state.groups.mapNotNull { group ->
            if (group.region != null) return@mapNotNull null
            val nodes = byGroup[group.id].orEmpty()
            // An empty folder is a place, not noise: a group the user just made
            // has to be visible, or creating one looks like it did nothing. It
            // is only shown while browsing, though — under a filter, an empty
            // card is one more thing to scroll past.
            val keep = nodes.isNotEmpty() ||
                (!searching && (!narrowing || state.filter == NodeFilter.Group(group.id)))
            if (keep) NodeSection(group, nodes) else null
        }
        val loose = shownProfiles.filter { it.groupId !in known }
        if (loose.isEmpty()) owned else owned + NodeSection(null, loose)
    }

    // The countries the list actually holds, so the sheet never offers a choice
    // that would empty it.
    val countries = remember(state.allProfiles) {
        state.allProfiles.mapNotNull { it.country?.uppercase() }.distinct().sorted()
    }

    // Point at the card the import produced: scroll to it, flash it once, then
    // let the highlight go. The list is grouped now, so the index is the card's
    // and a folded group has to open before its row can be seen.
    LaunchedEffect(highlightedNode) {
        val id = highlightedNode ?: return@LaunchedEffect
        val index = sections.indexOfFirst { section -> section.nodes.any { it.id == id } }
        if (index >= 0) {
            collapsedGroups.remove(sections[index].group?.id ?: 0L)
            listState.animateScrollToItem(index + 1)
        }
        delay(HIGHLIGHT_MS)
        viewModel.clearHighlight()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    // A Snackbar instead of a banner above the list: the banner pushed the
    // whole list down every time a message appeared, which is a layout shift in
    // exchange for information that is transient anyway.
    val transientMessage = state.message
        ?.takeIf { it !is ProfilesMessage.NoRegionsFound }
        ?.text()
    LaunchedEffect(transientMessage) {
        if (transientMessage != null) {
            // Short and self-dismissing. A reading like "延迟：210 ms" is not an
            // event to acknowledge, and the host's own timer left it sitting
            // over the add control long after it stopped being news. The
            // dismissal is ours so the timing is the same on every device, and
            // a new message replaces this one rather than queueing behind it.
            launch {
                delay(MESSAGE_VISIBLE_MS)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            snackbarHostState.showSnackbar(
                message = transientMessage,
                duration = SnackbarDuration.Indefinite,
            )
            viewModel.dismissMessage()
        }
    }

    // The list's own actions live in the bar: refresh and the group menu are
    // about this list, and a row of two icons under the title was a second
    // header with nothing in it but the same two buttons.
    var spin by remember { mutableIntStateOf(0) }
    val spinAngle by animateFloatAsState(
        targetValue = spin * 360f,
        animationSpec = tween(REFRESH_SPIN_MS),
        label = "refresh-spin",
    )
    // The bar's content is installed once per top bar, so the ids it sweeps have
    // to be read through a holder rather than captured at that moment.
    val allIds by rememberUpdatedState(state.allProfiles.map { it.id })
    val topBar = LocalTopBarActions.current
    DisposableEffect(topBar) {
        topBar?.content = {
            StarveilIconAction(
                icon = StarveilIcons.Refresh,
                contentDescription = stringResource(R.string.profiles_test_all),
                modifier = Modifier.graphicsLayer { rotationZ = spinAngle },
                onClick = {
                    // One turn of the glyph while the sweep runs, then a line
                    // that says what happened. The turn is the only progress
                    // the bar can show without a second widget.
                    spin += 1
                    onTestNodes(allIds) { message -> viewModel.postMessage(message) }
                },
            )
            Box {
                StarveilIconAction(
                    icon = StarveilIcons.More,
                    contentDescription = stringResource(R.string.profiles_group_actions),
                    onClick = { showGroupMenu = true },
                )
                DropdownMenu(
                    expanded = showGroupMenu,
                    onDismissRequest = { showGroupMenu = false },
                ) {
                    // The plus disc is gone; its one door moves here. The panel
                    // it opens is unchanged — scan, clipboard, manual, share
                    // link and subscription are still the five ways in.
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profiles_add_or_import)) },
                        onClick = {
                            showGroupMenu = false
                            clipboardText = readClipboardText(context)
                            addPanelStartsWithSubscription = false
                            addPanel = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profiles_group_new)) },
                        onClick = {
                            showGroupMenu = false
                            creatingGroup = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profiles_group_by_region)) },
                        onClick = {
                            showGroupMenu = false
                            viewModel.groupByRegion()
                        },
                    )
                }
            }
        }
        onDispose { topBar?.content = null }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        // Transparent: the ambient gradient behind the app is the page.
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        // The shell owns the window insets; a nested Scaffold adding them again
        // would push the list down by a status bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            state.message?.let { message ->
                if (message is ProfilesMessage.NoRegionsFound) {
                    // A dialog, not the banner. "By region" is the one action
                    // whose failure is "nothing appears", so a strip at the top
                    // of the list is exactly the wrong shape for it — the user
                    // tapped a button and watched the list, not the header.
                    AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
                        onDismissRequest = viewModel::dismissMessage,
                        title = { Text(stringResource(R.string.profiles_group_by_region)) },
                        text = { Text(message.text()) },
                        confirmButton = {
                            TextButton(onClick = viewModel::dismissMessage) {
                                Text(stringResource(R.string.profiles_dialog_ok))
                            }
                        },
                    )
                }
            }
            // The pinned row: what "automatic" means, and the way back to it.
            // It replaces the Fastest / By hand segmented control, which taught
            // the wrong thing — that picking a node is a mode you set first.
            // Here the default is stated once, and the row itself is the way
            // back, so a manual pick can never become a one-way door.
            if (state.hasProfiles) {
                AutoRow(
                    mode = selectionMode,
                    currentLabel = currentLabel,
                    currentMs = currentMs,
                    onClick = { viewModel.setSelectionMode(NodeSelectionMode.AUTO) },
                )
                StarveilDivider(startInset = 0.dp)
            }
            // The clipboard bar sits above the list: close to the content it
            // would add. Not while the add panel is up — the panel's own
            // clipboard card is the same import, and two doors to it on one
            // screen is what the panel was rewritten to stop doing.
            if (!addPanel) {
                clipboardSuggestion?.let { suggestion ->
                    ClipboardBar(
                        suggestion = suggestion,
                        onImport = viewModel::importClipboardSuggestion,
                        onDismiss = viewModel::dismissClipboardSuggestion,
                    )
                }
            }
            // One line, once. Long-press is the only way to reach edit, share,
            // move and delete, and nothing on the row says so; a caption that
            // stayed forever would be chrome, so it leaves after the first
            // long-press or the first tap on it.
            if (state.showLongPressHint) {
                Text(
                    text = stringResource(R.string.profiles_long_press_hint),
                    style = StarveilType.label,
                    color = StarveilColors.accentText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { viewModel.dismissNodeHint() }
                        .padding(
                            horizontal = StarveilSpacing.pageMargin,
                            vertical = StarveilSpacing.sm,
                        ),
                )
            }

            // The quota the provider published, when it published one.
            // Filtered to a group: that group's number or nothing. Showing
            // another subscription's quota here would be worse than showing
            // none, because the number looks like it belongs to what is on
            // screen. Unfiltered, the node in force is the one being described.
            val filterGroupId = (state.filter as? NodeFilter.Group)?.id
            val usage = if (filterGroupId != null) {
                state.usage[filterGroupId]
            } else {
                state.usage[state.selectedGroupId]
            }
            if (usage != null) {
                UsageBanner(info = usage)
            }

            if (!state.hasProfiles) {
                // A list with nothing in it says what it is and offers the two
                // ways in, in the middle of the page. "No nodes yet" above an
                // empty white card and a button in the corner is a page that
                // looks broken rather than new.
                NodesEmptyState(
                    onAdd = {
                        clipboardText = readClipboardText(context)
                        addPanelStartsWithSubscription = false
                        addPanel = true
                    },
                    onImport = {
                        clipboardText = readClipboardText(context)
                        addPanelStartsWithSubscription = true
                        addPanel = true
                    },
                )
            } else {
                // The page is the list. The search bar scrolls with it — it is
                // the first row, not a second header — and the list itself is
                // one card per group rather than one card holding everything:
                // a folder is a unit, and its header is where its mode, its
                // count and its own test live.
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = StarveilSpacing.pageMargin,
                        end = StarveilSpacing.pageMargin,
                        top = StarveilSpacing.sm,
                        bottom = StarveilSpacing.lg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(StarveilSpacing.md),
                ) {
                    item(key = "search") {
                        SearchFilterBar(
                            query = state.query,
                            onQueryChange = viewModel::setQuery,
                            onClear = viewModel::clearQuery,
                            filterActive = state.filter !is NodeFilter.All,
                            onOpenFilter = { filterSheet = true },
                        )
                    }
                    if (sections.isEmpty()) {
                        item(key = "empty") {
                            // Two ways to end up with nothing, and they need
                            // different sentences: a search that matched
                            // nothing, and a filter that did.
                            if (searching) {
                                EmptyState(
                                    title = stringResource(R.string.profiles_no_search_title),
                                    body = stringResource(R.string.profiles_no_search_body, state.query),
                                )
                            } else {
                                EmptyState(
                                    title = stringResource(R.string.profiles_no_matches_title),
                                    body = stringResource(R.string.profiles_no_matches_body),
                                )
                            }
                        }
                    }
                    sections.forEach { section ->
                        val key = section.group?.id ?: 0L
                        item(key = "group-$key") {
                            GroupCard(
                                title = section.group?.name
                                    ?: stringResource(R.string.profiles_group_none),
                                // The group's mode is a fact about the group, not
                                // a control: changing it stays in the long-press
                                // menu, where it has always been, and the two
                                // cannot be mistaken for each other.
                                modeLabel = section.group?.let { stringResource(it.mode.labelRes()) },
                                count = section.nodes.size,
                                collapsed = collapsedGroups[key] == true,
                                onToggle = {
                                    collapsedGroups[key] = collapsedGroups[key] != true
                                },
                                onTest = {
                                    onTestNodes(section.nodes.map { it.id }) { message ->
                                        viewModel.postMessage(message)
                                    }
                                },
                                onLongPress = section.group?.let { group ->
                                    {
                                        groupMenu = group
                                        viewModel.dismissNodeHint()
                                    }
                                },
                                // A folder with nothing in it says so, and says
                                // where its own management lives — the generic
                                // "long-press a node" would point at nothing.
                                emptyText = stringResource(R.string.profiles_group_empty_body),
                            ) {
                                section.nodes.forEachIndexed { index, profile ->
                                    // §5.2: rows settle in top-down instead of
                                    // snapping in as a block. `appeared` lives
                                    // out here so a row that scrolls away and
                                    // back does not replay its entrance.
                                    AppearOnFirstShow(
                                        key = profile.id,
                                        index = index,
                                        seen = appeared,
                                    ) {
                                        SwipeToDeleteRow(
                                            onDelete = { viewModel.deleteWithUndo(profile) },
                                        ) {
                                            NodeRow(
                                                profile = profile,
                                                health = state.health[profile.id],
                                                disambiguator = disambiguators[profile.id],
                                                selected = profile.id == state.selectedId,
                                                highlighted = profile.id == highlightedNode,
                                                showDivider = index != section.nodes.lastIndex,
                                                onClick = { viewModel.select(profile.id) },
                                                onLongClick = {
                                                    rowMenu = profile
                                                    viewModel.dismissNodeHint()
                                                },
                                                onTestClick = {
                                                    onTestNode(profile.id) { msg ->
                                                        viewModel.postMessage(msg)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Timed-out nodes are hidden by default: a subscription
                    // with hundreds of dead rows is a list you scroll once and
                    // never again. The rows are not deleted — a footer says how
                    // many are out of the way and brings them back.
                    if (timedOut.isNotEmpty()) {
                        item(key = "timed-out") {
                            Text(
                                text = stringResource(
                                    if (showTimedOut) {
                                        R.string.profiles_timed_out_shown
                                    } else {
                                        R.string.profiles_timed_out_hidden
                                    },
                                    timedOut.size,
                                ),
                                style = StarveilType.label,
                                color = StarveilColors.accentText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { showTimedOut = !showTimedOut },
                                    )
                                    .padding(
                                        horizontal = StarveilSpacing.lg,
                                        vertical = StarveilSpacing.md,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }

        // The Snackbar, pinned above the navigation bar.
        //
        // It used to live in this page's own Scaffold slot. That slot is
        // composed — a plain Text put there draws fine — but the Material
        // SnackbarHost inside it never drew a pixel, while the identical host
        // in the shell (same component, same state class) works. Hosting it
        // here instead of in the slot is the difference that makes the message
        // visible, and it keeps one owner: this page's state, this page's host.
        StarveilSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = StarveilSpacing.navContentInset),
        )
    }

    StarveilAddSheet(
        visible = addPanel,
        onDismissRequest = {
            addPanel = false
            viewModel.clearSubscriptionImport()
        },
    ) {
        ProfilesAddPanel(
            clipboard = clipboardPreview,
            previewOf = viewModel::preview,
            subscriptionImport = subscriptionImport,
            startWithSubscription = addPanelStartsWithSubscription,
            onViewSubscription = { groupId ->
                addPanel = false
                viewModel.clearSubscriptionImport()
                viewModel.filterByGroup(groupId)
            },
            onScan = {
                addPanel = false
                onScan()
            },
            onClipboard = {
                addPanel = false
                clipboardText?.let(viewModel::importMany)
            },
            onManual = {
                addPanel = false
                onNewNode(ProfileType.VLESS)
            },
            onImportLinks = { text ->
                addPanel = false
                viewModel.importMany(text)
            },
            onImportSubscription = { url, name ->
                addPanel = false
                viewModel.importSubscription(url, name)
            },
        )
    }

    if (filterSheet) {
        FilterSheet(
            filter = state.filter,
            groups = state.groups,
            countries = countries,
            onFavorites = viewModel::filterFavorites,
            onCountry = viewModel::filterByCountry,
            onGroup = viewModel::filterByGroup,
            onClear = viewModel::clearFilter,
            onDismiss = { filterSheet = false },
        )
    }

    state.share?.let { target ->
        ShareDialog(target = target, onDismiss = viewModel::dismissShare)
    }


    chainingProfile?.let { captured ->
        // Read through to the live row: the captured value is a snapshot, so a
        // pick would otherwise leave the sheet showing the old chain — the same
        // trap the group sheet had.
        val exit = state.profiles.firstOrNull { it.nodeKey == captured.nodeKey } ?: captured
        ChainProxySheet(
            exit = exit,
            all = state.allProfiles,
            groupNames = state.groups.associate { it.id to it.name },
            onPick = { candidate ->
                viewModel.setFrontNode(exit, candidate?.nodeKey)
                // A pick is the whole interaction: leaving the sheet open would
                // ask the user to close something they are done with.
                chainingProfile = null
            },
            onDismiss = { chainingProfile = null },
        )
    }

    groupMenu?.let { menuGroup ->
        // Read through to the live row: the captured value is a snapshot, so
        // changing the mode would otherwise leave the chips showing the old one.
        val group = state.groups.firstOrNull { it.id == menuGroup.id } ?: menuGroup
        val inUse = state.selectedGroupId == group.id
        val subscription = !group.subscriptionUrl.isNullOrBlank()
        // A sheet, not a dialog. The dialog put four text buttons in the middle
        // of the screen and explained its own constraints in a sentence under a
        // greyed-out button; a sheet is where this page's other menus live, and
        // it has room for the group's identity and its mode in one glance.
        ModalBottomSheet(
            onDismissRequest = { groupMenu = null },
            containerColor = StarveilColors.card,
            shape = StarveilShapes.sheet,
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Identity: the name, what kind of group it is, how many nodes
                // it holds. A domain-shaped name wraps rather than pushing the
                // badge off the sheet.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = StarveilSpacing.pageMargin,
                        end = StarveilSpacing.pageMargin,
                        top = StarveilSpacing.sm,
                    ),
                ) {
                    Text(
                        text = group.name,
                        style = StarveilType.headline,
                        color = StarveilColors.ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(StarveilSpacing.sm))
                    // No weight here: a weight of zero throws, and the badge
                    // is the one thing in this row that should keep its own
                    // width while the name takes what is left.
                    GroupKindBadge(subscription = subscription)
                }
                Text(
                    text = stringResource(R.string.profiles_group_menu_body, group.profileIds.size),
                    style = StarveilType.tech,
                    color = StarveilColors.ink3,
                    modifier = Modifier.padding(
                        start = StarveilSpacing.pageMargin,
                        end = StarveilSpacing.pageMargin,
                        top = StarveilSpacing.xs,
                    ),
                )

                // Mode: the three choices, applied on tap, with one line that
                // describes the choice that is in force. The description is the
                // whole explanation — there is no second paragraph anywhere in
                // this sheet.
                StarveilChipRow(modifier = Modifier.padding(top = StarveilSpacing.md)) {
                    GroupMode.entries.forEach { mode ->
                        StarveilChip(
                            label = stringResource(mode.labelRes()),
                            selected = group.mode == mode,
                            onClick = { viewModel.setGroupMode(group, mode) },
                            surface = ChipSurface.Dialog,
                        )
                    }
                }
                Text(
                    text = stringResource(group.mode.descriptionRes()),
                    style = StarveilType.body,
                    color = StarveilColors.ink2,
                    modifier = Modifier.padding(
                        start = StarveilSpacing.pageMargin,
                        end = StarveilSpacing.pageMargin,
                        top = StarveilSpacing.sm,
                        bottom = StarveilSpacing.md,
                    ),
                )

                // Actions. A folder is not something you connect through, so the
                // row is absent rather than disabled: a dead control plus a
                // sentence explaining why is the dialog's answer, and the mode
                // line above already says what a folder is for.
                if (group.mode != GroupMode.MANUAL && group.profileIds.isNotEmpty()) {
                    StarveilSheetRow(
                        text = stringResource(
                            if (inUse) {
                                R.string.profiles_group_connect_stop
                            } else {
                                R.string.profiles_group_connect
                            },
                        ),
                        icon = StarveilIcons.Activity,
                    ) {
                        if (inUse) viewModel.stopUsingGroup() else viewModel.selectGroup(group)
                        groupMenu = null
                    }
                }
                // Only a subscription-backed group has something to fetch; a
                // folder would just be a button that does nothing.
                if (subscription) {
                    StarveilSheetRow(
                        text = stringResource(R.string.profiles_group_refresh),
                        icon = StarveilIcons.Refresh,
                    ) {
                        groupMenu = null
                        viewModel.refreshSubscription(group)
                    }
                }
                StarveilSheetRow(
                    text = stringResource(R.string.profiles_group_rename),
                    icon = StarveilIcons.Pencil,
                ) {
                    groupMenu = null
                    groupToRename = group
                }

                // Delete stands apart, below a gap, and goes through the same
                // confirmation the rest of the app uses.
                Spacer(modifier = Modifier.height(StarveilSpacing.md))
                StarveilSheetRow(
                    text = stringResource(R.string.profiles_group_delete),
                    icon = StarveilIcons.Close,
                    destructive = true,
                ) {
                    groupMenu = null
                    groupToDelete = group
                }
            }
        }
    }

    // The row's actions, as a sheet rather than a dialog: a dialog puts four
    // text buttons in the middle of the screen, and the sheet is where the rest
    // of this page's menus live. Edit is first because it is what a long-press
    // is usually for.
    rowMenu?.let { profile ->
        // Resolved here rather than from a map: one menu is open at a time, so
        // one lookup is cheaper than keeping a directory for every row.
        val frontName = profile.frontNodeKey
            ?.let { key -> state.allProfiles.firstOrNull { it.nodeKey == key }?.name }
        val chainState = if (frontName == null) {
            stringResource(R.string.profiles_row_chain_unset)
        } else {
            stringResource(
                R.string.profiles_row_chain_via,
                frontName.take(FRONT_NAME_MAX) + if (frontName.length > FRONT_NAME_MAX) "…" else "",
            )
        }
        ModalBottomSheet(
            onDismissRequest = { rowMenu = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = StarveilColors.card,
            // Same top corners as the add sheet. Without this the menu sheet
            // took Material's default 28dp radius and the two sheets on one
            // screen disagreed.
            shape = StarveilShapes.sheet,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                NodeDetailHeader(
                    profile = profile,
                    groupLabel = groupNames[profile.groupId]
                        ?: stringResource(R.string.profiles_group_none),
                    health = state.health[profile.id],
                )
                NodeMenuGroupSeparator()

                // Operations, management, then the one destructive action.
                // Bands separate the groups; individual rows stay text-only.
                NodeMenuRow(stringResource(R.string.profiles_row_edit)) {
                    rowMenu = null
                    onEditNode(profile.type, profile.id)
                }
                NodeMenuRow(stringResource(R.string.profiles_row_probe)) {
                    rowMenu = null
                    probeViewModel.run(profile)
                }
                NodeMenuGroupSeparator()
                NodeMenuRow(
                    stringResource(
                        if (profile.favorite) R.string.profiles_favorite_remove else R.string.profiles_favorite_add,
                    ),
                ) {
                    rowMenu = null
                    viewModel.toggleFavorite(profile)
                }
                NodeMenuRow(stringResource(R.string.profiles_row_share)) {
                    rowMenu = null
                    viewModel.shareNode(profile)
                }
                NodeMenuRow(
                    text = stringResource(R.string.profiles_row_chain),
                    trailing = {
                        Text(
                            text = chainState,
                            style = StarveilType.label,
                            color = StarveilColors.ink3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                ) {
                    rowMenu = null
                    chainingProfile = profile
                }
                NodeMenuRow(stringResource(R.string.profiles_row_move)) {
                    rowMenu = null
                    movingProfile = profile
                }
                NodeMenuGroupSeparator()
                NodeMenuRow(
                    text = stringResource(R.string.profiles_delete, profile.name),
                    destructive = true,
                ) {
                    rowMenu = null
                    nodeToDelete = profile
                }
            }
        }
    }

    StagedProbeDialog(state = probeState, onDismiss = probeViewModel::dismiss)

    nodeToDelete?.let { profile ->
        AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
            onDismissRequest = { nodeToDelete = null },
            title = { Text(stringResource(R.string.profiles_node_delete_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm)) {
                    Text(stringResource(R.string.profiles_node_delete_body, profile.name))
                    val dependents = state.allProfiles.count { it.frontNodeKey == profile.nodeKey }
                    if (dependents > 0) {
                        Text(stringResource(R.string.profiles_delete_front_dependents, dependents))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWithUndo(profile)
                        nodeToDelete = null
                    },
                ) {
                    Text(
                        text = stringResource(R.string.profiles_delete, profile.name),
                        color = StarveilColors.errText,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { nodeToDelete = null }) {
                    Text(stringResource(R.string.profiles_group_cancel))
                }
            },
        )
    }

    groupToRename?.let { group ->
        GroupNameDialog(
            title = stringResource(R.string.profiles_group_rename_title),
            initial = group.name,
            onDismiss = { groupToRename = null },
            onConfirm = { name ->
                viewModel.renameGroup(group, name)
                groupToRename = null
            },
        )
    }

    groupToDelete?.let { group ->
        // Deleting a container is not the same as deleting its contents, so
        // the folder case asks. The two exceptions say why they do not ask:
        // a subscription's nodes cannot outlive their address, and a region
        // group never owned any nodes to begin with.
        val isSubscription = group.subscriptionUrl != null
        val isRegion = group.region != null
        val count = group.profileIds.size
        // Nothing to keep or take: an empty folder has one sensible action.
        val empty = !isSubscription && !isRegion && count == 0
        AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
            onDismissRequest = { groupToDelete = null },
            title = {
                Text(
                    if (isSubscription) {
                        stringResource(R.string.profiles_group_unsubscribe_title, group.name)
                    } else {
                        stringResource(R.string.profiles_group_delete_title_name, group.name)
                    },
                )
            },
            text = {
                Text(
                    when {
                        isSubscription -> stringResource(R.string.profiles_group_unsubscribe_body)
                        isRegion -> stringResource(R.string.profiles_group_delete_region_body)
                        empty -> stringResource(R.string.profiles_group_delete_empty_body)
                        else -> stringResource(R.string.profiles_group_delete_body_count, count)
                    },
                )
            },
            confirmButton = {
                if (isSubscription || isRegion || empty) {
                    TextButton(
                        onClick = {
                            viewModel.deleteGroup(group, deleteProfiles = isSubscription)
                            groupToDelete = null
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.profiles_group_delete),
                            color = StarveilColors.errText,
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.deleteGroup(group, deleteProfiles = true)
                            groupToDelete = null
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.profiles_group_delete_with_nodes, count),
                            color = StarveilColors.errText,
                        )
                    }
                }
            },
            dismissButton = {
                Row {
                    if (!isSubscription && !isRegion && !empty) {
                        TextButton(
                            onClick = {
                                viewModel.deleteGroup(group, deleteProfiles = false)
                                groupToDelete = null
                            },
                        ) {
                            Text(stringResource(R.string.profiles_group_delete_keep))
                        }
                    }
                    TextButton(onClick = { groupToDelete = null }) {
                        Text(stringResource(R.string.profiles_group_cancel))
                    }
                }
            },
        )
    }

    if (creatingGroup) {
        GroupNameDialog(
            title = stringResource(R.string.profiles_group_new_title),
            initial = "",
            onDismiss = { creatingGroup = false },
            onConfirm = { name ->
                viewModel.createGroup(name)
                creatingGroup = false
            },
        )
    }

    movingProfile?.let { profile ->
        AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
            onDismissRequest = { movingProfile = null },
            title = { Text(stringResource(R.string.profiles_group_move_title, profile.name)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm)) {
                    // "No group" first: taking a node back out is as common as
                    // putting it in, and a node from a share link starts there.
                    TextButton(
                        onClick = {
                            viewModel.moveToGroup(profile, null)
                            movingProfile = null
                        },
                    ) {
                        Text(stringResource(R.string.profiles_group_none))
                    }
                    state.groups.forEach { group ->
                        TextButton(
                            onClick = {
                                viewModel.moveToGroup(profile, group.id)
                                movingProfile = null
                            },
                            enabled = group.id != profile.groupId,
                        ) {
                            Text(group.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { movingProfile = null }) {
                    Text(stringResource(R.string.profiles_group_cancel))
                }
            },
        )
    }
}

/** Name entry, shared by "new group" and "rename". */
@Composable
private fun GroupNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            StarveilTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.profiles_group_name_label),
                                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.profiles_group_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profiles_group_cancel))
            }
        },
    )
}

@Composable
private fun ShareDialog(target: ShareTarget, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    var copiedJson by remember { mutableStateOf(false) }

    AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profiles_share_title, target.name)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val link = target.link
                if (link == null) {
                    Text(
                        text = stringResource(R.string.profiles_share_unavailable),
                        style = StarveilType.label,
                        color = StarveilColors.ink2,
                    )
                } else {
                    QrCode(content = link, modifier = Modifier.size(QR_SIZE))
                    Text(
                        text = link,
                        style = StarveilType.label,
                        color = StarveilColors.ink2,
                    )
                    TextButton(
                        onClick = {
                            writeClipboard(context, link, "Starveil node")
                            copied = true
                        },
                    ) {
                        Text(
                            stringResource(
                                if (copied) R.string.profiles_share_copied else R.string.profiles_share_copy,
                            ),
                        )
                    }
                }
                target.json?.let { json ->
                    TextButton(
                        onClick = {
                            writeClipboard(context, json, "Starveil sing-box config")
                            copiedJson = true
                        },
                    ) {
                        Text(
                            stringResource(
                                if (copiedJson) R.string.profiles_share_json_copied
                                else R.string.profiles_share_copy_json,
                            ),
                        )
                    }
                }
                // A link is one node's grammar and a front is a second node,
                // so the front cannot travel with it. Saying that here is the
                // difference between a shared node and a shared node that
                // silently behaves differently.
                if (target.omitsFront) {
                    Text(
                        text = stringResource(R.string.profiles_share_no_front),
                        style = StarveilType.label,
                        color = StarveilColors.ink3,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profiles_group_cancel))
            }
        },
    )
}

@Composable
private fun QrCode(content: String, modifier: Modifier = Modifier) {
    // Encoding a few hundred bytes into a bitmap is not free, and the link does
    // not change while the sheet is open.
    val image = remember(content) {
        runCatching {
            BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, QR_PX, QR_PX)
        }.getOrNull()
    }
    if (image == null) {
        // Too long for a QR code, or the encoder refused. The link is still on
        // screen to copy, so say what happened rather than showing a blank box.
        Text(
            text = stringResource(R.string.profiles_share_no_qr),
            style = StarveilType.label,
            color = StarveilColors.ink2,
        )
        return
    }
    Image(
        bitmap = image.asImageBitmap(),
        contentDescription = stringResource(R.string.profiles_share_qr_description),
        modifier = modifier,
    )
}

/** Rendered at 512px and scaled down, so the code stays crisp when scanned. */
private const val QR_PX = 512
private val QR_SIZE = 200.dp


/**
 * Cheap "could this be a node?" check, used only to decide whether to prefill
 * from the clipboard. Deliberately permissive: anything with a scheme.
 */
private fun looksImportable(text: String): Boolean =
    text.contains("://") || text.startsWith("http", ignoreCase = true)

/**
 * A message as the user reads it.
 *
 * The one place this feature's copy lives, so the view model can stay free of
 * it (C1) and the wording sits with the rest of the app's.
 */
@Composable
private fun ProfilesMessage.text(): String = when (this) {
    is ProfilesMessage.AddedNode -> stringResource(R.string.profiles_msg_added, name)
    is ProfilesMessage.ImportedNodes ->
        if (dropped == 0) {
            stringResource(R.string.profiles_msg_imported, count)
        } else {
            stringResource(R.string.profiles_msg_imported_skipped, count, dropped)
        }
    is ProfilesMessage.Existing ->
        if (name.isBlank()) {
            stringResource(R.string.profiles_msg_exists)
        } else {
            stringResource(R.string.profiles_msg_exists_named, name)
        }
    is ProfilesMessage.ImportedPartially ->
        stringResource(R.string.profiles_msg_imported_partial, count, failures.size)
    is ProfilesMessage.ImportFailed -> stringResource(R.string.profiles_msg_import_failed, failure.text())
    is ProfilesMessage.Refreshed ->
        if (dropped == 0) {
            stringResource(R.string.profiles_msg_refreshed, count)
        } else {
            stringResource(R.string.profiles_msg_refreshed_skipped, count, dropped)
        }
    is ProfilesMessage.RefreshFailed -> stringResource(R.string.profiles_msg_refresh_failed, reason.text())
    is ProfilesMessage.GroupedByRegion -> stringResource(R.string.profiles_msg_grouped, regions)
    is ProfilesMessage.GroupCreated -> stringResource(R.string.profiles_msg_group_created, name)
    is ProfilesMessage.FrontInvalidated -> stringResource(R.string.profiles_front_invalidated, exitName)
    ProfilesMessage.NoRegionsFound -> stringResource(R.string.profiles_msg_no_regions)
    is ProfilesMessage.External -> text
}

/**
 * A failure as the user reads it.
 *
 * The core returns data, not sentences (C1): a line number is useful only if
 * it can be phrased in the reader's language, and a parser message can quote
 * the body — which carries credentials.
 */
@Composable
private fun ImportFailure.text(): String = when (this) {
    ImportFailure.NothingToImport -> stringResource(R.string.profiles_failure_nothing)
    ImportFailure.LinkUnparseable -> stringResource(R.string.profiles_failure_link)
    is ImportFailure.Subscription -> reason.text()
}

@Composable
private fun SubscriptionFailure.text(): String = when (this) {
    SubscriptionFailure.Network -> stringResource(R.string.profiles_failure_network)
    is SubscriptionFailure.Http -> stringResource(R.string.profiles_failure_http, code)
    is SubscriptionFailure.MalformedDocument -> {
        // A local copy: `line` comes from another module and cannot be smart-cast.
        val at = line
        when {
            format == DocumentFormat.YAML && at != null ->
                stringResource(R.string.profiles_failure_yaml_line, at)
            format == DocumentFormat.JSON && at != null ->
                stringResource(R.string.profiles_failure_json_line, at)
            else -> stringResource(R.string.profiles_failure_document)
        }
    }
    SubscriptionFailure.NoUsableNodes -> stringResource(R.string.profiles_failure_no_nodes)
    SubscriptionFailure.SaveFailed -> stringResource(R.string.profiles_failure_save)
}

@OptIn(ExperimentalFoundationApi::class)
/**
 * Swipe left to delete, with the reversal offered in the app's one Snackbar (K2).
 *
 * The delete happens on the gesture rather than after a confirmation: it is
 * reversible, so a dialog in front of it would be asking permission for
 * something that can be taken back. The row leaving is the feedback, and the
 * Snackbar is the way back.
 *
 * This replaced a trailing delete button, which existed because "a node carries
 * credentials and re-importing one is not always possible". That is still true,
 * and it is exactly what the undo answers: the credentials are not lost, they
 * are one tap away.
 */
@Composable
private fun SwipeToDeleteRow(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            // Never let the box *hold* a dismissed state — the row leaves
            // because the list changed, not because this state says so.
            //
            // Returning true here is the obvious version and it is wrong: the
            // state is saveable, keyed by the LazyColumn item key, and the undo
            // restores the node under the *same* id on purpose. So a restored
            // row inherits the dismissed state and comes back as a blank red
            // card with a bin icon — permanent, and it survives navigation.
            // That is exactly what happened on device.
            false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        // Drawn only while the row is actually moving. SwipeToDismissBox keeps
        // the background composed at rest, so a filled background is a red
        // rectangle behind every row — it showed at the ends of the card and
        // tinted the whole list warm.
        backgroundContent = {
            // The target value, not progress and not the offset: progress is
            // non-zero at rest in this version (the row still painted red), and
            // requireOffset() throws when read during composition — it crashed
            // the app on the first frame after this screen opened. targetValue
            // is a plain state read and is Settled until a drag commits.
            if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
                return@SwipeToDismissBox
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StarveilColors.err.copy(alpha = 0.16f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    // Decorative: the row already announces itself, and a
                    // second "Delete <node>" would read it out twice.
                    contentDescription = null,
                    tint = StarveilColors.err,
                    modifier = Modifier.padding(end = StarveilSpacing.lg),
                )
            }
        },
    ) {
        content()
    }
}

/**
 * The node card (§6.3): name, group label, status dot, latency mini-curve and
 * the latency number.
 *
 * A card rather than a dense list row because it has to carry a curve, and a
 * curve beside a name needs the vertical room a row does not have — hence the
 * spec's 72dp minimum.
 */

@Composable
private fun NodeRow(
    profile: Profile,
    health: NodeHealth?,
    /** Non-null only when another row shows the same name. */
    disambiguator: String?,
    selected: Boolean,
    /** True for the row an import just created or found. */
    highlighted: Boolean = false,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTestClick: () -> Unit,
) {
    val reading = latencyReading(profile, health)
    val accent = StarveilColors.accent
    val line = StarveilColors.divider
    val status = health?.status ?: NodeStatus.UNTESTED
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = line,
                        start = Offset(StarveilSpacing.lg.toPx(), size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .background(if (highlighted) StarveilColors.selBg else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.profiles_node_long_press),
            )
            .heightIn(min = NODE_ROW_MIN_HEIGHT)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .then(
                if (selected) {
                    Modifier.border(1.dp, accent, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            // The FAB floats over the list's bottom-right corner; the row keeps
            // its number out from under it.
            .padding(
                start = 10.dp,
                top = 12.dp,
                end = 10.dp,
                bottom = 12.dp,
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // The dot always says how the node is doing; being the current
                // node is a different fact, and it is said at the other end of
                // the row by a check. One mark, one meaning, one place.
                StatusDot(status = status)
                Spacer(modifier = Modifier.width(StarveilSpacing.sm))
                Text(
                    text = profile.displayName(),
                    // Regular weight: the name is an identifier in a list of
                    // identifiers, not a heading. The card's header is the
                    // heading, and the rows under it are its contents.
                    style = StarveilType.body,
                    color = StarveilColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(StarveilSpacing.xs))
                // The protocol, on the first line, for every row: two nodes
                // that share a name are told apart by what they speak as much
                // as by their port, and a reader should not have to open a row
                // to find out which is which.
                MetaBadge(
                    label = profile.type.shortName(),
                    bed = profile.type.protocolTone().bg,
                    ink = profile.type.protocolTextColor(),
                )
                if (disambiguator != null) {
                    Spacer(modifier = Modifier.width(StarveilSpacing.xs))
                    Text(
                        text = disambiguator,
                        style = StarveilType.tech,
                        color = StarveilColors.ink2,
                        maxLines = 1,
                    )
                }
                Spacer(modifier = Modifier.width(StarveilSpacing.sm))
                Row(
                    modifier = Modifier
                        .width(LATENCY_COLUMN_WIDTH)
                        .clip(RoundedCornerShape(LATENCY_TOUCH_CORNER))
                        .clickable(role = Role.Button, onClick = onTestClick)
                        .padding(horizontal = StarveilSpacing.xs),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (reading) {
                        is LatencyReading.Value -> {
                            Text(
                                text = reading.ms.toString(),
                                style = StarveilType.numInline,
                                color = StarveilColors.ink,
                                maxLines = 1,
                            )
                            Text(
                                text = " ms",
                                style = StarveilType.tech,
                                color = StarveilColors.ink2,
                                maxLines = 1,
                            )
                        }
                        LatencyReading.Timeout -> Text(
                            text = stringResource(R.string.profiles_latency_timeout),
                            style = StarveilType.tech,
                            color = StarveilColors.err,
                            maxLines = 1,
                        )
                        LatencyReading.Untested -> Text(
                            text = stringResource(R.string.profiles_latency_untested),
                            style = StarveilType.tech,
                            color = StarveilColors.ink2,
                            maxLines = 1,
                        )
                    }
                }
                // The slot is always there, so the numbers stay in one column
                // whether a row is current or not.
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    if (selected) {
                        Icon(
                            imageVector = StarveilIcons.Check,
                            contentDescription = stringResource(R.string.profiles_node_current),
                            tint = accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(StarveilSpacing.xs))
            // The second line is the address, and nothing else. The group is
            // the card's header, the protocol is the badge on the first line,
            // and the chain is in the long-press menu — three facts that used
            // to be repeated here in one run of grey text. The host is elided
            // in the middle and the port is its own Text so it can never be the
            // part that gets cut: "which port" is what tells two nodes on one
            // host apart.
            val address = middleEllipsis(profile.server, HOST_CHARS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (profile.favorite) {
                    Icon(
                        imageVector = StarveilIcons.StarOutline,
                        contentDescription = stringResource(R.string.profiles_favorites),
                        tint = StarveilColors.ink2,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(StarveilSpacing.xs))
                }
                Text(
                    text = address,
                    style = StarveilType.label,
                    color = StarveilColors.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = ":" + profile.port,
                    style = StarveilType.label,
                    color = StarveilColors.ink2,
                    maxLines = 1,
                )
            }
        }
    }
}
/**
 * The status dot (§6.3): green, grey, red.
 *
 * Carries a description because colour alone is not a signal for anyone using a
 * screen reader — the dot is the only place the window's verdict appears.
 */
/**
 * The top of a node's sheet: who it is, what it speaks, and how it has been.
 *
 * The row can only carry a name, a protocol tag and a number; the sheet is
 * where the same node gets a face. The avatar is the protocol's family colour,
 * the same one the row's badge uses, so the sheet is recognisably the row
 * expanded rather than a second object.
 */
@Composable
private fun NodeDetailHeader(
    profile: Profile,
    groupLabel: String,
    health: NodeHealth?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = StarveilSpacing.pageMargin, vertical = StarveilSpacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProtocolAvatar(label = profile.type.shortName(), tone = profile.type.protocolTone())
            Spacer(modifier = Modifier.width(StarveilSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName(),
                    style = StarveilType.headline,
                    color = StarveilColors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(StarveilSpacing.xs))
                // The avatar already says the protocol; this line says which
                // group the node belongs to, so the two facts are not printed
                // twice in two centimetres.
                MetaBadge(
                    label = groupLabel,
                    bed = StarveilColors.segBg,
                    ink = StarveilColors.ink2,
                )
            }
        }
        Spacer(modifier = Modifier.height(StarveilSpacing.md))
        Text(
            text = "${profile.server}:${profile.port}",
            style = StarveilType.tech,
            color = StarveilColors.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (health != null) {
            Spacer(modifier = Modifier.height(StarveilSpacing.md))
            Text(
                text = detailHealthLine(profile, health),
                style = StarveilType.label,
                color = StarveilColors.ink2,
            )
            Spacer(modifier = Modifier.height(StarveilSpacing.sm))
            NodeHealthChart(
                health = health,
                contentDescription = stringResource(
                    R.string.profiles_detail_chart_desc,
                    profile.displayName(),
                ),
                height = 88.dp,
            )
        }
    }
}

/** The status and the last reading, in one line. */
@Composable
private fun detailHealthLine(profile: Profile, health: NodeHealth): String {
    val status = when (health.status) {
        NodeStatus.UP -> stringResource(R.string.profiles_status_up)
        NodeStatus.DOWN -> stringResource(R.string.profiles_status_down)
        NodeStatus.UNTESTED -> stringResource(R.string.profiles_status_untested)
    }
    val reading = when (val value = latencyReading(profile, health)) {
        is LatencyReading.Value -> "${value.ms} ms"
        LatencyReading.Timeout -> stringResource(R.string.profiles_latency_timeout)
        LatencyReading.Untested -> stringResource(R.string.profiles_latency_untested)
    }
    return "$status · $reading"
}

/** The protocol as a 56dp tile, for the sheet's header. */
@Composable
private fun ProtocolAvatar(label: String, tone: HubTone) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tone.bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = StarveilType.headline.copy(fontSize = 15.sp),
            color = StarveilColors.ink,
            maxLines = 1,
        )
    }
}

/** A small metadata tag: protocol family on a row, group on a sheet. */
@Composable
private fun MetaBadge(label: String, bed: Color, ink: Color) {
    Text(
        text = label,
        style = StarveilType.tech.copy(fontSize = 10.sp, letterSpacing = 0.4.sp),
        color = ink,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bed)
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

/**
 * The badge's text colour.
 *
 * The family's own ink is a 3:1 glyph colour; at 10sp the badge is text and
 * needs 4.5:1. Three of the four pass as they are; the amber one uses the
 * warning tone, which is the same hue family at a text-safe value.
 */
@Composable
private fun ProfileType.protocolTextColor(): Color = when (this) {
    ProfileType.WIREGUARD -> StarveilColors.warn
    ProfileType.OTHER -> StarveilColors.ink2
    else -> protocolTone().ink
}

/**
 * A protocol's family colour.
 *
 * The badge is a wayfinding mark, not decoration: four families a reader can
 * learn in one glance. The mapping is semantic rather than hashed, so adding a
 * node never changes the colour of the nodes beside it.
 */
@Composable
private fun ProfileType.protocolTone(): HubTone = when (this) {
    // TLS-shaped proxies.
    ProfileType.VLESS, ProfileType.VMESS, ProfileType.TROJAN -> StarveilColors.hubLogs
    // The legacy stream-cipher family.
    ProfileType.SHADOWSOCKS -> StarveilColors.hubScenes
    // QUIC / UDP transports.
    ProfileType.HYSTERIA2, ProfileType.TUIC -> StarveilColors.hubDiagnostics
    // Everything else that is still a tunnel.
    ProfileType.WIREGUARD -> StarveilColors.hubSettings
    ProfileType.OTHER -> HubTone(bg = StarveilColors.segBg, ink = StarveilColors.ink2)
}

@Composable
private fun StatusDot(status: NodeStatus, modifier: Modifier = Modifier) {
    val description = when (status) {
        NodeStatus.UP -> stringResource(R.string.profiles_status_up)
        NodeStatus.DOWN -> stringResource(R.string.profiles_status_down)
        NodeStatus.UNTESTED -> stringResource(R.string.profiles_status_untested)
    }
    // Untested is hollow: a filled grey dot reads as a verdict, and "nobody has
    // measured this" is the absence of one.
    if (status == NodeStatus.UNTESTED) {
        Box(
            modifier = modifier
                .size(STATUS_DOT_SIZE)
                .clip(CircleShape)
                .border(1.5.dp, StarveilColors.ringOff, CircleShape)
                .semantics { contentDescription = description },
        )
        return
    }
    Box(
        modifier = modifier
            .size(STATUS_DOT_SIZE)
            .clip(CircleShape)
            .background(if (status == NodeStatus.UP) StarveilColors.accent else StarveilColors.err)
            .semantics { contentDescription = description },
    )
}

/** What the card's latency number says, and whether it is a real reading. */
internal sealed interface LatencyReading {
    data class Value(val ms: Long) : LatencyReading

    /** The last probe did not come back. */
    data object Timeout : LatencyReading

    /** Nothing has been measured, by the sampler or by hand. */
    data object Untested : LatencyReading
}

/**
 * Which number the card shows.
 *
 * The curve's own value wins when there is one, so the number and the line
 * cannot disagree. A failed *latest* probe takes precedence over both: the
 * smoothed value is still meaningful, but the card is reporting "right now",
 * and "right now" is that the node did not answer.
 *
 * `Profile.latencyMs` is the fallback, not the first choice. It is what the
 * manual "test latency" button writes, and it is the only reading a node has
 * until the sampler has collected a few — showing "untested" next to a node the
 * user just measured would be a lie.
 */
internal fun latencyReading(profile: Profile, health: NodeHealth?): LatencyReading {
    // Read once into a local: `NodeHealth` is in another module, so its
    // properties cannot be smart-cast after a null check.
    val ema = health?.emaMs
    return when {
        health?.latest?.reachable == false -> LatencyReading.Timeout
        ema != null -> LatencyReading.Value(ema)
        profile.latencyMs >= 0 -> LatencyReading.Value(profile.latencyMs)
        else -> LatencyReading.Untested
    }
}

/** §6.3 card metrics. */
private val NODE_ROW_MIN_HEIGHT = 64.dp

/** How many placeholders fill the card while the list is being read. */
private const val SKELETON_ROWS = 5

/**
 * One card's worth of the list: a group, or no group at all, and the nodes shown
 * under it.
 *
 * A value rather than grouping inside the LazyColumn's item lambda: the list's
 * shape is then computed once per state change, and the highlight scroll can ask
 * where a node is without a second traversal.
 */
private data class NodeSection(val group: ProxyGroup?, val nodes: List<Profile>)
private val SELECTED_BORDER_WIDTH = 2.dp
private val STATUS_DOT_SIZE = 8.dp
private val SPARKLINE_WIDTH = 64.dp
private val SPARKLINE_HEIGHT = 20.dp

/** §2.1's "GlowPrimaryDim": the halo a selected card is lit with. */
private const val SELECTED_HALO_ALPHA = 0.16f

/**
 * The pinned row: automatic selection, and the way back to it.
 *
 * It replaces the Fastest / By hand segmented control. The control made the
 * choice of a node look like a mode to set before picking one, and it left the
 * default — the state almost everyone stays in — with no line of its own. This
 * row states the default, names what it picked, and is itself the way back:
 * tapping it in manual returns to automatic and picks immediately.
 */
@Composable
private fun AutoRow(
    mode: NodeSelectionMode,
    currentLabel: String?,
    currentMs: Long?,
    onClick: () -> Unit,
) {
    val auto = mode == NodeSelectionMode.AUTO
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = StarveilSpacing.pageMargin, vertical = StarveilSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = StarveilIcons.Bolt,
            contentDescription = null,
            tint = if (auto) StarveilColors.accent else StarveilColors.ink2,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(StarveilSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profiles_auto_title),
                style = StarveilType.body,
                color = StarveilColors.ink,
                maxLines = 1,
            )
            Text(
                text = when {
                    !auto -> stringResource(R.string.profiles_auto_manual)
                    currentLabel == null -> stringResource(R.string.profiles_auto_current_none)
                    currentMs != null -> stringResource(
                        R.string.profiles_auto_current,
                        currentLabel,
                        currentMs,
                    )
                    else -> stringResource(R.string.profiles_auto_current_plain, currentLabel)
                },
                style = StarveilType.label,
                color = StarveilColors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (auto) {
            Icon(
                imageVector = StarveilIcons.Check,
                contentDescription = stringResource(R.string.profiles_auto_on),
                tint = StarveilColors.accent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * The one bar that narrows the list: search, and the filter at its end.
 *
 * One field rather than a search box with a second control beside it: both do
 * the same job to the same list, and two rounded rectangles side by side read
 * as two unrelated things. The funnel carries a dot when a filter is on, so the
 * list cannot be narrowed by something invisible.
 */
@Composable
private fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    filterActive: Boolean,
    onOpenFilter: () -> Unit,
) {
    StarveilSearchField(
        value = query,
        onValueChange = { next -> if (next.isEmpty()) onClear() else onQueryChange(next) },
        placeholder = stringResource(R.string.profiles_search_hint),
        trailing = {
            Box {
                IconButton(onClick = onOpenFilter, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = StarveilIcons.Filter,
                        contentDescription = stringResource(
                            if (filterActive) {
                                R.string.profiles_filter_active
                            } else {
                                R.string.profiles_filter_open
                            },
                        ),
                        tint = if (filterActive) StarveilColors.accent else StarveilColors.ink2,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (filterActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                            .size(6.dp)
                            .background(StarveilColors.accent, CircleShape),
                    )
                }
            }
        },
    )
}

/**
 * One group, as a card: a header, then its rows.
 *
 * The header is the group's identity and its facts in one line — name, mode,
 * count — plus the two things you can do without opening the menu: fold it, or
 * measure its nodes. The mode is a label, not a control: changing it stays in
 * the long-press menu, where it has always been, and the two shapes cannot be
 * mistaken for each other.
 */
@Composable
private fun GroupCard(
    title: String,
    modeLabel: String?,
    count: Int,
    collapsed: Boolean,
    onToggle: () -> Unit,
    onTest: () -> Unit,
    onLongPress: (() -> Unit)?,
    emptyText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    StarveilCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .combinedClickable(
                    onClick = onToggle,
                    onLongClick = onLongPress,
                    role = Role.Button,
                )
                .padding(
                    start = StarveilSpacing.lg,
                    end = StarveilSpacing.sm,
                    top = StarveilSpacing.xs,
                    bottom = StarveilSpacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = StarveilType.headline,
                color = StarveilColors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (modeLabel != null) {
                Text(
                    text = modeLabel.uppercase(),
                    style = StarveilType.tech,
                    color = StarveilColors.ink2,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(StarveilSpacing.sm))
            }
            Text(
                text = count.toString(),
                style = StarveilType.tech,
                color = StarveilColors.ink2,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(StarveilSpacing.xs))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button, onClick = onTest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = StarveilIcons.Bolt,
                    contentDescription = stringResource(R.string.profiles_group_test),
                    tint = StarveilColors.accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Icon(
                imageVector = if (collapsed) StarveilIcons.Down else StarveilIcons.Up,
                contentDescription = stringResource(
                    if (collapsed) {
                        R.string.profiles_group_expand
                    } else {
                        R.string.profiles_group_collapse
                    },
                ),
                tint = StarveilColors.ink2,
                modifier = Modifier.size(18.dp),
            )
        }
        if (!collapsed) {
            StarveilDivider(startInset = StarveilSpacing.lg)
            if (count == 0) {
                Text(
                    text = emptyText,
                    style = StarveilType.label,
                    color = StarveilColors.ink2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = StarveilSpacing.lg,
                            vertical = StarveilSpacing.md,
                        ),
                )
            }
            content()
        }
    }
}

/**
 * The filter sheet.
 *
 * A sheet rather than a row of chips on the page: the chips mixed favorites,
 * "all", countries and subscription names in one horizontal strip that could
 * not show what it held. Here each kind is a section, and the funnel that opens
 * it is the only thing the page has to carry.
 *
 * A filter is still one value, not a set of switches — favorites, a country and
 * a group are alternatives. Choosing one in any section replaces what the
 * others had, which is why the favorites switch turns itself off when a country
 * is picked.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    filter: NodeFilter,
    groups: List<ProxyGroup>,
    countries: List<String>,
    onFavorites: (Boolean) -> Unit,
    onCountry: (String?) -> Unit,
    onGroup: (Long?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarveilColors.card,
        shape = StarveilShapes.sheet,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = StarveilSpacing.lg),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = StarveilSpacing.pageMargin, end = StarveilSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profiles_filter_title),
                    style = StarveilType.headline,
                    color = StarveilColors.ink,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClear, enabled = filter !is NodeFilter.All) {
                    Text(
                        text = stringResource(R.string.profiles_filter_clear),
                        style = StarveilType.label,
                        color = if (filter is NodeFilter.All) {
                            StarveilColors.ink3
                        } else {
                            StarveilColors.accentText
                        },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Switch) {
                        onFavorites(filter !is NodeFilter.Favorites)
                    }
                    .padding(
                        start = StarveilSpacing.pageMargin,
                        end = StarveilSpacing.md,
                        top = StarveilSpacing.xs,
                        bottom = StarveilSpacing.xs,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profiles_filter_favorites),
                    style = StarveilType.body,
                    color = StarveilColors.ink,
                    modifier = Modifier.weight(1f),
                )
                StarveilSwitch(
                    checked = filter is NodeFilter.Favorites,
                    onCheckedChange = onFavorites,
                )
            }
            if (countries.isNotEmpty()) {
                FilterSection(stringResource(R.string.profiles_filter_country))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StarveilSpacing.pageMargin),
                    horizontalArrangement = Arrangement.spacedBy(StarveilSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm),
                ) {
                    countries.forEach { iso ->
                        val selected = filter == NodeFilter.Country(iso)
                        StarveilChip(
                            // The code, not a translated country name: the name
                            // is the locale's business and the flag already says
                            // which country this is.
                            label = CountryFlags.flag(iso)?.let { "$it $iso" } ?: iso,
                            selected = selected,
                            onClick = { onCountry(if (selected) null else iso) },
                            surface = ChipSurface.Dialog,
                        )
                    }
                }
            }
            if (groups.isNotEmpty()) {
                FilterSection(stringResource(R.string.profiles_filter_groups))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StarveilSpacing.pageMargin),
                    horizontalArrangement = Arrangement.spacedBy(StarveilSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm),
                ) {
                    groups.forEach { group ->
                        val selected = filter == NodeFilter.Group(group.id)
                        StarveilChip(
                            label = group.name,
                            selected = selected,
                            onClick = { onGroup(if (selected) null else group.id) },
                            surface = ChipSurface.Dialog,
                        )
                    }
                }
            }
        }
    }
}

/** A section caption inside the filter sheet. */
@Composable
private fun FilterSection(text: String) {
    Text(
        text = text.uppercase(),
        style = StarveilType.tech,
        color = StarveilColors.ink2,
        modifier = Modifier.padding(
            start = StarveilSpacing.pageMargin,
            end = StarveilSpacing.pageMargin,
            top = StarveilSpacing.lg,
            bottom = StarveilSpacing.sm,
        ),
    )
}

/** Tap target padding around the latency badge. */
private val LATENCY_TOUCH_CORNER = 8.dp

/**
 * The latency column: wide enough for "1023 ms" at the inline size and for
 * "timeout" in the mono face, so neither the unit nor the word is ever clipped.
 */
private val LATENCY_COLUMN_WIDTH = 76.dp

/** The FAB's footprint (56dp) plus its margin from the screen edge (16dp). */

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = StarveilType.label,
        color = StarveilColors.ink2,
    )
}

private fun GroupMode.labelRes(): Int = when (this) {
    GroupMode.MANUAL -> R.string.profiles_group_mode_folder
    GroupMode.SELECTOR -> R.string.profiles_group_mode_selector
    GroupMode.URL_TEST -> R.string.profiles_group_mode_urltest
}

/** One line describing the mode in force. The sheet's only explanation. */
private fun GroupMode.descriptionRes(): Int = when (this) {
    GroupMode.MANUAL -> R.string.profiles_group_mode_folder_desc
    GroupMode.SELECTOR -> R.string.profiles_group_mode_selector_desc
    GroupMode.URL_TEST -> R.string.profiles_group_mode_urltest_desc
}

/**
 * The group's kind, as a badge.
 *
 * Subscription-backed and local groups behave differently — one refreshes from
 * an address, the other is a folder someone made — and the sheet is the only
 * place that difference is worth naming.
 */
@Composable
private fun GroupKindBadge(subscription: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (subscription) StarveilColors.selBg else StarveilColors.chipB,
        border = BorderStroke(1.dp, if (subscription) StarveilColors.selBg else StarveilColors.chipB),
    ) {
        Text(
            text = stringResource(
                if (subscription) {
                    R.string.profiles_group_kind_subscription
                } else {
                    R.string.profiles_group_kind_local
                },
            ),
            style = StarveilType.label,
            color = if (subscription) StarveilColors.selInk else StarveilColors.ink2,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = StarveilSpacing.sm,
                vertical = StarveilSpacing.xs,
            ),
        )
    }
}

private fun ProfileType.editorLabel(): String = when (this) {
    ProfileType.VLESS -> "VLESS"
    ProfileType.VMESS -> "VMess"
    ProfileType.TROJAN -> "Trojan"
    ProfileType.SHADOWSOCKS -> "Shadowsocks"
    ProfileType.HYSTERIA2 -> "Hysteria2"
    ProfileType.TUIC -> "TUIC"
    ProfileType.WIREGUARD -> "WireGuard"
    ProfileType.OTHER -> "Node"
}

/**
 * One line in a sheet: a title, a tap, and a hairline above it.
 *
 * 56dp tall with the label at headline weight — a menu item is a sentence you
 * read, not a paragraph you parse, and the previous text buttons were neither.
 */
@Composable
private fun StarveilSheetRow(
    text: String,
    destructive: Boolean = false,
    /**
     * A glyph that says what the row does before the label does.
     *
     * Tinted by meaning, not by decoration: destructive rows carry the error
     * colour, the rest the accent, so a row can be recognised by shape and
     * colour at the edge of the sheet.
     */
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    showDivider: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val tint = if (destructive) StarveilColors.errText else StarveilColors.accent
    Column {
        if (showDivider) {
            StarveilDivider(startInset = StarveilSpacing.pageMargin, endInset = StarveilSpacing.pageMargin)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = StarveilSpacing.pageMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(StarveilSpacing.md))
            }
            Text(
                text = text,
                style = StarveilType.headline,
                color = if (destructive) StarveilColors.errText else StarveilColors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Spacer(modifier = Modifier.width(StarveilSpacing.sm))
                trailing()
            }
        }
    }
}

@Composable
private fun NodeMenuRow(
    text: String,
    destructive: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    StarveilSheetRow(
        text = text,
        destructive = destructive,
        showDivider = false,
        trailing = trailing,
        onClick = onClick,
    )
}

@Composable
private fun NodeMenuGroupSeparator() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(StarveilSpacing.sm)
            .background(StarveilColors.bg),
    )
}

/** One line in the add sheet. */
@Composable
private fun AddSheetRow(title: String, onClick: () -> Unit) {
    StarveilSheetRow(text = title, onClick = onClick)
}

/**
 * The quota the subscription reported, when the provider reports one.
 *
 * A line, not a card: it is a fact about the subscription, not a section. It
 * disappears entirely when the header was missing or unreadable — a "no data"
 * banner would be a permanent apology for something the user cannot act on.
 */
@Composable
private fun UsageBanner(
    info: com.justnpcx.starveil.core.model.SubscriptionUserInfo,
    modifier: Modifier = Modifier,
) {
    val days = info.daysLeft(System.currentTimeMillis() / 1000L)
    val parts = buildList {
        val used = info.usedBytes
        val total = info.totalBytes
        when {
            used != null && total != null ->
                add(stringResource(R.string.profiles_usage_of, formatBytes(used), formatBytes(total)))
            used != null -> add(stringResource(R.string.profiles_usage_used, formatBytes(used)))
            total != null -> add(stringResource(R.string.profiles_usage_total, formatBytes(total)))
        }
        if (days != null) add(stringResource(R.string.profiles_usage_days, days))
    }
    if (parts.isEmpty()) return
    Text(
        text = parts.joinToString(" · "),
        style = StarveilType.label,
        color = StarveilColors.ink2,
        modifier = modifier.padding(
            start = StarveilSpacing.pageMargin,
            end = StarveilSpacing.pageMargin,
            top = StarveilSpacing.sm,
        ),
    )
}

/** Bytes in the shortest unit that keeps one decimal. */
private fun formatBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0 || value >= 100) {
        "${value.toLong()} ${units[unit]}"
    } else {
        String.format(java.util.Locale.US, "%.1f %s", value, units[unit])
    }
}

/**
 * The staged test's result.
 *
 * Four rows always, because the point is to show which layer answered and
 * which did not. A row that cannot apply to this protocol says why instead of
 * disappearing — a missing row would read as "passed".
 */
@Composable
private fun StagedProbeDialog(state: StagedProbeUiState, onDismiss: () -> Unit) {
    if (state is StagedProbeUiState.Idle) return
    AlertDialog(
            containerColor = StarveilColors.card,
            shape = StarveilShapes.dialog,
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    R.string.profiles_probe_title,
                    when (state) {
                        is StagedProbeUiState.Running -> state.profileName
                        is StagedProbeUiState.Done -> state.profileName
                        is StagedProbeUiState.Failed -> state.profileName
                        StagedProbeUiState.Idle -> ""
                    },
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StarveilSpacing.sm)) {
                when (state) {
                    StagedProbeUiState.Idle -> Unit

                    is StagedProbeUiState.Running -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(StarveilSpacing.md),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.profiles_probe_running))
                    }

                    is StagedProbeUiState.Failed -> Text(
                        text = stringResource(R.string.profiles_probe_failed, state.message),
                        style = StarveilType.label,
                        color = StarveilColors.errText,
                    )

                    is StagedProbeUiState.Done -> {
                        val report = state.report
                        report.stages.forEach { stage -> StageRow(stage.stage, stage.outcome, stage.ms, stage.note) }
                        Spacer(modifier = Modifier.height(StarveilSpacing.xs))
                        Text(
                            text = report.failedAt?.let { failed ->
                                stringResource(
                                    R.string.profiles_probe_stuck,
                                    stageLabel(failed.stage),
                                    outcomeLabel(failed.outcome),
                                )
                            } ?: stringResource(R.string.profiles_probe_reached),
                            style = StarveilType.label,
                            color = if (report.failedAt == null) StarveilColors.ok else StarveilColors.errText,
                        )
                        Text(
                            text = stringResource(R.string.profiles_probe_scope),
                            style = StarveilType.tech,
                            color = StarveilColors.ink3,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profiles_dialog_ok))
            }
        },
    )
}

@Composable
private fun StageRow(stage: ProbeStage, outcome: StageOutcome, ms: Long, note: StageNote) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stageLabel(stage),
            style = StarveilType.label,
            color = StarveilColors.ink,
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = when (outcome) {
                StageOutcome.OK -> stringResource(R.string.profiles_probe_ok, ms)
                StageOutcome.TIMEOUT -> stringResource(R.string.profiles_probe_timeout)
                StageOutcome.FAILED -> stringResource(R.string.profiles_probe_failed_stage)
                StageOutcome.SKIPPED -> noteLabel(note)
            },
            style = StarveilType.label,
            color = when (outcome) {
                StageOutcome.OK -> StarveilColors.ok
                StageOutcome.TIMEOUT, StageOutcome.FAILED -> StarveilColors.errText
                StageOutcome.SKIPPED -> StarveilColors.ink3
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun stageLabel(stage: ProbeStage): String = stringResource(
    when (stage) {
        ProbeStage.DNS -> R.string.profiles_probe_stage_dns
        ProbeStage.TCP -> R.string.profiles_probe_stage_tcp
        ProbeStage.TLS -> R.string.profiles_probe_stage_tls
        ProbeStage.HTTP -> R.string.profiles_probe_stage_http
    },
)

@Composable
private fun outcomeLabel(outcome: StageOutcome): String = stringResource(
    when (outcome) {
        StageOutcome.OK -> R.string.profiles_probe_outcome_ok
        StageOutcome.TIMEOUT -> R.string.profiles_probe_timeout
        StageOutcome.FAILED -> R.string.profiles_probe_failed_stage
        StageOutcome.SKIPPED -> R.string.profiles_probe_skipped
    },
)

@Composable
private fun noteLabel(note: StageNote): String = stringResource(
    when (note) {
        StageNote.NONE -> R.string.profiles_probe_skipped
        StageNote.LITERAL_HOST -> R.string.profiles_probe_note_literal
        StageNote.UDP_PROTOCOL -> R.string.profiles_probe_note_udp
        StageNote.REALITY_HANDSHAKE -> R.string.profiles_probe_note_reality
        StageNote.NO_TLS -> R.string.profiles_probe_note_no_tls
        StageNote.NO_HTTP -> R.string.profiles_probe_note_no_http
    },
)



/** How long a freshly imported row stays highlighted. */
private const val HIGHLIGHT_MS = 1200L

/** How long the refresh glyph takes to come round once. */
private const val REFRESH_SPIN_MS = 650

/**
 * A suffix for every node whose title would read the same as another's.
 *
 * The order is the one a reader would use: the port first, because two nodes on
 * one host differ by it; then the protocol; then, when both of those are equal
 * too, an ordinal. Only the colliding rows get a suffix.
 */
internal fun disambiguate(profiles: List<Profile>): Map<Long, String> {
    val out = mutableMapOf<Long, String>()
    profiles
        .groupBy { it.displayName().take(NAME_PREFIX_CHARS) }
        .filterValues { it.size > 1 }
        .values
        .forEach { group ->
            if (group.map { it.port }.distinct().size == group.size) {
                group.forEach { out[it.id] = ":" + it.port }
                return@forEach
            }
            if (group.map { it.type }.distinct().size == group.size) {
                group.forEach { out[it.id] = it.type.shortName() }
                return@forEach
            }
            group.forEachIndexed { index, profile -> out[profile.id] = "#" + (index + 1) }
        }
    return out
}

/** Roughly what a node title shows before the ellipsis, at the list's type size. */
private const val NAME_PREFIX_CHARS = 12

/** How much of a host survives before the middle of it is elided. */
private const val HOST_CHARS = 20

/** "a-very-long-subdomain.example.com" -> "a-very-lon…ample.com". */
/** Long enough to recognise a node, short enough to leave room for "via". */
private const val FRONT_NAME_MAX = 14

internal fun middleEllipsis(text: String, max: Int = HOST_CHARS): String =
    if (text.length <= max) text else text.take(max / 2) + "…" + text.takeLast(max - max / 2 - 1)

/** The protocol as it appears in a suffix: "HY2", "SS", "WG". */
internal fun ProfileType.shortName(): String = when (this) {
    ProfileType.VLESS -> "VLESS"
    ProfileType.VMESS -> "VMess"
    ProfileType.TROJAN -> "Trojan"
    ProfileType.SHADOWSOCKS -> "SS"
    ProfileType.HYSTERIA2 -> "HY2"
    ProfileType.TUIC -> "TUIC"
    ProfileType.WIREGUARD -> "WG"
    ProfileType.OTHER -> "?"
}
