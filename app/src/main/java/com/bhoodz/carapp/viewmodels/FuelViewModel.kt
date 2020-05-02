package com.bhoodz.carapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bhoodz.carapp.api.RetrofitClient
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.response.GetListResponse
import com.bhoodz.carapp.models.response.MaintainResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.os.Handler

private const val TAG = "FuelViewModel"
val EMPTY_FUEL_LIST: List<Fuel> = Collections.emptyList()

class FuelViewModel : ViewModel() {
    private val errorMessage = MutableLiveData<String>()
    val errorMessageEntry: LiveData<String>
        get() = errorMessage

    private val errorMessageMaintain = MutableLiveData<String>()
    val errorMessageMaintainEntry: LiveData<String>
        get() = errorMessageMaintain

    private val fuels = MutableLiveData<List<Fuel>>()
    val fuelEntries: LiveData<List<Fuel>>
        get() = fuels

    private val vehicleId = MutableLiveData<String>()
    val vehicleIdEntry: LiveData<String>
        get() = vehicleId

    private val isLoadingGet = MutableLiveData<Boolean>()
    val isLoadingGetEntry: LiveData<Boolean>
        get() = isLoadingGet

    private val isLoadingMaintain = MutableLiveData<Boolean>()
    val isLoadingMaintainEntry: LiveData<Boolean>
        get() = isLoadingMaintain

    private val isSuccessMaintain = MutableLiveData<Boolean>()
    val isSuccessMaintainEntry: LiveData<Boolean>
        get() = isSuccessMaintain

    init {
        Log.d(TAG, "NEW FUEL VIEW MODEL")
        fuels.postValue(EMPTY_FUEL_LIST)
        vehicleId.postValue(null)
        errorMessage.postValue("")
        errorMessageMaintain.postValue("")
        isLoadingGet.postValue(false)
        isLoadingMaintain.postValue(false)
    }

    fun setVehicleId(id: String?){
        vehicleId.value = id
    }

    fun clearFuels() {
        fuels.value = EMPTY_FUEL_LIST
    }

    fun cleaErrorMessageMaintain() {
        errorMessageMaintain.value = ""
    }

    fun getFuels(currentVehicleId: String?, token: String?) {
        if (token == null || currentVehicleId == null) return
        Log.d(TAG, "Getting Fuels...")

        val r = Runnable {
            isLoadingGet.value = true
        }
        val handler = Handler()
        handler.postDelayed(r, 500)
        errorMessage.value = ""

        RetrofitClient.instance.getFuels("Bearer $token", currentVehicleId).enqueue(object: Callback<GetListResponse<Fuel>> {
            override fun onFailure(call: Call<GetListResponse<Fuel>>, t: Throwable) {
                errorMessage.value = retrieveFailureResponse(t)
                isLoadingGet.value = false
            }

            override fun onResponse(call: Call<GetListResponse<Fuel>>, response: Response<GetListResponse<Fuel>>) {
                val payload = response.body()

                if (response.isSuccessful && payload != null) {
                    if (payload.success){
                        //Check if this is still the vehicle being processed. If not, then do not update the fuels data
                        if (vehicleId.value == currentVehicleId) {
                            fuels.value = payload.data
                        }
                        errorMessage.value = ""

                    } else {
                        Log.d(TAG, "transaction error: ${payload.error}")
                        errorMessage.value = payload.error
                    }

                } else {
                    Log.d(TAG, "onResponse body is null: ${response.errorBody()?.toString()}")
                    errorMessage.value = "Unable to retrieve the fuel body response"
                }
                handler.removeCallbacks(r)
                isLoadingGet.value = false
            }
        })
    }

    fun addFuel(data: Fuel, token: String?) {
        if(!handleInitializeMaintainRequest(token)) return
        isLoadingMaintain.value = true
        isSuccessMaintain.value = false

        RetrofitClient.instance.addFuel("Bearer $token", data).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                errorMessageMaintain.value = retrieveFailureResponse(t)
                isLoadingMaintain.value = false
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                errorMessageMaintain.value = retrieveOkMaintainResponse(response, data, token) {
                    isSuccessMaintain.value = true
                }
                isLoadingMaintain.value = false
            }
        })
    }

    fun updateFuel(data: Fuel, token: String?) {
        if(!handleInitializeMaintainRequest(token)) return
        isLoadingMaintain.value = true
        isSuccessMaintain.value = false

        RetrofitClient.instance.updateFuel("Bearer $token", data._id!!, data).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                errorMessageMaintain.value = retrieveFailureResponse(t)
                isLoadingMaintain.value = false
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                errorMessageMaintain.value = retrieveOkMaintainResponse(response, data, token) {
                    isSuccessMaintain.value = true
                }
                isLoadingMaintain.value = false
            }
        })
    }

    fun deleteFuel(data: Fuel, token: String?) {
        if (token == null) {
            errorMessage.value = "Unauthorized to maintain Fuel"
            return
        }
        errorMessage.value = ""

        RetrofitClient.instance.deleteFuel("Bearer $token", data._id!!).enqueue(object: Callback<MaintainResponse<Fuel>> {
            override fun onFailure(call: Call<MaintainResponse<Fuel>>, t: Throwable) {
                errorMessage.value = retrieveFailureResponse(t)
            }

            override fun onResponse(call: Call<MaintainResponse<Fuel>>, response: Response<MaintainResponse<Fuel>>) {
                errorMessage.value = retrieveOkMaintainResponse(response, data, token) { }
            }
        })
    }

    private fun handleInitializeMaintainRequest(token: String?): Boolean {
        if (token == null) {
            errorMessageMaintain.value = "Unauthorized to maintain Fuel"
            return false
        }
        errorMessageMaintain.value = ""

        return true
    }

    private fun retrieveOkMaintainResponse(response: Response<MaintainResponse<Fuel>>,
                                               fuel: Fuel,
                                               token: String?,
                                               onSuccess: () -> Unit): String {

        val payload = response.body()

        return if (response.isSuccessful && payload != null) {
            if (payload.success){
                getFuels(fuel.vehicle, token)
                onSuccess()
                ""

            } else {
                Log.d(TAG, "transaction error: ${payload.error}")
                payload.error
            }

        } else {
            Log.d(TAG, "onResponse body is null: ${response.errorBody()?.toString()}")
            "Unable to retrieve the vehicle body response"
        }
    }

    private fun retrieveFailureResponse(t: Throwable): String {
        var message = "Unable to communicate to server"
        if (t.message != null) {
            message = t.message!!
        }

        Log.d(TAG, "onFailure error: $message")
        return message
    }
}