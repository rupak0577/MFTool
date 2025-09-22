package com.example.mftool.di

import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactory {
    fun provideHiltWorkerFactory(): HiltWorkerFactory
}