package com.danielgraca.blog_dam_app.retrofit.service

import com.danielgraca.blog_dam_app.model.response.PostListResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Interface to define the post service
 */
interface PostService {
    /**
     * Get a list of posts
     *
     * @param token the token to be used in the request
     * @param page the page to be requested
     *
     * @return a list of posts
     */
    @GET("api/posts")
    @Headers("Accept: application/json")
    fun getPosts(@Header("Authorization") token: String, @Query("page") page: Int): Call<PostListResponse?>

}