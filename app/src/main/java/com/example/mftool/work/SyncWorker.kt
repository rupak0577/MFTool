package com.example.mftool.work

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mftool.R
import com.example.mftool.api.ApiCall
import com.example.mftool.api.ApiService
import com.example.mftool.api.IsinDetailsResponse
import com.example.mftool.api.safeApiCall
import com.example.mftool.db.IsinDao
import com.example.mftool.db.IsinEntity
import com.example.mftool.repository.Isin
import com.example.mftool.repository.readCsv
import com.example.mftool.utils.CDispatchers
import com.example.mftool.work.DelegatingWorker.Companion.DELEGATING_WORKER_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dispatchers: CDispatchers,
    private val apiService: ApiService,
    private val dao: IsinDao,
) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORKER_INPUT_DATA_FORCE_SYNC = "WORKER_INPUT_DATA_FORCE_SYNC"
        const val WORKER_OUTPUT_DATA_PROGRESS = "WORKER_OUTPUT_DATA_PROGRESS"

        fun start(force: Boolean): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DelegatingWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .setInputData(
                    Data.Builder()
                        .putString(DELEGATING_WORKER_NAME, SyncWorker::class.qualifiedName)
                        .putBoolean(WORKER_INPUT_DATA_FORCE_SYNC, force)
                        .build()
                )
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return withContext(dispatchers.io()) {
            val force = inputData.getBoolean(WORKER_INPUT_DATA_FORCE_SYNC, false)
            fetchDetailsFromNetwork(force || dao.getIsinEntitiesSync().isEmpty())
        }
    }

    suspend fun fetchDetailsFromNetwork(force: Boolean): Result {
        val funds = loadFunds()
        return withContext(dispatchers.default()) {
            for ((idx, fund) in funds.withIndex()) {
                if (isStopped) {
                    break
                }

                val apiCallResponse = if (force)
                    safeApiCall { apiService.getDetails(fund.isin) }
                else
                    safeApiCall { apiService.getDetailsLatest(fund.isin) }
                handleResponse(apiCallResponse, fund.owners, force)

                val progress = ((idx + 1).toFloat()/funds.size.toFloat())
                setProgress(workDataOf(WORKER_OUTPUT_DATA_PROGRESS to progress))
                delay(if (idx+1 == funds.size) 0 else 10_000)
            }

            if (isStopped)
                Result.failure()
            else
                Result.success()
        }
    }

    private suspend fun handleResponse(
        response: ApiCall<IsinDetailsResponse?>,
        owners: String,
        force: Boolean
    ) {
        when (response) {
            is ApiCall.Success<IsinDetailsResponse?> -> {
                val unwrappedResponse = response.response
                if (unwrappedResponse != null) {
                    var lastMarketPeakDate = ""
                    var peak: Double
                    if (force) {
                        val peakItem = findLastMarketPeak(unwrappedResponse.data)
                        lastMarketPeakDate = peakItem?.date ?: ""
                        peak = peakItem?.nav?.toDouble() ?: 0.0
                    } else {
                        lastMarketPeakDate = dao.getPeakDate(unwrappedResponse.meta.schemeCode)
                        peak = dao.getPeak(unwrappedResponse.meta.schemeCode)
                    }

                    dao.upsertNav(
                        IsinEntity(
                            schemeCode = unwrappedResponse.meta.schemeCode,
                            date = unwrappedResponse.data.first().date,
                            nav = unwrappedResponse.data.first().nav.toDouble(),
                            fundHouse = unwrappedResponse.meta.fundHouse,
                            schemeName = unwrappedResponse.meta.schemeName,
                            schemeType = unwrappedResponse.meta.schemeType,
                            schemeCategory = unwrappedResponse.meta.schemeCategory,
                            peak = peak,
                            peakDate = lastMarketPeakDate,
                            owners = owners
                        )
                    )
                } else {

                }
            }

            is ApiCall.Error -> {
//                when (response.errorType) {
//                    is ApiError.NETWORK -> Repository.ErrorState.NetworkError
//                    is ApiError.TIMEOUT -> Repository.ErrorState.TimeoutError
//                    is ApiError.UNOWN -> Repository.ErrorState.UnownError
//                }
            }
        }
    }

    private suspend fun findLastMarketPeak(data: List<IsinDetailsResponse.Data>): IsinDetailsResponse.Data? {
        return withContext(dispatchers.default()) {
            data.maxBy { it.nav.toDouble() }
        }
    }

    private suspend fun loadFunds(): List<Isin> {
        return withContext(dispatchers.io()) {
            val `is`: InputStream = appContext.resources.openRawResource(R.raw.csvdata)
            val reader = BufferedReader(
                InputStreamReader(`is`, Charset.forName("UTF-8"))
            )
            readCsv(reader)
        }
    }
}