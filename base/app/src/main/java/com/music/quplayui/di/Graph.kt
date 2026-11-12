package com.music.quplayui.di

import android.app.Application
import app.quplay.host.android.MyspotiRuntime
import com.music.quplayui.data.api.Api
import com.music.quplayui.ui.repository.AppRepository

/** Manual replacement for Spotui's Hilt graph: app-wide singletons. */
object Graph {
    lateinit var app: Application
        private set

    fun init(application: Application) {
        app = application
        // The data saver first: on a metered network with it on, nothing may go out, not even the page.
        com.music.quplayui.data.DataSaver.init(application)
        // The Spotify session: hidden Web Player + API (operation hashes refresh themselves).
        MyspotiRuntime.start(application, loadPage = !com.music.quplayui.data.DataSaver.active)
        // The streaming quality the user chose, set on the player at every page load.
        com.music.quplayui.data.preferences.applyStreamingQuality(application)
        // The crossfade length (0 = off), in the player from the first song on.
        com.music.quplayui.data.preferences.applyCrossfade(com.music.quplayui.data.preferences.getCrossfadeSeconds(application))
        com.music.quplayui.data.local.ListenHistory.init(application)
        // Downloaded songs (private storage), and the downloads left unfinished last time.
        // Supporter thank-yous (accent colour) before any screen draws.
        com.music.quplayui.data.api.SupportReminder.init(application)
        com.music.quplayui.data.offline.OfflineStore.init(application)
        com.music.quplayui.data.offline.OfflineCollections.init(application)
        SongPlayer.startLibraryOutbox(application)
        com.music.quplayui.data.offline.OfflineCollections.refreshAll(application)
        com.music.quplayui.data.local.LocalPlaylists.init(application)
        com.music.quplayui.data.local.LibraryOrder.init(application)
        runCatching { com.music.quplayui.data.DataSaverGlide.install(application) }
        com.music.quplayui.data.offline.OfflineDownloader.resumePending()
        // Episodes have their own queue and their own file: resumed the same way.
        com.music.quplayui.data.offline.PodcastDownloader.resumePending()
        // Installed extensions: each enabled one loads in its own sandbox, off the main thread.
        com.music.quplayui.extensions.Extensions.init(application)
        // Live Radio: its own setting and favorites file (see com.music.spotui.radio).
        com.music.quplayui.radio.RadioFeaturePref.init(application)
        com.music.quplayui.radio.RadioFavoritesStore.init(application)
        // SponsorBlock for podcasts: idle until turned on in the settings.
        com.music.quplayui.podcast.SponsorSkip.init()
    }

    val api: Api by lazy { Api(app) }
    val repository: AppRepository by lazy { AppRepository(api) }
    val currentSongState: CurrentSongState by lazy { CurrentSongState() }
}
