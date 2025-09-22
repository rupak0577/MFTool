package com.example.mftool.di

import com.example.mftool.repository.DefaultRepository
import com.example.mftool.repository.Repository
import com.example.mftool.utils.CDispatchers
import com.example.mftool.utils.DefaultDispatchers
import com.example.mftool.work.DefaultSyncManager
import com.example.mftool.work.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds
    @Singleton
    abstract fun providesCDispatchers(
        dispatchers: DefaultDispatchers
    ): CDispatchers

    @Binds
    @Singleton
    abstract fun providesIsinRepository(
        defaultRepository: DefaultRepository
    ): Repository

    @Binds
    @Singleton
    abstract fun bindsSyncManager(
        defaultSyncManager: DefaultSyncManager,
    ): SyncManager
}