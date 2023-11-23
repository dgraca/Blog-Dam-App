package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

class Error (
    @SerializedName("message") val message: String?,
    @SerializedName("errors") val errors: Map<String, List<String>>
)