package com.danielgraca.blog_dam_app.retrofit

import com.danielgraca.blog_dam_app.retrofit.service.UserAuthService
import com.danielgraca.blog_dam_app.retrofit.service.UserDataService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Class to initialize Retrofit
 * Retrofit is a library that makes it easy to consume RESTful web services
 *
 */
class RetrofitInitializer {
    // Solution from
    // https://stackoverflow.com/questions/39918814/use-jsonreader-setlenienttrue-to-accept-malformed-json-at-line-1-column-1-path
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    /**
     * Create a Retrofit instance
     */
    private var retrofit: Retrofit? = Retrofit.Builder()
        .baseUrl("https://8763-2001-8a0-dd84-2800-3032-ee47-4614-11b.ngrok-free.app/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Create a service for each endpoint for the authentication service
    fun userAuthService() = retrofit?.create(UserAuthService::class.java)
    // Create a service for each endpoint for the user data service
    fun userDataService() = retrofit?.create(UserDataService::class.java)

}