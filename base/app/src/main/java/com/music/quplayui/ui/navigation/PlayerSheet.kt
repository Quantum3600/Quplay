package com.music.quplayui.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The full player, shown over the pages instead of being one of them, like the official
 * app: pulled down, it uncovers the page it was opened from, with the mini player and the
 * tabs already in place, and closing it leaves them there.
 */
object PlayerSheet {
    val open = mutableStateOf(false)

    fun show() { open.value = true }
    fun close() { open.value = false }

    /** The mini player's top edge in the window (px), where the player rises from and goes back to. */
    var miniTop by mutableFloatStateOf(0f)
}

/**
 * How far the player's content has come in while it opens (0 .. 1), read in draw lambdas: the
 * cover, the titles and the controls fade in once the sheet is under way, after its background.
 */
val LocalPlayerReveal = androidx.compose.runtime.staticCompositionLocalOf<() -> Float> { { 1f } }

/*
 * Opening, as the official app: the player grows out of the mini player. Its background rises
 * from the mini player's top edge and fades in over the first part of the way, then the cover,
 * titles and controls fade in and settle a few dp up. Closing plays it backwards: the content
 * fades first, then the sheet sinks into the mini player and fades into it. No dimming of the
 * page behind: darker than the player's own background, it made the screen blink dark then light
 * on the first and last frames (measured on a recording, reported).
 *
 * It replaces a cover flown between the two players (a copy that ran out of step with the real
 * one: a ghost square during the slide, a lone cover crossing the page on the way down) and a
 * slide whose clock started on the frame that built the whole player (its first frames lost
 * to that work: the "jump" at the start).
 */

