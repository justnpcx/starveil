package com.justnpcx.starveil.feature.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The exit card's five states, as the matrix the task book pins.
 *
 * The UI reads this function; a state that is not in the matrix is a state the
 * card cannot draw, which is the defect the matrix exists to prevent.
 */
class ExitCardModeTest {

    @Test
    fun `the five states map to five modes`() {
        assertEquals(ExitCardMode.LIVE, exitCardMode(true, hasLive = true, probeFailed = false, hasLast = true))
        assertEquals(ExitCardMode.FAILED, exitCardMode(true, hasLive = false, probeFailed = true, hasLast = true))
        assertEquals(
            ExitCardMode.NOT_MEASURED,
            exitCardMode(true, hasLive = false, probeFailed = false, hasLast = true),
        )
        assertEquals(ExitCardMode.LAST, exitCardMode(false, hasLive = false, probeFailed = false, hasLast = true))
        assertEquals(ExitCardMode.NONE, exitCardMode(false, hasLive = false, probeFailed = false, hasLast = false))
    }

    @Test
    fun `a live measurement wins over a previous failure`() {
        assertEquals(ExitCardMode.LIVE, exitCardMode(true, hasLive = true, probeFailed = true, hasLast = true))
    }

    @Test
    fun `a stopped tunnel never shows a live value`() {
        assertEquals(ExitCardMode.LAST, exitCardMode(false, hasLive = true, probeFailed = false, hasLast = true))
        assertEquals(ExitCardMode.NONE, exitCardMode(false, hasLive = true, probeFailed = false, hasLast = false))
    }
}
