package com.danielgraca.blog_dam_app.model

import com.google.gson.annotations.SerializedName

data class UserData (
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
)