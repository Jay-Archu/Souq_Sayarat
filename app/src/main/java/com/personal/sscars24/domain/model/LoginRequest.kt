package com.personal.sscars24.domain.model

data class LoginRequest(
    val phone_number : String,
    val captcha_token : String,
)
