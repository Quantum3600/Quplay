package com.music.quplayui.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.music.quplayui.ui.theme.AppBackground
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.music.quplayui.ui.immersive.motion.ImmersiveMotion.Pages as IPages
import com.music.quplayui.ui.immersive.motion.ImmersiveMotion.PageFade as IFade

@Composable
fun MyNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val startDestination = Routes.Home.route
    val reduceState = androidx.compose.runtime.rememberUpdatedState(com.music.quplayui.ui.look.rememberReduceMotion())

    com.music.quplayui.ui.components.ArtistChooserHost()
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { IPages.enter(reduceState.value)(this) },
        exitTransition = { IPages.exit(reduceState.value)(this) },
        popEnterTransition = { IPages.popEnter(reduceState.value)(this) },
        popExitTransition = { IPages.popExit(reduceState.value)(this) },
    ){
        page(Routes.Home.route){
            com.music.quplayui.ui.immersive.screens.ImmersiveHomeScreen(navHostController)
        }
        page(Routes.Search.route){
            com.music.quplayui.ui.immersive.screens.ImmersiveSearchScreen(navHostController)
        }
        page(Routes.Library.route) {
            com.music.quplayui.ui.immersive.screens.ImmersiveLibraryScreen(navHostController)
        }
        // Older links to the player route open the player sheet over the page instead.
        page(Routes.Player.route){
            LaunchedEffect(Unit) {
                navHostController.popBackStack(Routes.Player.route, inclusive = true)
                PlayerSheet.show()
            }
        }

        page(Routes.Queue.route){
            com.music.quplayui.ui.immersive.pages.ImmersiveQueueScreen(navHostController)
        }

        page(Routes.LocalFiles.route){
            com.music.quplayui.ui.immersive.pages.ImmersiveLocalFilesScreen(navHostController)
        }

        // Live Radio (see com.music.spotui.radio): plays through LocalPlayer, as a Local Files song does.
        page(Routes.LiveRadio.route){
            com.music.quplayui.radio.ui.ImmersiveLiveRadioScreen(navHostController)
        }

        page(Routes.Downloads.route){
            com.music.quplayui.ui.immersive.pages.ImmersiveDownloadsScreen(navHostController)
        }

        page(Routes.HiddenSongs.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveHiddenSongsScreen(navHostController)
        }

        // The local player's equalizer, speed and silence skipping (Settings > Playback, the player's menu).
        page(Routes.Equalizer.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveEqualizerScreen(navHostController)
        }

        page(Routes.NewReleases.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveNewReleasesScreen(navHostController)
        }

        page(Routes.Episodes.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveEpisodesScreen(navHostController)
        }

        page(Routes.NewEpisodes.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveNewEpisodesScreen(navHostController)
        }

        page(Routes.Recap.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveRecapScreen(navHostController)
        }

        page(Routes.Friends.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveFriendsScreen(navHostController)
        }

        // Spotify's own list for the account (every device), not this phone's counter.
        page(Routes.Recent.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveRecentlyPlayedScreen(navHostController)
        }

        // New releases and episodes of what the account follows (the bell on Home).
        page(Routes.WhatsNew.route) {
            com.music.quplayui.ui.immersive.pages.ImmersiveWhatsNewScreen(navHostController)
        }

        page(Routes.Liked.route){
            com.music.quplayui.ui.immersive.screens.ImmersiveContextScreen(navHostController, type = "liked", id = "")
        }

        // Settings: sound quality, downloads, data, display, language.
        page(Routes.Settings.route){
            com.music.quplayui.ui.immersive.pages.ImmersiveSettingsScreen(navHostController)
        }

        // Extensions: the Store and Installed tabs; one installed extension's page; a repository's extension before install.
        page(Routes.Extensions.route){
            com.music.quplayui.ui.immersive.pages.ImmersiveExtensionsScreen(navHostController)
        }
        page("${Routes.Extension.route}/{id}", arguments = listOf(navArgument("id") { defaultValue = "" })){ entry ->
            com.music.quplayui.ui.immersive.pages.ImmersiveExtensionScreen(navHostController, entry.arguments?.getString("id").orEmpty())
        }
        page("${Routes.ExtensionEntry.route}/{id}", arguments = listOf(navArgument("id") { defaultValue = "" })){ entry ->
            com.music.quplayui.ui.immersive.pages.ImmersiveExtensionEntryScreen(navHostController, entry.arguments?.getString("id").orEmpty())
        }
        // An extension's own page (webview.page), sandboxed.
        page("extension-page/{id}", arguments = listOf(navArgument("id") { defaultValue = "" })){ entry ->
            com.music.quplayui.ui.screens.ExtensionPageScreen(navHostController, entry.arguments?.getString("id").orEmpty())
        }

        page(
            "${Routes.Category.route}/{genre}?title={title}",
            arguments = listOf(navArgument("title") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val genre = navBackStackEntry.arguments?.getString("genre").orEmpty()
            val title = navBackStackEntry.arguments?.getString("title").orEmpty()
            com.music.quplayui.ui.immersive.screens.ImmersiveCategoryScreen(navHostController, genre = genre, title = title.ifBlank { genre })
        }

        page(
            "${Routes.Album.route}/{uString}?artist={artist}&id={albumId}",
            arguments = listOf(
                navArgument("artist") { defaultValue = "" },
                navArgument("albumId") { defaultValue = "" },
            ),
        ) { navBackStackEntry ->
            val uId = navBackStackEntry.arguments?.getString("uString")
            val artist = navBackStackEntry.arguments?.getString("artist").orEmpty()
            val albumId = navBackStackEntry.arguments?.getString("albumId").orEmpty()
            uId?.let { name ->
                com.music.quplayui.ui.immersive.screens.ImmersiveAlbumByIdScreen(navHostController, name = name, artist = artist, id = albumId)
            }
        }

        page(
            "${Routes.Playlist.route}/{pId}?name={name}",
            arguments = listOf(navArgument("name") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val pId = navBackStackEntry.arguments?.getString("pId")
            pId?.let { com.music.quplayui.ui.immersive.screens.ImmersiveContextScreen(navHostController, type = "playlist", id = it) }
        }

        page(
            "${Routes.Profile.route}/{uId}?name={name}",
            arguments = listOf(navArgument("name") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val uId = navBackStackEntry.arguments?.getString("uId")
            val name = navBackStackEntry.arguments?.getString("name").orEmpty()
            uId?.let { com.music.quplayui.ui.immersive.people.ImmersiveProfileScreen(navHostController, userId = it, userName = name) }
        }

        page("$LIKED_BY_ARTIST_ROUTE/{key}") { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("key")?.let { com.music.quplayui.ui.immersive.screens.ImmersiveContextScreen(navHostController, type = "liked-artist", id = it) }
        }

        page(
            "${com.music.quplayui.ui.screens.FOLLOW_LIST_ROUTE}/{uId}/{mode}",
        ) { navBackStackEntry ->
            val uId = navBackStackEntry.arguments?.getString("uId")
            val mode = navBackStackEntry.arguments?.getString("mode")
            uId?.let { com.music.quplayui.ui.immersive.people.ImmersiveFollowListScreen(navHostController, userId = it, followers = mode == "followers") }
        }

        page(
            "${Routes.Show.route}/{sId}?name={name}",
            arguments = listOf(navArgument("name") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val sId = navBackStackEntry.arguments?.getString("sId")
            val name = navBackStackEntry.arguments?.getString("name").orEmpty()
            sId?.let { com.music.quplayui.ui.immersive.podcast.ImmersiveShowScreen(navHostController, showId = it, showName = name) }
        }

        page(
            "${Routes.Episode.route}/{eId}?name={name}",
            arguments = listOf(navArgument("name") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val eId = navBackStackEntry.arguments?.getString("eId")
            val name = navBackStackEntry.arguments?.getString("name").orEmpty()
            eId?.let { com.music.quplayui.ui.immersive.pages.ImmersiveEpisodeScreen(navHostController, episodeId = it, fallbackName = name) }
        }

        page(
            "${Routes.Folder.route}/{uri}?name={name}",
            arguments = listOf(navArgument("name") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val uri = navBackStackEntry.arguments?.getString("uri")
            val name = navBackStackEntry.arguments?.getString("name").orEmpty()
            uri?.let { com.music.quplayui.ui.immersive.pages.ImmersiveFolderScreen(navHostController, it, name) }
        }

        page("${Routes.LocalPlaylist.route}/{id}") { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("id")?.let { com.music.quplayui.ui.immersive.pages.ImmersiveLocalPlaylistScreen(navHostController, it) }
        }

        page(
            "${Routes.ArtistReleases.route}/{aString}?id={artistId}",
            arguments = listOf(navArgument("artistId") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val aId = navBackStackEntry.arguments?.getString("aString")
            val artistId = navBackStackEntry.arguments?.getString("artistId").orEmpty()
            aId?.let { com.music.quplayui.ui.immersive.screens.ImmersiveArtistReleasesScreen(navHostController, it, artistId) }
        }

        page(
            "${Routes.Artist.route}/{aString}?id={artistId}",
            arguments = listOf(navArgument("artistId") { defaultValue = "" }),
        ) { navBackStackEntry ->
            val aId = navBackStackEntry.arguments?.getString("aString")
            val artistId = navBackStackEntry.arguments?.getString("artistId").orEmpty()
            aId?.let { aid ->
                com.music.quplayui.ui.immersive.screens.ImmersiveArtistScreen(navHostController, aid, artistId)
            }
        }
    }
}

// Material 3 emphasized curves: quick start, long soft landing (and the reverse to leave).
internal val Decelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
internal val Accelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
internal const val SHEET_IN_MS = 340
internal const val SHEET_OUT_MS = 240

/** A destination on an opaque background, so sliding pages never show through each other. */
private fun NavGraphBuilder.page(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable(route, arguments = arguments) { entry ->
    val scope = this
    val settled = androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            scope.transition.currentState == scope.transition.targetState
        }
    }
    val immersive = isImmersive()
    // Immersive: the page runs its own fade (ImmersiveMotion.PageFade) and hands it to the Liquid
    // Glass recording it, which then fades with it on every frame of the transition.
    val fade = if (!immersive) null else scope.transition.animateFloat(
        transitionSpec = {
            val ms = if (targetState == androidx.compose.animation.EnterExitState.Visible) IFade.enterMs else IFade.exitMs
            tween(ms)
        },
        label = "pageFade",
    ) { if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 0f }
    val alpha = androidx.compose.runtime.remember(fade) { { fade?.value ?: 1f } }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPageSettled provides settled,
        com.music.quplayui.ui.immersive.shell.backdrop.backdrops.LocalBackdropSourceAlpha provides alpha,
    ) {
        // Large windows: detail pages (album, playlist, artist, settings…) keep a readable width,
        // centred, instead of rows 2000 px wide; the tab pages and grids use the whole width
        // (more columns). The inner box is always there, so a resize never rebuilds the page.
        val capped = com.music.quplayui.ui.components.windowSize().large &&
            entry.destination.route?.substringBefore('/')?.substringBefore('?') !in FullWidthRoutes
        // Immersive look: every page (Classic fallbacks too) sits on its black page colour.
        Box(Modifier.fillMaxSize()
            .then(if (fade == null) Modifier else Modifier.graphicsLayer { this.alpha = fade.value })
            .background(if (immersive) com.music.quplayui.ui.immersive.Immersive.Page else AppBackground)) {
            Box(
                Modifier.fillMaxHeight().then(
                    if (capped) Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                        .widthIn(max = com.music.quplayui.ui.components.WindowSize.MaxPageWidth)
                    else Modifier.fillMaxWidth()
                )
            ) { scope.content(entry) }
        }
    }
}

/** Pages that use a large window's whole width (grids, the tabs' own pages, the player). */
private val FullWidthRoutes = setOf(
    Routes.Home.route, Routes.WhatsNew.route, Routes.Search.route, Routes.Library.route, Routes.Player.route, Routes.Queue.route,
    Routes.Category.route, Routes.NewReleases.route,
)

/**
 * false while this page is sliding in or out. Heavy views (video players, surfaces)
 * wait for it: built in the first frame, they stalled it and the motion jumped.
 */
val LocalPageSettled = androidx.compose.runtime.staticCompositionLocalOf<androidx.compose.runtime.State<Boolean>> {
    androidx.compose.runtime.mutableStateOf(true)
}

/** The Immersive look is always on. */
@Composable
internal fun isImmersive(): Boolean = true
