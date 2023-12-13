package com.danielgraca.blog_dam_app.model.data

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

/**
 * Parses data to be sent to the server when creating a post
 */
data class PostData(
    @SerializedName("title") val title: String,
    @SerializedName("body") val content: String,
)