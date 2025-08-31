package com.example.mftool.repository

import com.example.mftool.vo.IsinObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val repositoryState: StateFlow<State>

    fun loadDetailsFromCache(): Flow<List<IsinObject>>
    suspend fun fetchDetailsFromNetwork(force: Boolean)

    sealed interface State {
        object Loading : State
        object Loaded : State
    }

    sealed interface ErrorState : State {
        object NetworkError : ErrorState
        object TimeoutError : ErrorState
        object UnownError : ErrorState
    }
}