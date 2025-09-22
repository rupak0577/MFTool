package com.example.mftool.repository

import com.example.mftool.vo.IsinObject
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun loadDetailsFromCache(): Flow<List<IsinObject>>
}