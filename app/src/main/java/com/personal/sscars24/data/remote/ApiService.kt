package com.personal.sscars24.data.remote

import com.personal.sscars24.domain.model.CountryCodeResponse
import com.personal.sscars24.domain.model.LoginRequest
import com.personal.sscars24.domain.model.LoginResponse
import com.personal.sscars24.domain.model.OtpRequest
import com.personal.sscars24.domain.model.OtpResponse
import com.personal.sscars24.domain.model.RegisterRequest
import com.personal.sscars24.domain.model.RegisterResponse
import com.personal.sscars24.domain.model.ResendOtpRequest
import com.personal.sscars24.domain.model.ResendOtpResponse
import com.personal.sscars24.domain.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("/api/country-codes")
    suspend fun getCountryCodes(
    ): List<CountryCodeResponse>

    @POST("/api/auth/login")
    suspend fun postLogin(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("/api/auth/verify-otp")
    suspend fun otpVerify(
        @Body request: OtpRequest
    ): OtpResponse

    @POST("/api/auth/resend-otp")
    suspend fun resendOtp(
        @Body request: ResendOtpRequest
    ): ResendOtpResponse

    @Multipart
    @POST("/api/search/upload-attachment")
    suspend fun uploadAttachment(
        @Header("Authorization") token: String?,
        @Part attachment: MultipartBody.Part
    ): UploadResponse

    @POST("/api/auth/register")
    suspend fun register(
        @Header("Authorization") token: String?,
        @Body request: RegisterRequest
    ): RegisterResponse
}