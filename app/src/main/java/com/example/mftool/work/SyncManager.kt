package com.example.mftool.work

import androidx.work.Data
import kotlinx.coroutines.flow.Flow

interface SyncManager {
    val isSyncing: Flow<Boolean>
    val progress: Flow<List<Data>>

    fun requestSync(force: Boolean)

    fun stopSync()

    companion object {
        const val SYNC_WORK = "SYNC_WORK"
    }
}