package com.music.quplayui.ui.viewmodel

import com.music.quplayui.di.Graph
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.quplayui.data.api.Response
import com.music.quplayui.data.entity.LibraryEntry
import com.music.quplayui.ui.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel constructor(private val repository: AppRepository = Graph.repository) : ViewModel() {

    private val _playlists: MutableStateFlow<Response<List<LibraryEntry>>> = MutableStateFlow(Response.Loading())
    val playlists: StateFlow<Response<List<LibraryEntry>>> = _playlists

    private var genreKey: String? = null

    fun load(genre: String) {
        if (genreKey == genre) return
        genreKey = genre
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideCategoryPlaylists(genre).collect { _playlists.value = it }
        }
    }
}
