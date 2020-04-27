package com.bhoodz.carapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vehicle(var _id: String?, val make: String, val model: String, val year: Int, val user: String, val name: String) : Parcelable{
    override fun toString(): String {
        return "Vehicle Id: ${_id}, make: ${make}, model: ${model}, year: ${year}, user: ${user}, name: ${name}"
    }
}