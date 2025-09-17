package com.personal.sscars24.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.sscars24.data.local.UserPreferences
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
import com.personal.sscars24.domain.model.UploadResponse
import com.personal.sscars24.domain.repository.LoginRepo
import com.personal.sscars24.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: LoginRepo,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private var loginJob: Job? = null
    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted

    private val _getCountryInfo =
        MutableStateFlow<Resource<List<CountryCodeResponse>>>(Resource.Empty)
    val getCountryInfo: StateFlow<Resource<List<CountryCodeResponse>>> get() = _getCountryInfo

    private val _getLoginInfo = MutableSharedFlow<Resource<LoginResponse>>(replay = 0)
    val getLoginInfo: SharedFlow<Resource<LoginResponse>> = _getLoginInfo.asSharedFlow()

    private val _verifyOtpInfo = MutableSharedFlow<Resource<OtpResponse>>(replay = 0)
    val verifyOtpInfo: SharedFlow<Resource<OtpResponse>> = _verifyOtpInfo.asSharedFlow()

    private val _uploadInfo = MutableSharedFlow<Resource<UploadResponse>>(replay = 0)
    val uploadInfo: SharedFlow<Resource<UploadResponse>> = _uploadInfo.asSharedFlow()
    private val _resendOtpInfo = MutableSharedFlow<Resource<ResendOtpResponse>>(replay = 0)
    val resendOtpInfo: SharedFlow<Resource<ResendOtpResponse>> = _resendOtpInfo.asSharedFlow()

    private val _registerInfo = MutableSharedFlow<Resource<RegisterResponse>>(replay = 0)
    val registerInfo: SharedFlow<Resource<RegisterResponse>> = _registerInfo.asSharedFlow()

    fun setPermissionsGranted(granted: Boolean) {
        _permissionsGranted.value = granted
    }
    fun <T> saveKey(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            userPreferences.storeData(key, value)
        }
    }
    fun getCountryCode() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            if (isConnected.value) {
                _getCountryInfo.value = Resource.Loading
                val result = repo.getCountryCode(context)
                _getCountryInfo.value = result
            } else {
                _getCountryInfo.value = Resource.Failure(
                    isNetworkError = true,
                    errorCode = null,
                    errorBody = null
                )
            }
        }
    }

    fun postLogin(request: LoginRequest) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            if (isConnected.value) {
                _getLoginInfo.emit(Resource.Loading)
                val result = repo.postLogin(context, request)
                _getLoginInfo.emit(result)
            } else {
                _getLoginInfo.emit(
                    Resource.Failure(
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null
                    )
                )
            }
        }
    }

    fun otpVerify(request: OtpRequest) {
        viewModelScope.launch {
            if (isConnected.value) {
                _verifyOtpInfo.emit(Resource.Loading)
                val result = repo.otpVerify(context, request)
                _verifyOtpInfo.emit(result)
            } else {
                _verifyOtpInfo.emit(
                    Resource.Failure(
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null
                    )
                )
            }
        }
    }

    fun resendOtp(request: ResendOtpRequest) {
        viewModelScope.launch {
            if (isConnected.value) {
                _resendOtpInfo.emit(Resource.Loading)

                val result = repo.resendOtp(context, request)
                _resendOtpInfo.emit(result)

            } else {
                _resendOtpInfo.emit(
                    Resource.Failure(
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null
                    )
                )
            }
        }
    }

    fun uploadAttachment(bitmap: Bitmap? = null, uri: Uri? = null) {
        viewModelScope.launch {
            val tokenValue: String? = userPreferences.getData(UserPreferences.Token)
                .filterNotNull()
                .filter { it.isNotBlank() }
                .first()
            if (isConnected.value) {
                _uploadInfo.emit(Resource.Loading)

                val result = when {
                    uri != null -> {
                        repo.uploadAttachmentFromUri(context, uri,tokenValue)
                    }
                    bitmap != null -> {
                        repo.uploadAttachmentFromBitmap(context, bitmap,tokenValue)
                    }
                    else -> {
                        Resource.Failure(
                            isNetworkError = false,
                            errorCode = null,
                            errorBody = null
                        )
                    }
                }
                _uploadInfo.emit(result)

            } else {
                _uploadInfo.emit(
                    Resource.Failure(
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null
                    )
                )
            }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            val tokenValue: String? = userPreferences.getData(UserPreferences.Token)
                .filterNotNull()
                .filter { it.isNotBlank() }
                .first()
            if (isConnected.value) {
                _registerInfo.emit(Resource.Loading)

                val result = repo.register(context, tokenValue,request)
                _registerInfo.emit(result)

            } else {
                _registerInfo.emit(
                    Resource.Failure(
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loginJob?.cancel()
    }

    fun clearAll() {
        viewModelScope.launch {
            userPreferences.clearAll()
        }
    }
}