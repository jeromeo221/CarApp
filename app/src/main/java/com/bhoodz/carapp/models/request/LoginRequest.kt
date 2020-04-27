package com.bhoodz.carapp.models.request

data class LoginRequest(val email: String,
                   val password: String,
                   val mtoken: String? = "",
                   val isMobile: Boolean = true)