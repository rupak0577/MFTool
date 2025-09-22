package com.example.mftool.repository

import com.example.mftool.db.IsinDao
import com.example.mftool.db.toIsinObject
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultRepository @Inject constructor(
    private val dao: IsinDao,
) : Repository {

    override fun loadDetailsFromCache() =
        dao.getIsinEntities().map { it.map { entity -> entity.toIsinObject() } }
}