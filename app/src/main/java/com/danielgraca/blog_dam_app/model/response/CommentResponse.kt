package com.danielgraca.blog_dam_app.model.response

import com.google.gson.annotations.SerializedName

/**
 * Parses the response from the server when requesting a post
 */
data class CommentResponse (
    @SerializedName("id") val id: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("user_id") val user: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("likes") val likes: List<LikeResponse>?,
)