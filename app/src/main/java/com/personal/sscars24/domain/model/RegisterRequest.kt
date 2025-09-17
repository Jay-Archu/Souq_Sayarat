package com.personal.sscars24.domain.model

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val date_of_birth: String,
    val user_type: String,
    val company_name: String,
    val owner_name: String,
    val company_address: String,
    val company_phone_number: String,
    val company_registration_number: String,
    val facebook_page: String,
    val instagram_company_profile: String,
    val profile_pic: String,
    val phone_number: String,
    val is_dealer: Boolean,
    val whatsapp: String,
    val document: String,
)
