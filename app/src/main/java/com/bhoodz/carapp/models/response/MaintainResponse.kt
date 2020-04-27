package com.bhoodz.carapp.models.response

data class MaintainResponse<T>(val success: Boolean, val data: T, val error: String)