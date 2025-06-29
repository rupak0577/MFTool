package com.example.mftool.di

import com.example.mftool.db.IsinDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaoModule {
    @Provides
    fun providesDao(database: IsinDb) = database.isinDao()
}