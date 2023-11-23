package com.danielgraca.blog_dam_app.retrofit.service

import com.danielgraca.blog_dam_app.model.data.UserEditData
import com.danielgraca.blog_dam_app.model.response.UserEditResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT


/**
 * Interface to define the user authentication service
 */
interface UserDataService {

    /**
     * Send a GET request to the server to get the user data
     */
    @GET("api/user")
    @Headers("Accept: application/json")
    fun get(@Header("Authorization") token: String): Call<UserEditResponse>

    /**
     * Send a POST request to the server to update the user data
     */
    @PUT("api/user")
    @Headers("Accept: application/json")
    fun update(@Header("Authorization") token: String, @Body body: UserEditData): Call<UserEditResponse>

    /**
     * Send a DELETE request to the server to delete the user data
     */
    @DELETE("api/user")
    @Headers("Accept: application/json")
    fun delete(@Header("Authorization") token: String): Call<UserEditResponse>
}