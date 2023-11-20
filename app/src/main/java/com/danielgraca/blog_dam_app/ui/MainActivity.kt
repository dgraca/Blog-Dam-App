package com.danielgraca.blog_dam_app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.UserData
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Called when the activity is becoming visible to the user
     */
    override fun onStart() {
        super.onStart()

        getUserData()
    }

    /**
     * Get user data
     */
    private fun getUserData() {
        val token = "Bearer ${getToken()}"
        Log.d("TOKEN", token)

        // Get reference to API
        val call = RetrofitInitializer().userAuthService()?.getUserData(token)

        call?.enqueue(object : Callback<UserData?> {
            override fun onResponse(call: Call<UserData?>, response: Response<UserData?>) {
                if (response.isSuccessful) {
                    // TODO: Handle successful response
                    Log.d("onResponse", response.body().toString())
                } else if (response.code() == 401) {
                    logout()
                    Log.e("onResponse error 401", response.message())
                }
            }

            override fun onFailure(call: Call<UserData?>, t: Throwable) {
                Log.e("onFailure error", t.message.toString())
                logout()
            }
        })
    }

    /**
     * Get token from shared preferences
     */
    private fun getToken(): String? {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null)
    }

    /**
     * Clear token from shared preferences
     */
    private fun clearToken() {
        // Get reference to shared preferences
        val sharedPref = getSharedPreferences("AUTH", MODE_PRIVATE)

        // Get reference to editor
        val editor = sharedPref.edit()

        // Clear token from shared preferences
        editor.remove("TOKEN")
        editor.apply()
    }

    /**
     * Go to auth activity
     */
    private fun goToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
    }

    /**
     * Logout
     */
    private fun logout() {
        clearToken()
        goToAuthActivity()
    }
}