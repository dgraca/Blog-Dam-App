package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

data class UserEdit (
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
)