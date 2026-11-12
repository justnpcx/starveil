package com.justnpcx.starveil.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.justnpcx.starveil.core.ui.theme.StarveilColors
import com.justnpcx.starveil.core.ui.theme.StarveilSpacing
import com.justnpcx.starveil.core.ui.util.rememberAnimationsEnabled

/**
 * A row-shaped placeholder, for a list that is still reading its data.
 *
 * The shape mirrors a node row — status dot, name, secondary line, a number at
 * the end — so the list does not jump when the real rows arrive. It is drawn
 * from tokens, never from a screenshot, so it follows the palette.
 *
 * The shimmer is a slow alpha pulse, not a moving highlight: a sweep across a
 * list of rows is the most expensive thing a Fire TV's fill rate can be asked
 * to do, and the skeleton is already the least important pixel on screen.
 */
@Composable
fun StarveilSkeletonRow(
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    titleWidth: Float = 0.46f,
    subtitleWidth: Float = 0.72f,
) {
    val line = StarveilColors.divider
    val block = StarveilColors.segBg
    val animations = rememberAnimationsEnabled()
    val alpha = if (animations) {
        val transition = rememberInfiniteTransition(label = "skeleton")
        val value by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "skeleton-alpha",
        )
        value
    } else {
        0.6f
    }
    Row(
        modifier = modifier
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
            .heightIn(min = 64.dp)
            .padding(horizontal = StarveilSpacing.lg, vertical = StarveilSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(block.copy(alpha = alpha)),
        )
        Spacer(modifier = Modifier.width(StarveilSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(titleWidth)
                    .height(13.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(block.copy(alpha = alpha)),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(subtitleWidth)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(block.copy(alpha = alpha)),
            )
        }
        Spacer(modifier = Modifier.width(StarveilSpacing.md))
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(block.copy(alpha = alpha)),
        )
    }
}
