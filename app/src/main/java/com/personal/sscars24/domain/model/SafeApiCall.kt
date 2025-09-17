package com.personal.sscars24.domain.model

import android.content.Context
import com.personal.sscars24.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        context: Context,
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return@withContext Resource.Failure(
                    isNetworkError = true,
                    errorCode = null,
                    errorBody = null
                )
            }

            try {
                val result = apiCall.invoke()
                Resource.Success(result)
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(
                            isNetworkError = false,
                            errorCode = throwable.code(),
                            errorBody = throwable.response()?.errorBody()
                        )
                    }
                    else -> {
                        Resource.Failure(
                            isNetworkError = true,
                            errorCode = null,
                            errorBody = null
                        )
                    }
                }
            }
        }
    }

}
