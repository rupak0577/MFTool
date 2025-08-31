package com.example.mftool.api

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend inline fun <T> safeApiCall(responseFunction: suspend () -> T): ApiCall<T?> {
    return try {
        ApiCall.Success(responseFunction.invoke())
    } catch (e: Exception) {
        when (e) {
            is SocketTimeoutException -> ApiCall.Error(ApiError.TIMEOUT())
            is HttpException, is IOException -> ApiCall.Error(ApiError.NETWORK())

            else -> ApiCall.Error(ApiError.UNOWN())
        }
    }
}

sealed class ApiCall<out T> {
    class Success<out T>(val response: T) : ApiCall<T>()
    class Error<T>(val errorType: ApiError) : ApiCall<T>()
}

sealed class ApiError {
    class NETWORK : ApiError()
    class TIMEOUT : ApiError()
    class UNOWN : ApiError()
}