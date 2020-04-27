package com.bhoodz.carapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bhoodz.carapp.api.RetrofitClient
import com.bhoodz.carapp.helpers.AuthHelper
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.models.request.MaintainRequest
import com.bhoodz.carapp.models.response.GetListResponse
import com.bhoodz.carapp.models.response.MaintainResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "VehicleViewModel"

enum class VehicleRequestType {
    GET_VEHICLES
}

class VehicleViewModel : ViewModel() {

    private val errorMessage = MutableLiveData<String>()
    val errorMessageEntry: LiveData<String>
        get() = errorMessage

    private val vehicles = MutableLiveData<List<Vehicle>>()
    val vehicleEntries: LiveData<List<Vehicle>>
        get() = vehicles

    private val isLoading = MutableLiveData<Boolean>()
    val isLoadingEntry: LiveData<Boolean>
        get() = isLoading

    init {
        vehicles.postValue(null)
        errorMessage.postValue("")
        isLoading.postValue(false)
    }

    fun getVehicles(token: String?) {
        if (token == null) return
        isLoading.value = true
        errorMessage.value = ""

        RetrofitClient.instance.getVehicles("Bearer $token").enqueue(object: Callback<GetListResponse<Vehicle>> {
            override fun onFailure(call: Call<GetListResponse<Vehicle>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<GetListResponse<Vehicle>>, response: Response<GetListResponse<Vehicle>>) {
                val payload = response.body()
                if (response.isSuccessful && payload != null) {
                    if (payload.success){
                        vehicles.value = payload.data
                        errorMessage.value = ""

                    } else {
                        Log.d(TAG, "transaction error: ${payload.error}")
                        errorMessage.value = payload.error
                    }

                } else {
                    Log.d(TAG, "onResponse body is null: ${response.errorBody()?.toString()}")
                    errorMessage.value = "Unable to retrieve the vehicle body response"
                }

                isLoading.value = false
            }
        })
    }

    fun addVehicle(data: Vehicle, token: String?) {
        if(!handleInitializeRequest(token)) return
        val requestBody = MaintainRequest(data)
        RetrofitClient.instance.addVehicle("Bearer $token", requestBody).enqueue(object: Callback<MaintainResponse<Vehicle>> {
            override fun onFailure(call: Call<MaintainResponse<Vehicle>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Vehicle>>, response: Response<MaintainResponse<Vehicle>>) {
                handleOkMaintainResponse(response, token)
            }
        })
    }

    fun updateVehicle(data: Vehicle, token: String?) {
        if (data._id == null) {
            errorMessage.value = "Vehicle is not being edited but added"
            return
        }
        if(!handleInitializeRequest(token)) return
        val requestBody = MaintainRequest(data)
        RetrofitClient.instance.updateVehicle("Bearer $token", data._id!!, requestBody).enqueue(object: Callback<MaintainResponse<Vehicle>> {
            override fun onFailure(call: Call<MaintainResponse<Vehicle>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Vehicle>>, response: Response<MaintainResponse<Vehicle>>) {
                handleOkMaintainResponse(response, token)
            }
        })
    }

    fun deleteVehicle(id: String?, token: String?) {
        if (id == null) return
        if(!handleInitializeRequest(token)) return
        RetrofitClient.instance.deleteVehicle("Bearer $token", id).enqueue(object: Callback<MaintainResponse<Vehicle>> {
            override fun onFailure(call: Call<MaintainResponse<Vehicle>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Vehicle>>, response: Response<MaintainResponse<Vehicle>>) {
                handleOkMaintainResponse(response, token)
            }
        })
    }

    private fun handleInitializeRequest(token: String?): Boolean {
        if (token == null) {
            errorMessage.value = "Unauthorized to maintain Vehicle"
            return false
        }
        isLoading.value = true
        errorMessage.value = ""
        return true
    }

    private fun handleOkMaintainResponse(response: Response<MaintainResponse<Vehicle>>, token: String?) {
        val payload = response.body()

        if (response.isSuccessful && payload != null) {
            if (payload.success){
                getVehicles(token)
                errorMessage.value = ""

            } else {
                Log.d(TAG, "transaction error: ${payload.error}")
                errorMessage.value = payload.error
            }

        } else {
            Log.d(TAG, "onResponse body is null: ${response.errorBody()?.toString()}")
            errorMessage.value = "Unable to retrieve the vehicle body response"
        }
        isLoading.value = false
    }

    private fun handleFailureResponse(t: Throwable) {
        var message= "Unable to communicate to server"
        if (t.message != null) {
            message = t.message!!
        }

        Log.d(TAG, "onFailure error: $message")
        errorMessage.value = message
        isLoading.value = false
    }
}