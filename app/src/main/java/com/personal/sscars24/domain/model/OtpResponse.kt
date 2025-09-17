package com.personal.sscars24.domain.model

data class OtpResponse(
    val status_code: Int,
    val access_token: String,
    val message: String,
    val refresh_token: String,
    val is_registered: Boolean,
    val user: User
)

data class User(
    val ban_reason: String?,
    val company_address: String?,
    val company_name: String?,
    val company_phone_number: String?,
    val company_registration_number: String?,
    val created_at: String?,
    val date_of_birth: String?,
    val document: String?,
    val email: String?,
    val facebook_page: String?,
    val first_name: String?,
    val id: Int,
    val instagram_company_profile: String?,
    val is_banned: Int,
    val is_dealer: Int,
    val is_verified: String?,
    val is_whatsapp: Int,
    val last_name: String?,
    val last_otp_sent_at: String?,
    val location: String?,
    val otps_sent_today: Int,
    val owner_name: String?,
    val profile_image: String?,
    val profile_pic: String?,
    val updated_at: String?,
    val user_type: String?,
    val whatsapp: String?
)
