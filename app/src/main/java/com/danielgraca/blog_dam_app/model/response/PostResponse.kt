package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

/**
 * Parses the response from the server when requesting a post
 */
data class PostResponse (
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val content: String,
    @SerializedName("author") val author: AuthorResponse?,
)