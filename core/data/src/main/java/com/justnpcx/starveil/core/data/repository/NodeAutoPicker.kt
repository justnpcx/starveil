package com.justnpcx.starveil.core.data.repository

import com.justnpcx.starveil.core.data.preferences.PreferencesRepository
import com.justnpcx.starveil.core.model.AutoPick
import com.justnpcx.starveil.core.model.NodeCandidate
import com.justnpcx.starveil.core.model.NodeRegion
import com.justnpcx.starveil.core.model.NodeRegions
import com.justnpcx.starveil.core.model.Profile
import com.justnpcx.starveil.core.model.decide
import com.justnpcx.starveil.core.model.nodeHealthOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * "Pick the best node now", over the stored nodes and their health.
 *
 * One implementation for the two places that ask: the home page's auto control
 * before this release, and the Nodes page's selection row. The policy itself
 * lives in `core:model` ([decide]); this only assembles its inputs — the nodes,
 * their health, the node in force and the region the user already chose — so
 * the two callers cannot drift apart.
 *
 * It is a *decision*, not a write: the caller still applies the winner, and a
 * pick inside the stickiness margin is deliberately not a switch.
 */
@Singleton
class NodeAutoPicker @Inject constructor(
    private val profiles: ProfileRepository,
    private val nodeHealth: NodeHealthRepository,
    private val preferences: PreferencesRepository,
) {

    suspend fun pick(): AutoPick? {
        val nodes = profiles.observeAll().first()
        if (nodes.isEmpty()) return null
        val samples = nodeHealth.observeAll().first()
        val currentId = preferences.selectedProfileId.first().takeIf { it != 0L }
        return decide(
            candidates = nodes.map { profile ->
                NodeCandidate(
                    profileId = profile.id,
                    region = NodeRegions.resolve(profile.name, profile.country),
                    health = samples[profile.id]?.let(::nodeHealthOf),
                )
            },
            currentId = currentId,
            preferredRegion = preferredRegionOf(nodes, currentId),
            nowMs = System.currentTimeMillis(),
        )
    }

}

/**
 * The region of the node the user already chose — the only manual preference
 * the app stores.
 *
 * resolve(), not classify(): the same rule the region groups use, so a node the
 * Nodes page files under 美国 is also the one the picker treats as American.
 * Name-only classification would disagree for every node whose name says
 * nothing — which is most imported ones.
 */
fun preferredRegionOf(nodes: List<Profile>, selectedId: Long?): NodeRegion? =
    nodes.firstOrNull { it.id == selectedId }
        ?.let { NodeRegions.resolve(it.name, it.country) }
