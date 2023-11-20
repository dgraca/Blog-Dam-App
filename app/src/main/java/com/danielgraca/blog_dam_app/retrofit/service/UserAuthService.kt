package com.danielgraca.blog_dam_app.retrofit.service

import com.danielgraca.blog_dam_app.model.LoginData
import com.danielgraca.blog_dam_app.model.UserAuth
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Interface to define the user authentication service
 */
interface UserAuthService {

    /**
     * Send a POST request to the server to authenticate the user
     */
    @POST("api/login")
    fun login(@Body body: LoginData): Call<UserAuth>

    /**
     * Send a POST request to the server to register the user
     */
    @POST("api/register")
    fun register(): Call<UserAuth>

    /**
     * Send a POST request to the server to logout the user
     */
    @POST("api/logout")
    fun logout(): Call<UserAuth>
}