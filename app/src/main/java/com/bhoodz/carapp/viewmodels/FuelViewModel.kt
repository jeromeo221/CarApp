package com.bhoodz.carapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bhoodz.carapp.api.RetrofitClient
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.models.request.MaintainRequest
import com.bhoodz.carapp.models.response.GetListResponse
import com.bhoodz.carapp.models.response.MaintainResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

private const val TAG = "FuelViewModel"
val EMPTY_FUEL_LIST: List<Fuel> = Collections.emptyList()

class FuelViewModel : ViewModel() {
    private val errorMessage = MutableLiveData<String>()
    val errorMessageEntry: LiveData<String>
        get() = errorMessage

    private val fuels = MutableLiveData<List<Fuel>>()
    val fuelEntries: LiveData<List<Fuel>>
        get() = fuels

    private val vehicleId = MutableLiveData<String>()
    val vehicleIdEntry: LiveData<String>
        get() = vehicleId

    private val isLoading = MutableLiveData<Boolean>()
    val isLoadingEntry: LiveData<Boolean>
        get() = isLoading

    init {
        fuels.postValue(EMPTY_FUEL_LIST)
        vehicleId.postValue(null)
        errorMessage.postValue("")
        isLoading.postValue(false)
    }

    fun setVehicleId(id: String?){
        vehicleId.value = id
    }

    fun getFuels(vehicleId: String?, token: String?) {
        if (token == null || vehicleId == null) return
        isLoading.value = true
        fuels.value = EMPTY_FUEL_LIST
        errorMessage.value = ""

        RetrofitClient.instance.getFuels("Bearer $token", vehicleId).enqueue(object: Callback<GetListResponse<Fuel>> {
            override fun onFailure(call: Call<GetListResponse<Fuel>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<GetListResponse<Fuel>>, response: Response<GetListResponse<Fuel>>) {
                val payload = response.body()

                if (response.isSuccessful && payload != null) {
                    if (payload.success){
                        Log.d(TAG, "FUEL PAYLOAD DATA: ${payload.data.size}")
                        fuels.value = payload.data
                        errorMessage.value = ""

                    } else {
                        Log.d(TAG, "transaction error: ${payload.error}")
                        errorMessage.value = payload.error
                    }

                } else {
                    Log.d(TAG, "onResponse body is null: ${response.errorBody()?.toString()}")
                    errorMessage.value = "Unable to retrieve the fuel body response"
                }

                isLoading.value = false
            }
        })
    }

    fun addFuel(data: Fuel, token: String?) {
        if(!handleInitializeRequest(token)) return
        //val requestBody = MaintainRequest(data)
        RetrofitClient.instance.addFuel("Bearer $token", data).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                handleOkMaintainResponse(response, data, token)
            }
        })
    }

    fun updateFuel(data: Fuel, token: String?) {
        if(!handleInitializeRequest(token)) return
        //val requestBody = MaintainRequest(data)
        RetrofitClient.instance.updateFuel("Bearer $token", data._id!!, data).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                handleOkMaintainResponse(response, data, token)
            }
        })
    }

    fun deleteFuel(data: Fuel, token: String?) {
        if(!handleInitializeRequest(token)) return
        RetrofitClient.instance.deleteFuel("Bearer $token", data._id!!).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                handleFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                handleOkMaintainResponse(response, data, token)
            }
        })
    }

    private fun handleInitializeRequest(token: String?): Boolean {
        if (token == null) {
            errorMessage.value = "Unauthorized to maintain Fuel"
            return false
        }
        isLoading.value = true
        errorMessage.value = ""
        return true
    }

    private fun handleOkMaintainResponse(response: Response<MaintainResponse<Fuel>>, fuel: Fuel, token: String?) {
        val payload = response.body()

        if (response.isSuccessful && payload != null) {
            if (payload.success){
                getFuels(fuel.vehicle, token)
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
        var message: String = "Unable to communicate to server"
        if (t.message != null) {
            message = t.message!!
        }

        Log.d(TAG, "onFailure error: $message")
        errorMessage.value = message
        isLoading.value = false
    }
}