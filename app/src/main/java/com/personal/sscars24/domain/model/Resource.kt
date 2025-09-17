package com.personal.sscars24.domain.model

import okhttp3.ResponseBody
import org.json.JSONObject

sealed class Resource<out T> {

    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()
    object Idle : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}

fun Resource.Failure.getErrorMessage(): String {
    return if (isNetworkError) {
        "Network error occurred."
    }  else {
        try {
            errorBody?.string()?.let { raw ->
                val json = JSONObject(raw)
                when {
                    json.has("error") -> json.getString("error")
                    json.has("message") -> json.getString("message")
                    else -> "An unknown error occurred."
                }
            } ?: "An unknown error occurred."
        } catch (e: Exception) {
            "An unknown error occurred."
        }
    }
}
