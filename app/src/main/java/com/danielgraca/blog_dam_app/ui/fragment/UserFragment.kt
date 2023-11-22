package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.data.UserEditData
import com.danielgraca.blog_dam_app.model.response.UserEditErrorResponse
import com.danielgraca.blog_dam_app.model.response.UserEditResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    // UI elements
    private lateinit var tilEditUserName: TextInputLayout
    private lateinit var tilEditEmail: TextInputLayout
    private lateinit var tilEditPassword: TextInputLayout
    private lateinit var btnEditUser: MaterialButton
    private lateinit var btnDeleteUser: ExtendedFloatingActionButton
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
        tilEditUserName = view.findViewById(R.id.tilEditName)
        tilEditEmail = view.findViewById(R.id.tilEditEmail)
        tilEditPassword = view.findViewById(R.id.tilEditPassword)
        btnEditUser = view.findViewById(R.id.btnEditUser)
        btnDeleteUser = view.findViewById(R.id.fabDeleteUser)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(requireContext(), "AUTH")

        // Get user data
        getUserData()

        // Set click listeners
        btnEditUser.setOnClickListener { updateUserData() }
        btnDeleteUser.setOnClickListener { showDeleteDialog() }
    }

    /**
     * Show delete dialog
     */
    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_dialog_title))
            .setMessage(resources.getString(R.string.delete_dialog_supporting_text))
            .setNegativeButton(resources.getString(R.string.delete_dialog_decline)) { dialog, which ->
                // do nothing
            }
            .setPositiveButton(resources.getString(R.string.delete_dialog_accept)) { dialog, which ->
                // send a request o server to delete account
                deleteAccount()
            }
            .show()
    }

    /**
     * Delete account
     */
    private fun deleteAccount() {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().userDataService()?.delete(token)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                if (response.isSuccessful) {
                    logout()
                } else if (response.code() == 401) {
                    // show error message
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Get user data
     */
    private fun getUserData() {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().userDataService()?.get(token)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                if (response.isSuccessful) {
                    // set edit_username with name from response
                    tilEditUserName.editText?.setText(response.body()!!.name)
                    // set edit_email with email from response
                    tilEditEmail.editText?.setText(response.body()!!.email)
                } else if (response.code() == 401) {
                    logout()
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Update user data
     */
    private fun updateUserData() {
        // hide errors
        tilEditUserName.error = null
        tilEditEmail.error = null
        tilEditPassword.error = null

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        var data = UserEditData(
            name = tilEditUserName.editText?.text.toString(),
            email = tilEditEmail.editText?.text.toString(),
            password = tilEditPassword.editText?.text.toString(),
        )

        Log.d("DATA", data.toString())

        // Get reference to API
        val call = RetrofitInitializer().userDataService()?.update(token, data)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                Log.d("RESPONSE", response.body().toString())
                if (response.isSuccessful) {
                    // set edit_username with name from response
                    tilEditUserName.editText?.setText(response.body()!!.name)
                    // set edit_email with email from response
                    tilEditEmail.editText?.setText(response.body()!!.email)

                    // TODO: update navigation header name and email
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                } else if (response.code() == 422) {
                    // Request is invalid, handle errors
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, UserEditErrorResponse::class.java)
                    // handle errors
                    handleUpdateErrors(errorResponse.errors)
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Handle update errors
     */
    private fun handleUpdateErrors(errors: Map<String, List<String>>) {
        // loop through errors
        for ((key, value) in errors) {
            // check if key is name
            if (key == "name") {
                // loop through name errors and append errors to error message
                for (error in value) {
                    tilEditUserName.error = error
                }
            }

            // check if key is email
            if (key == "email") {
                // loop through email errors and append errors to error message
                for (error in value) {
                    tilEditEmail.error = error
                }
            }

            // check if key is password
            if (key == "password") {
                // loop through password errors and append errors to error message
                for (error in value) {
                    tilEditPassword.error = error
                }
            }
        }
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