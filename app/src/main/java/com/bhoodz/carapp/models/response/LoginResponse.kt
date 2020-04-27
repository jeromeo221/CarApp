package com.bhoodz.carapp.models.response

import com.bhoodz.carapp.models.Login

data class LoginResponse(val success: Boolean, val data: Login, val error: String)