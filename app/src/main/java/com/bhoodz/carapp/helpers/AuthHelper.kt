package com.bhoodz.carapp.helpers

import com.auth0.android.jwt.JWT
import com.bhoodz.carapp.models.TokenPayload
import java.util.*

object AuthHelper {
    fun isTokenExpired(token: String?): Boolean {
        if (token == null) {
            return true
        }

        val jwt = JWT(token)
        val tokenExpireDate = jwt.claims["exp"]?.asDate()
        if (tokenExpireDate != null) {
            val nowDate = Date()
            return nowDate.after(tokenExpireDate)
        }
        return true
    }

    fun getTokenPayload(token: String?): TokenPayload? {
        if (token == null) {
            return null
        }

        val jwt = JWT(token)
        return jwt.claims["payload"]?.asObject(TokenPayload::class.java)
    }
}