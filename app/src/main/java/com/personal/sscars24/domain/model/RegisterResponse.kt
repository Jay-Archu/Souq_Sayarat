package com.personal.sscars24.domain.model

data class RegisterResponse(
    val message: String,
    val status_code: Int,
    val user: User,
)
