package com.example.mftool.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IsinDao {
    @Query(value = "SELECT * FROM isin")
    fun getIsinEntities(): Flow<List<IsinEntity>>

    @Query(value = "SELECT * FROM isin")
    suspend fun getIsinEntitiesSync(): List<IsinEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(values: List<IsinEntity>)

    @Upsert
    suspend fun upsertNav(value: IsinEntity)

    @Query("SELECT peak FROM isin WHERE schemeCode = :isin")
    suspend fun getPeak(isin: Int): Double
}