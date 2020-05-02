package com.bhoodz.carapp.models.request

import android.os.Parcelable

enum class ResendRequestType {
    GET_VEHICLES, ADD_VEHICLE, UPDATE_VEHICLE, DELETE_VEHICLE, GET_FUELS, ADD_FUEL, UPDATE_FUEL, DELETE_FUEL
}

data class ResendRequest(val type: ResendRequestType, val token: String, val entity: Parcelable?) {
    override fun toString(): String {
        return "type: $type, token: $token, entity: $entity"
    }
}