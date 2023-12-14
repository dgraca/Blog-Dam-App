package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

/**
 * Parses the response from the server when requesting a post
 */
data class LikeResponse (
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val user: Int,
)