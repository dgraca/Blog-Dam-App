package com.danielgraca.blog_dam_app.model.response

import com.danielgraca.blog_dam_app.model.data.PostData
import com.google.gson.annotations.SerializedName

/**
 * Parses the response from the server when requesting a list of posts
 */
data class PostListResponse (
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("data") val data: List<PostData>?,
    @SerializedName("last_page") val lastPage: Int?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("total") val total: Int?
)