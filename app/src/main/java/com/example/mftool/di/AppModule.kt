package com.example.mftool.di

import android.content.Context
import androidx.room.Room
import com.example.mftool.api.ApiService
import com.example.mftool.db.IsinDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        IsinDb::class.java,
        "IsinDb"
    ).build()

    @Provides
    @Singleton
    fun providesRetrofit(): ApiService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl("https://api.mfapi.in/")
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)
    }
}