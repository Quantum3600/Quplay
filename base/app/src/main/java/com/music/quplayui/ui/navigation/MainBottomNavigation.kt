package com.music.quplayui.ui.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * What re-tapping the tab you are already on does in the official app: Home and Library
 * scroll their list back to the top without reloading anything, Search focuses its field.
 * The bar only sees the tap; the list state and the text field live inside the screens,
 * so the tap is published here and each screen collects its own event.
 */
object TabReselect {
    // replay = 0 + a one-slot buffer: a tap is never held back, and a tap that lands while
    // the screen is not composed is simply dropped rather than replayed on the next visit.
    private val _home = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _library = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _search = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val home: SharedFlow<Unit> = _home
    val library: SharedFlow<Unit> = _library
    val search: SharedFlow<Unit> = _search

    fun emit(route: String) {
        when (route) {
            Routes.Home.route -> _home.tryEmit(Unit)
            Routes.Library.route -> _library.tryEmit(Unit)
            Routes.Search.route -> _search.tryEmit(Unit)
        }
    }
}
