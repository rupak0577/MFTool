package com.example.mftool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.workDataOf
import com.example.mftool.repository.Repository
import com.example.mftool.vo.IsinObject
import com.example.mftool.work.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val syncManager: SyncManager
) : ViewModel() {

    val uiData: StateFlow<List<IsinObject>> =
        repository.loadDetailsFromCache().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = arrayListOf(),
        )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val progress = syncManager.progress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = arrayListOf(workDataOf())
        )

    fun fetchDetails(force: Boolean) {
        syncManager.requestSync(force)
    }

    fun stopSync() {
        syncManager.stopSync()
    }
}