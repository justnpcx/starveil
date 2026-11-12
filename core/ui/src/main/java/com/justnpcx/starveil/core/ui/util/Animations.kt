package com.justnpcx.starveil.core.ui.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Whether the platform's animator scale is on.
 *
 * A loading shimmer is decoration; a user who has turned animations off has
 * said they do not want it. Read once per composition: the setting changes by
 * leaving the app, and a second read on every frame would be a binder call in
 * a draw loop.
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}
