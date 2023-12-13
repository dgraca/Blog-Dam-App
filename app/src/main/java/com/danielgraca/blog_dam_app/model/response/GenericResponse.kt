package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

data class GenericResponse (
    @SerializedName("message") val message: String?,
)