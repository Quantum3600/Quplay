/*
 * Adapted from BitChord (https://github.com/kushagrasinghx/BitChord), GPL-3.0-or-later,
 * sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerControls.kt and
 * sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/theme/ArtworkPalette.kt at edc1e6f.
 * Modified for Myspoti, 2026-10.
 *
 * Changes: the scrubber, transport, volume row, round glyph discs and the bottom row. Quality
 * badges, Automix, lossless, the output/party capsule and the autoplay toggle are left out;
 * the glyphs are Material Icons Rounded; the pale player tint is read from the cover colours
 * the mesh backdrop already extracted, and the part of the palette code made of colours
 * published by a streaming service is dropped.
 */
package com.music.spotui.ui.immersive.player

import com.music.spotui.ui.components.zoneOn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import app.myspoti.engine.R
import com.music.spotui.di.Haptics
import com.music.spotui.ui.immersive.Immersive
import com.music.spotui.ui.look.MeshPalette
import com.music.spotui.ui.look.ThinSlider
import com.music.spotui.ui.theme.SpotuiTheme
import com.music.spotui.ui.components.rememberFrameClock

/**
 * The colour every secondary line and glyph in the player is set in: the cover's own colour
 * lifted pale rather than a translucent white. White wherever nothing provides it. The title,
 * transport, seek and volume bars stay white on purpose.
 */
internal val LocalPlayerTint = compositionLocalOf { Color.White }

private const val PLAYER_TINT_LIGHTNESS = 0.88f
private const val PLAYER_TINT_MAX_SATURATION = 0.40f

/** Below this a swatch is black, white or grey to the eye, whatever hue HSL assigns it. */
private const val TINT_MIN_CHROMA = 0.08f

/**
 * The pale tint of [palette]'s first colour that is actually visible (real chroma), or white for
 * a cover with no colour worth echoing: a near-black with a trace of red is "pink" at full
 * saturation in HSL, and lifting it pale would put a pink on the player that is nowhere on the cover.
 */
internal fun playerTintOf(palette: MeshPalette): Color {
    val source = palette.colors.firstOrNull { maxOf(it.red, it.green, it.blue) - minOf(it.red, it.green, it.blue) >= TINT_MIN_CHROMA }
        ?: return Color.White
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(source.toArgb(), hsl)
    hsl[1] = hsl[1].coerceIn(0.18f, PLAYER_TINT_MAX_SATURATION)
    hsl[2] = PLAYER_TINT_LIGHTNESS
    return Color(ColorUtils.HSLToColor(hsl))
}

/** m:ss. */
internal fun formatTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000L).toInt()
    return "%d:%02d".format(total / 60, total % 60)
}

/**
 * The seek bar, with elapsed and remaining either side under it. The playhead is read per tick,
 * in the bar's draw and in the two times, so a tick redraws the bar and recomposes only the times,
 * not the player the bar sits in.
 */
@Composable
internal fun PlayerScrubber(
    state: ImmersivePlayerState,
    onSeek: (Long) -> Unit,
) {
    val scrub = rememberPlayerScrub(state.title, state.positionMs, state.durationMs)
    // As many ticks as the bar has pixels to move, as the Classic player's: ~5 a second on a long song.
    val frame = rememberFrameClock(state.playing && !state.buffering, fps = 15)
    // A crossfade: the times go from the outgoing song to the new one half way, with the
    // credits, dimmed through the switch rather than jumping at full strength.
    Column(Modifier.fillMaxWidth().graphicsLayer { alpha = state.blend?.let { 0.3f + 0.7f * it.wordsAlpha() } ?: 1f }) {
        ThinSlider(
            value = { frame.value; scrub.shown(state.positionMs(), state.durationMs) },
            onValueChange = scrub::drag,
            onValueChangeFinished = { scrub.release { onSeek((it * (state.seekLengthMs?.invoke() ?: state.durationMs)).toLong()) } },
            loading = state.buffering && !scrub.scrubbing,
            // AutoMix: the mix's stretch in the accent from the moment it is planned, beating while it plays.
            mixZone = { state.mixCue?.zoneOn(state.durationMs, incomingShown = state.blend?.outgoingWords == false) },
            mixColor = com.music.spotui.ui.immersive.Immersive.Accent,
        )
        ScrubTimes(frame, scrub, state)
        com.music.spotui.ui.components.PlaybackSpeedNote(LocalPlayerTint.current.copy(alpha = 0.8f), com.music.spotui.ui.immersive.Immersive.Accent,
            Modifier.align(Alignment.CenterHorizontally).offset(y = (-4).dp))
    }
}

/** The elapsed and remaining times under the seek bar, read per tick of [frame]. */
@Composable
private fun ScrubTimes(frame: State<Long>, scrub: PlayerScrub, state: ImmersivePlayerState) {
    frame.value
    val shown = scrub.shown(state.positionMs(), state.durationMs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // The slider's touch target extends well past the drawn bar: pull the labels back up under it.
            .offset(y = (-9).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val duration = state.durationMs.coerceAtLeast(0L)
        val elapsed = (shown * duration).toLong()
        Text(
            text = formatTime(elapsed),
            style = Immersive.Caption,
            color = LocalPlayerTint.current.copy(alpha = 0.55f),
        )
        Text(
            text = "-" + formatTime(duration - elapsed),
            style = Immersive.Caption,
            color = LocalPlayerTint.current.copy(alpha = 0.55f),
        )
    }
}

/**
 * Translucent circular button used for the track menu and the like control. [active] brightens
 * the disc rather than only the glyph: it sits on artwork of any colour, and a white icon on a
 * white-ish sleeve has no tint change left to make.
 */
@Composable
internal fun CircleGlyph(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
) {
    val discAlpha by animateFloatAsState(
        targetValue = if (active) 0.34f else 0.18f,
        label = "glyphDisc",
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(LocalPlayerTint.current.copy(alpha = discAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                Haptics.click()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = icon,
            animationSpec = tween(durationMillis = 180),
            label = "playerMenuGlyph",
        ) { glyph ->
            Icon(
                imageVector = glyph,
                contentDescription = contentDescription,
                tint = LocalPlayerTint.current,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

/** The skip glyphs' touch box, beside the larger play button. */
private val PLAYER_SKIP_TOUCH_SIZE = 64.dp

/**
 * The skip glyphs are drawn a little flatter than they are wide, so the pair reads lower and
 * longer next to the play button without losing any width.
 */
private const val PLAYER_SKIP_HEIGHT_SCALE = 0.85f

/** Previous, play/pause, next: bare glyphs, no backgrounds. */
@Composable
internal fun TransportRow(
    isPlaying: Boolean,
    /** While the stream resolves and buffers, the play glyph would be a lie. */
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    val playSize = 88.dp
    val playTouch = 92.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        // SpaceAround, not SpaceEvenly: the outer margins take half a gap.
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportGlyph(
            icon = Icons.Rounded.SkipPrevious,
            contentDescription = stringResource(R.string.immersive_player_previous),
            size = 64.dp,
            touchSize = PLAYER_SKIP_TOUCH_SIZE,
            heightScale = PLAYER_SKIP_HEIGHT_SCALE,
            onClick = onPrevious,
        )
        // The spinner and the button hand over in a short fade, not in one frame.
        com.music.spotui.ui.components.LoadedFade(isLoading) { loading ->
        if (loading) {
            // Same footprint as the play/pause target: a smaller box would shunt everything below it.
            Box(Modifier.size(playTouch), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(38.dp))
            }
        } else {
            TransportGlyph(
                icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (isPlaying) R.string.immersive_player_pause else R.string.immersive_player_play),
                size = playSize,
                touchSize = playTouch,
                onClick = onPlayPause,
            )
        }
        }
        TransportGlyph(
            icon = Icons.Rounded.SkipNext,
            contentDescription = stringResource(R.string.immersive_player_next),
            size = 64.dp,
            touchSize = PLAYER_SKIP_TOUCH_SIZE,
            heightScale = PLAYER_SKIP_HEIGHT_SCALE,
            onClick = onNext,
        )
    }
}

/**
 * Transport glyph. The circular clip belongs on the touch target, never on the [Icon]: clipping
 * the icon itself shaves the corners off wide glyphs.
 */
@Composable
private fun TransportGlyph(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    touchSize: Dp = size,
    onClick: () -> Unit,
    heightScale: Float = 1f,
) {
    Box(
        modifier = Modifier
            .size(touchSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                Haptics.click()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        // Play and pause swap as in the Classic player (the skip glyphs never change).
        com.music.spotui.ui.components.GlyphSwap(icon) { glyph ->
            Icon(
                imageVector = glyph,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier
                    .size(size)
                    .then(if (heightScale != 1f) Modifier.graphicsLayer { scaleY = heightScale } else Modifier),
            )
        }
    }
}

/** The volume bar between its two speaker glyphs. */
@Composable
internal fun VolumeRow(volume: PlayerVolume) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.VolumeDown,
            contentDescription = stringResource(R.string.immersive_player_volume_down),
            tint = LocalPlayerTint.current.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        ThinSlider(
            value = volume.level.value,
            onValueChange = volume::drag,
            onValueChangeFinished = volume::release,
            idleHeight = 6.dp,
            activeHeight = 10.dp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = stringResource(R.string.immersive_player_volume_up),
            tint = LocalPlayerTint.current.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

private val BOTTOM_ACTION_SIZE = 44.dp

/**
 * One of the bottom row's round toggles (lyrics, listen on, queue).
 * All three share a uniform circular shape and colours, lighting up when active.
 */
@Composable
private fun BottomGlyph(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (highlighted) 0.24f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "bottomGlyphBg",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (highlighted) 1f else 0.75f,
        animationSpec = tween(durationMillis = 180),
        label = "bottomGlyphIcon",
    )
    Box(
        modifier = Modifier
            .size(BOTTOM_ACTION_SIZE + 32.dp, BOTTOM_ACTION_SIZE)
            .clip(RoundedCornerShape(32.dp))
            .background(LocalPlayerTint.current.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                Haptics.click()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = icon,
            animationSpec = tween(durationMillis = 180),
            label = "bottomGlyphIconSwap",
        ) { glyph ->
            Icon(
                imageVector = glyph,
                contentDescription = null,
                tint = LocalPlayerTint.current.copy(alpha = iconAlpha),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * Lyrics, Listen on, and Queue: the row the player ends on.
 *
 * All three toggles have uniform circular shape and colours.
 * In queue, the lyrics button stays in place (does not disappear).
 * Listen on can be hidden via Settings.
 */
@Composable
internal fun PlayerActionRow(
    lyricsOpen: Boolean,
    queueOpen: Boolean,
    deviceActive: Boolean,
    onToggleLyrics: () -> Unit,
    onToggleQueue: () -> Unit,
    onOpenDevices: () -> Unit,
) {
    val showListenOn = LocalInspectionMode.current || com.music.spotui.data.preferences.PlayerCapsulePref.shown
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomGlyph(
            icon = Icons.Rounded.Lyrics,
            contentDescription = stringResource(if (lyricsOpen) R.string.immersive_player_lyrics_close else R.string.immersive_player_lyrics),
            onClick = onToggleLyrics,
            highlighted = lyricsOpen,
        )
        if (showListenOn) {
            BottomGlyph(
                icon = Icons.Rounded.Devices,
                contentDescription = stringResource(R.string.immersive_player_devices),
                onClick = onOpenDevices,
                highlighted = deviceActive,
            )
        }
        BottomGlyph(
            icon = Icons.AutoMirrored.Rounded.QueueMusic,
            contentDescription = stringResource(if (queueOpen) R.string.immersive_player_queue_close else R.string.immersive_player_queue),
            onClick = onToggleQueue,
            highlighted = queueOpen,
        )
    }
}

@Preview
@Composable
private fun PlayerActionRowPreview() {
    SpotuiTheme {
        PlayerActionRow(
            lyricsOpen = false,
            queueOpen = false,
            deviceActive = false,
            onToggleLyrics = {},
            onToggleQueue = {},
            onOpenDevices = {},
        )
    }
}
