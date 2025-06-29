package com.example.mftool.api

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET(" mf/{isin}")
    suspend fun getDetails(@Path("isin") isin: String): IsinDetailsResponse

    @GET(" mf/{isin}/latest")
    suspend fun getDetailsLatest(@Path("isin") isin: String): IsinDetailsResponse
}