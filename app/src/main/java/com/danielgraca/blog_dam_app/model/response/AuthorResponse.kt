package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

/**
 * Parses the response from the server when requesting a post
 * (author comes attached to a post)
 */
data class AuthorResponse (
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
)