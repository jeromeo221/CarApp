package com.bhoodz.carapp.api

import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.models.request.LoginRequest
import com.bhoodz.carapp.models.request.MaintainRequest
import com.bhoodz.carapp.models.response.GetListResponse
import com.bhoodz.carapp.models.response.LoginResponse
import com.bhoodz.carapp.models.response.MaintainResponse
import retrofit2.Call
import retrofit2.http.*

interface Api {

    @POST("login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @GET("vehicles")
    fun getVehicles(@Header("Authorization") token: String): Call<GetListResponse<Vehicle>>

    @POST("vehicles")
    fun addVehicle(
        @Header("Authorization") token: String,
        @Body body: MaintainRequest<Vehicle>
    ) : Call<MaintainResponse<Vehicle>>

    @PUT("vehicles/{id}")
    fun updateVehicle(
        @Header("Authorization") token: String,
        @Path("id") vehicleId: String,
        @Body body: MaintainRequest<Vehicle>
    ) : Call<MaintainResponse<Vehicle>>

    @DELETE("vehicles/{id}")
    fun deleteVehicle(
        @Header("Authorization") token: String,
        @Path("id") vehicleId: String
    ) : Call<MaintainResponse<Vehicle>>

    @GET("fuels")
    fun getFuels(
        @Header("Authorization") token: String,
        @Query("vehicleId") vehicleId: String
    ): Call<GetListResponse<Fuel>>

    @POST("fuels")
    fun addFuel(
        @Header("Authorization") token: String,
        @Body body: Fuel
    ) : Call<MaintainResponse<Fuel>>

    @PUT("fuels/{id}")
    fun updateFuel(
        @Header("Authorization") token: String,
        @Path("id") fuelId: String,
        @Body body: Fuel
    ) : Call<MaintainResponse<Fuel>>

    @DELETE("fuels/{id}")
    fun deleteFuel(
        @Header("Authorization") token: String,
        @Path("id") fuelId: String
    ) : Call<MaintainResponse<Fuel>>
}