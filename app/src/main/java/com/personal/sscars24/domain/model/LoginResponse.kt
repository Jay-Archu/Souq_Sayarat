package com.personal.sscars24.domain.model

data class LoginResponse(
    val captcha_token_received : Boolean,
    val message : String,
    val otp : String,
    val remaining_otps : Int,
    val request_id : String,
    val status_code : Int,
)
