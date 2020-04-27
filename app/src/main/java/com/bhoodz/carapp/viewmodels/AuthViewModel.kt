package com.bhoodz.carapp.viewmodels

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.bhoodz.carapp.api.RetrofitClient
import com.bhoodz.carapp.models.TokenPayload
import com.bhoodz.carapp.models.request.LoginRequest
import com.bhoodz.carapp.models.request.ResendRequest
import com.bhoodz.carapp.models.request.ResendRequestType
import com.bhoodz.carapp.models.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

private const val TAG = "AuthViewModel"
private const val PREFERENCE_FILE = "CAR_APP_PREF"
private const val MOBILE_AUTH_PREFERENCE = "MOBILE_AUTH"



class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val email = MutableLiveData<String>()

    private val token = MutableLiveData<String>()
    val tokenEntry: LiveData<String>
        get() = token

    private val isLoading = MutableLiveData<Boolean>()
    val isLoadingEntry: LiveData<Boolean>
        get() = isLoading

    private val mobileToken = MutableLiveData<String>()
    val mobileTokenEntry: LiveData<String>
        get() = mobileToken

    private val errorMessage = MutableLiveData<String>()
    val errorMessageEntry: LiveData<String>
        get() = errorMessage

    private val resendRequest = MutableLiveData<ResendRequest>()
    val resendRequestEntry: LiveData<ResendRequest>
        get() = resendRequest

    init {
        token.postValue(null)
        isLoading.postValue(false)
        errorMessage.postValue("")

        val mobileAuth = getApplication<Application>()
            .getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
            .getString(MOBILE_AUTH_PREFERENCE, null)
        Log.d(TAG, "mobile auth text: $mobileAuth")

        if (mobileAuth != null && mobileAuth.isNotEmpty()) {
            val authStrings: List<String> = mobileAuth.split("|")
            Log.d(TAG, "Auth strings size: ${authStrings.size}")

            email.postValue(authStrings[0])
            if (authStrings.size > 1) {
                mobileToken.postValue(authStrings[1])
            } else {
                mobileToken.postValue(null)
            }
        } else {
            email.postValue(null)
            mobileToken.postValue(null)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "Clearing AuthViewModel with email: ${email.value} and mtoken: ${mobileToken.value}")
        var authString = ""
        if (email.value != null && mobileToken.value != null) {
            authString = "${email.value}|${mobileToken.value}"
        }
        getApplication<Application>().getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).edit {
            putString(MOBILE_AUTH_PREFERENCE, authString)
        }
    }

    fun login(email: String, password: String) {
        this.email.value = email
        login(password, null, null)
    }

    fun login() {
        login("", null, null)
    }

    fun relogin(resendRequestType: ResendRequestType, entity: Parcelable?) {
        login("", resendRequestType, entity)
    }

    private fun login(password: String, resendRequestType: ResendRequestType?, entity: Parcelable?) {
        if (email.value == null) return
        isLoading.value = true
        resendRequest.value = null

        Log.d(TAG, "Logging in with Mobile token: ${mobileToken.value}")
        val loginBody = LoginRequest(email.value!!, password, mobileToken.value, true)
        RetrofitClient.instance.login(loginBody).enqueue(object: Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d(TAG, "login error: ${t.message}")
                var message = "Failed to login"
                if (t.message != null) {
                    message = t.message!!
                }
                errorMessage.value = message
                isLoading.value = false
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val payload = response.body()
                if(response.isSuccessful && payload != null) {
                    if (payload.success) {
                        Log.d(TAG, "login token: ${payload.data.token}")
                        Log.d(TAG, "mobile login token: ${payload.data.mtoken}")

                        token.value = payload.data.token
                        errorMessage.value = ""

                        if (payload.data.mtoken != null) {
                            mobileToken.value = payload.data.mtoken
                        }

                        if (resendRequestType != null) {
                            resendRequest.value = ResendRequest(resendRequestType, payload.data.token, entity)
                        }
                    } else {
                        Log.d(TAG, "transaction error: ${response.body()?.error}")
                        errorMessage.value = payload.error
                        email.value = null
                        token.value = null
                        mobileToken.value = null
                    }
                } else {
                    Log.d(TAG, "login error: ${response.errorBody()?.toString()}")
                    errorMessage.value = "Unable to login"
                }

                isLoading.value = false
            }
        })
    }

    fun logout() {
        email.value = null
        token.value = null
        mobileToken.value = null
    }
}