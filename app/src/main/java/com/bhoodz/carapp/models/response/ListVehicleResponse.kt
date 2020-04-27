package com.bhoodz.carapp.models.response

import com.bhoodz.carapp.models.Vehicle

data class ListVehicleResponse(val success: Boolean, val data: List<Vehicle>, val error: String)