package com.bhoodz.carapp.models.response

data class GetListResponse<T>(val success: Boolean, val data: List<T>, val error: String)