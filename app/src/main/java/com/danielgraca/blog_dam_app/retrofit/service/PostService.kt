package com.danielgraca.blog_dam_app.retrofit.service

import com.danielgraca.blog_dam_app.model.data.PostData
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.model.response.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
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

    /**
     * Get a post
     *
     * @param token the token to be used in the request
     * @param postId the id of the post to be requested
     *
     * @return a post
     */
    @GET("api/posts/{postId}")
    @Headers("Accept: application/json")
    fun getPost(@Header("Authorization") token: String, @Path("postId") postId: Int): Call<PostResponse?>

    /**
     * Create a post with an image
     */
    @Multipart
    @POST("api/posts")
    @Headers("Accept: application/json")
    fun create(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<PostResponse?>

    /**
     * Delete a post
     *
     * @param token the token to be used in the request
     * @param post the post to be created
     *
     */
    @DELETE("api/posts/{postId}")
    @Headers("Accept: application/json")
    fun deletePost(@Header("Authorization") token: String, @Path("postId") postId: Int): Call<Void>
}