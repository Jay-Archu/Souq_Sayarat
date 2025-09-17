package com.personal.sscars24.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.personal.sscars24.data.remote.ApiService
import com.personal.sscars24.domain.model.CountryCodeResponse
import com.personal.sscars24.domain.model.LoginRequest
import com.personal.sscars24.domain.model.LoginResponse
import com.personal.sscars24.domain.model.OtpRequest
import com.personal.sscars24.domain.model.OtpResponse
import com.personal.sscars24.domain.model.RegisterRequest
import com.personal.sscars24.domain.model.RegisterResponse
import com.personal.sscars24.domain.model.ResendOtpRequest
import com.personal.sscars24.domain.model.ResendOtpResponse
import com.personal.sscars24.domain.model.Resource
import com.personal.sscars24.domain.model.SafeApiCall
import com.personal.sscars24.domain.model.UploadResponse
import com.personal.sscars24.presentation.ui.common.createTempFileFromBitmap
import com.personal.sscars24.presentation.ui.common.createTempFileFromUri
import com.personal.sscars24.presentation.ui.common.guessMimeType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

class LoginRepo @Inject constructor(private val apiService: ApiService) : SafeApiCall {

    suspend fun getCountryCode(context: Context): Resource<List<CountryCodeResponse>> {
        return safeApiCall(context) {
            apiService.getCountryCodes()
        }
    }

    suspend fun postLogin(context: Context,request: LoginRequest): Resource<LoginResponse> {
        return safeApiCall(context) {
            apiService.postLogin(request)
        }
    }

    suspend fun otpVerify(context: Context,request: OtpRequest): Resource<OtpResponse> {
        return safeApiCall(context) {
            apiService.otpVerify(request)
        }
    }

    suspend fun resendOtp(context: Context,request: ResendOtpRequest): Resource<ResendOtpResponse> {
        return safeApiCall(context) {
            apiService.resendOtp(request)
        }
    }

    suspend fun uploadAttachmentFromBitmap(context: Context, bitmap: Bitmap,token:String?, fileNamePrefix: String = "attachment"): Resource<UploadResponse> {
        val tempFile = createTempFileFromBitmap(context, bitmap, fileNamePrefix)
        val mediaType = guessMimeType(tempFile) ?: "image/jpeg"
        val requestBody = tempFile.asRequestBody(mediaType.toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("attachment", tempFile.name, requestBody)

        val result = safeApiCall(context) {
            apiService.uploadAttachment("Bearer $token",multipart)
        }

        try { tempFile.delete() } catch (_: Exception) {}

        return result
    }

    suspend fun uploadAttachmentFromUri(
        context: Context, uri: Uri,
        token: String?, fileNamePrefix: String = "attachment"): Resource<UploadResponse> {
        val tempFile = createTempFileFromUri(context, uri, fileNamePrefix)
        val mediaType = guessMimeType(tempFile) ?: "application/octet-stream"
        val requestBody = tempFile.asRequestBody(mediaType.toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("attachment", tempFile.name, requestBody)

        val result = safeApiCall(context) {
            apiService.uploadAttachment("Bearer $token",multipart)
        }

        try { tempFile.delete() } catch (_: Exception) {}

        return result
    }

    suspend fun register(context: Context,token:String?,request: RegisterRequest): Resource<RegisterResponse> {
        return safeApiCall(context) {
            apiService.register("Bearer $token",request)
        }
    }
}