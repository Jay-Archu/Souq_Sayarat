package com.personal.sscars24.domain.model

data class UploadResponse(
    val status_code: Int,
    val attachment_url: String,
    val message: String,
    val success: Boolean,

)
