package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

class ErrorResponse (
    @SerializedName("message") val message: String?,
    @SerializedName("errors") val errors: Map<String, List<String>>
)