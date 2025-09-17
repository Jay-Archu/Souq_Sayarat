package com.personal.sscars24.domain.model

data class OtpRequest (
    val otp : String,
    val request_id : String,
    val remaining_otps : Int,
)