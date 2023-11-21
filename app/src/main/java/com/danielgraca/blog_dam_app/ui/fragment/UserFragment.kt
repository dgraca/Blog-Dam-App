package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.UserData
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    // UI elements
    private lateinit var etEditUserName: EditText
    private lateinit var etEditEmail: EditText
    private lateinit var etEditPassword: EditText
    private lateinit var btnEditUser: Button
    private lateinit var sharedPreferences: SharedPreferencesUtils

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    /**
     * Called when the fragment's view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get references to UI elements
        etEditUserName = view.findViewById(R.id.etEditUserName)
        etEditEmail = view.findViewById(R.id.etEditEmail)
        etEditPassword = view.findViewById(R.id.etEditPassword)
        btnEditUser = view.findViewById(R.id.btnEditUser)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(requireContext(), "AUTH")

        // Get user data
        getUserData()

        // Set click listeners
        btnEditUser.setOnClickListener { updateUserData() }
    }

    /**
     * Get user data
     */
    private fun getUserData() {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"
        Log.d("TOKEN", token)

        // Get reference to API
        val call = RetrofitInitializer().userDataService()?.get(token)

        call?.enqueue(object : Callback<UserData?> {
            override fun onResponse(call: Call<UserData?>, response: Response<UserData?>) {
                if (response.isSuccessful) {
                    // set edit_username with name from response
                    etEditUserName.setText(response.body()?.name)
                    // set edit_email with email from response
                    etEditEmail.setText(response.body()?.email)
                } else if (response.code() == 401) {
                    logout()
                }
            }

            override fun onFailure(call: Call<UserData?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Update user data
     */
    private fun updateUserData() {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"
        Log.d("TOKEN", token)

        var data = UserData(
            name = etEditUserName.text.toString()?:"",
            email = etEditEmail.text.toString()?:"",
            password = etEditPassword.text.toString()?: ""
        )

        // Get reference to API
        val call = RetrofitInitializer().userDataService()?.update(token, data)

        call?.enqueue(object : Callback<UserData?> {
            override fun onResponse(call: Call<UserData?>, response: Response<UserData?>) {
                if (response.isSuccessful) {
                    // set edit_username with name from response
                    etEditUserName.setText(response.body()?.name)
                    // set edit_email with email from response
                    etEditEmail.setText(response.body()?.email)

                    // TODO: update navigation header name and email
                } else if (response.code() == 401) {
                    logout()
                } else {
                    // TODO: handle errors
                    Log.e("ERROR", response.message())
                }
            }

            override fun onFailure(call: Call<UserData?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Go to auth activity
     */
    private fun goToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }

    /**
     * Logout
     */
    private fun logout() {
        sharedPreferences.clear("TOKEN")
        goToAuthActivity()
    }
}