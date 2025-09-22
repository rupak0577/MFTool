package com.example.mftool.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mftool.work.SyncManager.Companion.SYNC_WORK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class DefaultSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
): SyncManager {

    // Here, collector (UI) can be slow so conflate will keep emitter running and provide
    // the latest value, skipping intermediates
    override val isSyncing: Flow<Boolean> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(SYNC_WORK)
        .map { list -> list.any { it.state == WorkInfo.State.RUNNING }}
        .conflate()

    override val progress: Flow<List<Data>> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(SYNC_WORK)
        .mapLatest { list -> list.map { it.progress } }
        .conflate()

    override fun requestSync(force: Boolean) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SYNC_WORK, ExistingWorkPolicy.KEEP, SyncWorker.start(force))
    }

    override fun stopSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SYNC_WORK)
    }
}