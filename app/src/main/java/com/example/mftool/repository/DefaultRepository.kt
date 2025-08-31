package com.example.mftool.repository

import android.content.Context
import androidx.compose.ui.util.fastMaxBy
import com.example.mftool.R
import com.example.mftool.api.ApiCall
import com.example.mftool.api.ApiError
import com.example.mftool.api.ApiService
import com.example.mftool.api.IsinDetailsResponse
import com.example.mftool.api.safeApiCall
import com.example.mftool.db.IsinDao
import com.example.mftool.db.IsinEntity
import com.example.mftool.db.toIsinObject
import com.example.mftool.utils.CDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.inject.Inject

class DefaultRepository @Inject constructor(
    private val apiService: ApiService,
    private val dao: IsinDao,
    private val dispatchers: CDispatchers,
    @param:ApplicationContext private val context: Context
) : Repository {

    private val _repositoryState =
        MutableStateFlow<Repository.State>(Repository.State.Loading)

    override val repositoryState: StateFlow<Repository.State> = _repositoryState

    override fun loadDetailsFromCache() =
        dao.getIsinEntities().map { it.map { entity -> entity.toIsinObject() } }
            .onEach {
                _repositoryState.emit(Repository.State.Loaded)
            }

    override suspend fun fetchDetailsFromNetwork(force: Boolean) {
        _repositoryState.emit(Repository.State.Loading)

        val funds = loadFunds()
        withContext(dispatchers.default()) {
            for (fund in funds) {
                val apiCallResponse = if (force)
                    safeApiCall { apiService.getDetails(fund.isin) }
                else
                    safeApiCall { apiService.getDetailsLatest(fund.isin) }
                handleResponse(apiCallResponse, force)
            }
        }

        _repositoryState.emit(Repository.State.Loaded)
    }

    private suspend fun handleResponse(response: ApiCall<IsinDetailsResponse?>, force: Boolean) {
        when (response) {
            is ApiCall.Success<IsinDetailsResponse?> -> {
                val unwrappedResponse = response.response
                if (unwrappedResponse != null) {
                    val peak = if (force) {
                        findPeak(unwrappedResponse.data)
                    } else {
                        maxOf(
                            dao.getPeak(unwrappedResponse.meta.schemeCode),
                            unwrappedResponse.data.first().nav.toDouble()
                        )
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
                            peak = peak
                        )
                    )
                } else {
                    withContext(dispatchers.main()) {
                        _repositoryState.emit(Repository.ErrorState.UnownError)
                    }
                }
            }

            is ApiCall.Error -> {
                withContext(dispatchers.main()) {
                    _repositoryState.emit(
                        when (response.errorType) {
                            is ApiError.NETWORK -> Repository.ErrorState.NetworkError
                            is ApiError.TIMEOUT -> Repository.ErrorState.TimeoutError
                            is ApiError.UNOWN -> Repository.ErrorState.UnownError
                        }
                    )
                }
            }
        }
    }

    private suspend fun findPeak(data: List<IsinDetailsResponse.Data>): Double {
        return withContext(dispatchers.default()) {
            data.fastMaxBy { it.nav.toDouble() }?.nav?.toDouble() ?: 0.0
        }
    }

    private suspend fun loadFunds(): List<Isin> {
        return withContext(dispatchers.io()) {
            val `is`: InputStream = context.resources.openRawResource(R.raw.csvdata)
            val reader = BufferedReader(
                InputStreamReader(`is`, Charset.forName("UTF-8"))
            )
            readCsv(reader)
        }
    }
}