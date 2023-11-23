package com.danielgraca.blog_dam_app.model.data

import com.google.gson.annotations.SerializedName

data class Login (
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
)