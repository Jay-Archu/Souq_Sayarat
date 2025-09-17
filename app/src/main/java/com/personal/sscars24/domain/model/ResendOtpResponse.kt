package com.personal.sscars24.domain.model

data class ResendOtpResponse(
    val status_code : Int,
    val message : String,
    val request_id : String,
    val otp : String,
    val remaining_otps : Int,
)
