package com.example.mftool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mftool.MainViewModel.UiState.Error
import com.example.mftool.MainViewModel.UiState.Loaded
import com.example.mftool.MainViewModel.UiState.Loading
import com.example.mftool.repository.Repository
import com.example.mftool.vo.IsinObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
) : ViewModel() {
    sealed interface UiState {
        object Loading : UiState
        object Loaded : UiState
        data class Error(
            val error: String?,
        ) : UiState
    }

    val uiData: StateFlow<List<IsinObject>> =
        repository.loadDetailsFromCache().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = arrayListOf(),
        )

    val uiState: StateFlow<UiState> =
        repository.repositoryState.map { state ->
            when (state) {
                Repository.State.Loaded -> {
                    Loaded
                }

                Repository.State.Loading -> {
                    Loading
                }

                Repository.ErrorState.NetworkError -> Error("Network Error")
                Repository.ErrorState.TimeoutError -> Error("Timeout Error")
                Repository.ErrorState.UnownError -> Error("Unown Error")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Loading,
        )

    fun fetchDetails(force: Boolean) {
        viewModelScope.launch { repository.fetchDetailsFromNetwork(force) }
    }
}