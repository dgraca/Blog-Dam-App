package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

/**
 * data class's main purpose is to hold data
 * @SerializedName("key") means the value should be serialized to JSON with the provided key
 */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthorResponse,
    @SerializedName("message") val message: String,
    @SerializedName("errors") val errors: Map<String, List<String>>
)
