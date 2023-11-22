package com.danielgraca.blog_dam_app.retrofit.service

import com.danielgraca.blog_dam_app.model.data.LoginData
import com.danielgraca.blog_dam_app.model.response.AuthResponse
import com.danielgraca.blog_dam_app.model.data.RegisterData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


/**
 * Interface to define the user authentication service
 */
interface UserAuthService {

    /**
     * Send a POST request to the server to authenticate the user
     */
    @POST("api/login")
    fun login(@Body body: LoginData): Call<AuthResponse>

    /**
     * Send a POST request to the server to register the user
     */
    @POST("api/register")
    fun register(@Body body: RegisterData): Call<AuthResponse>

    /**
     * Send a POST request to the server to logout the user
     */
    @GET("api/logout")
    @Headers("Accept: application/json")
    fun logout(@Header("Authorization") token: String): Call<AuthResponse>
}