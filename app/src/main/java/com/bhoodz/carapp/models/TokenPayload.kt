package com.bhoodz.carapp.models

data class TokenPayload(val userId: String, val email: String){

    override fun toString(): String {
        return "User Id: $userId, Email: $email"
    }
}