package com.example.mftool.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.mftool.di.HiltWorkerFactory
import dagger.hilt.android.EntryPointAccessors

class DelegatingWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    companion object {
        const val DELEGATING_WORKER_NAME = "DELEGATING_WORKER_NAME"
    }

    private val delegateWorker = EntryPointAccessors.fromApplication<HiltWorkerFactory>(appContext)
        .provideHiltWorkerFactory()
        .createWorker(
            appContext,
            workerParameters.inputData.getString(DELEGATING_WORKER_NAME) ?: "",
            workerParameters
        ) as? CoroutineWorker
        ?: throw IllegalArgumentException("Unable to find appropriate worker")

    override suspend fun doWork(): Result {
        return delegateWorker.doWork()
    }
}