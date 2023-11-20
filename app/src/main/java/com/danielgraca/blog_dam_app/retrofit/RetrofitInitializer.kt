package com.danielgraca.blog_dam_app.retrofit

import com.danielgraca.blog_dam_app.retrofit.service.UserAuthService
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

    private val BASE_URL = "http://10.0.2.2:8000/"
    // Solution from
    // https://stackoverflow.com/questions/39918814/use-jsonreader-setlenienttrue-to-accept-malformed-json-at-line-1-column-1-path
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    /**
     * Create a Retrofit instance
     */
    var retrofit: Retrofit? = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun userAuthService() = retrofit?.create(UserAuthService::class.java)

}