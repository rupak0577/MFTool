package com.example.mftool

import android.app.Application
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.mftool.work.SyncManager.Companion.SYNC_WORK
import com.example.mftool.work.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MFToolApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        val lastSyncTime = sharedPreferences.getLong("LAST_SYNC_TIMESTAMP", 0)

        if (System.currentTimeMillis() - lastSyncTime >= TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)) {
            WorkManager.getInstance(this).apply {
                // Run sync on app startup and ensure only one sync worker runs at any time
                enqueueUniqueWork(
                    SYNC_WORK,
                    ExistingWorkPolicy.KEEP,
                    SyncWorker.start(true),
                )
            }
        }
    }
}