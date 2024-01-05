package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doBeforeTextChanged
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.data.UserEditData
import com.danielgraca.blog_dam_app.model.response.ErrorResponse
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
    private lateinit var tvUpdateErrorMessage: TextView
    private lateinit var sharedPreferences: SharedPreferencesUtils
    private lateinit var edit_user_overlay: RelativeLayout

    private var loading: Boolean = false

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
        tvUpdateErrorMessage = view.findViewById(R.id.tvUpdateErrorMessage)
        btnDeleteUser = view.findViewById(R.id.fabDeleteUser)
        edit_user_overlay = view.findViewById(R.id.edit_user_overlay)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(requireContext(), "AUTH")

        // Get user data
        getUserData()

        // Set click listeners
        btnEditUser.setOnClickListener { updateUserData() }
        btnDeleteUser.setOnClickListener { showDeleteDialog() }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilEditUserName.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilEditUserName.error = null
            tvUpdateErrorMessage.visibility = View.GONE
            tvUpdateErrorMessage.text = null
        }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilEditEmail.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilEditEmail.error = null
            tvUpdateErrorMessage.visibility = View.GONE
            tvUpdateErrorMessage.text = null
        }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilEditPassword.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilEditPassword.error = null
            tvUpdateErrorMessage.visibility = View.GONE
            tvUpdateErrorMessage.text = null
        }
    }

    /**
     * Show delete dialog
     */
    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_dialog_title))
            .setMessage(resources.getString(R.string.delete_dialog_supporting_text))
            .setNegativeButton(resources.getString(R.string.delete_dialog_decline)) { _, _ ->
                // do nothing
            }
            .setPositiveButton(resources.getString(R.string.delete_dialog_accept)) { _, _ ->
                // send a request o server to delete account
                deleteAccount()
            }
            .show()
    }

    /**
     * Delete account
     */
    private fun deleteAccount() {
        showSpinner()

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().userService()?.delete(token)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                hideSpinner()
                if (response.isSuccessful) {
                    logout()
                } else if (response.code() == 401) {
                    // show error message
                    logout()
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                hideSpinner()
                logout()
            }
        })
    }

    /**
     * Get user data
     */
    private fun getUserData() {
        showSpinner()

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().userService()?.get(token)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                hideSpinner()
                if (response.isSuccessful) {
                    var name = response.body()!!.name
                    var email = response.body()!!.email

                    // store user data in shared preferences
                    sharedPreferences.store("USER:name", name!!)
                    sharedPreferences.store("USER:email", email!!)

                    // set edit_username with name from response
                    tilEditUserName.editText?.setText(name)
                    // set edit_email with email from response
                    tilEditEmail.editText?.setText(email)
                } else if (response.code() == 401) {
                    logout()
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                hideSpinner()
                logout()
            }
        })
    }

    /**
     * Update user data
     */
    private fun updateUserData() {
        showSpinner()

        // clear errors
        clearErrors()
        // clear focus
        clearFocus()

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        var data = UserEditData(
            name = tilEditUserName.editText?.text.toString(),
            email = tilEditEmail.editText?.text.toString(),
            password = tilEditPassword.editText?.text.toString(),
        )

        // Get reference to API
        val call = RetrofitInitializer().userService()?.update(token, data)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                hideSpinner()
                if (response.isSuccessful) {
                    // set edit_username with name from response
                    tilEditUserName.editText?.setText(response.body()!!.name)
                    // set edit_email with email from response
                    tilEditEmail.editText?.setText(response.body()!!.email)

                    if (response.body() != null) {
                        updateNavigationHeader(response.body()!!)
                    }

                    showUpdateToast(true)
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                } else if (response.code() == 422) {
                    // Request is invalid, handle errors
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    // handle errors
                    handleUpdateErrors(errorResponse)
                    showUpdateToast(false)
                }
            }

            override fun onFailure(call: Call<UserEditResponse?>, t: Throwable) {
                hideSpinner()
                logout()
            }
        })
    }

    /**
     * Show update toast to alert user of successful update
     */
    private fun showUpdateToast(success: Boolean = true) {
        var message = ""
        message = if(success) {
            resources.getString(R.string.updated_successful)
        } else {
            resources.getString(R.string.updated_successful)
        }

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Update navigation header
     */
    private fun updateNavigationHeader(response: UserEditResponse) {
        // get reference to navigation header
        val navigationUserName = requireActivity().findViewById<TextView>(R.id.tvUserName)
        val navigationEmail = requireActivity().findViewById<TextView>(R.id.tvUserEmail)

        // set navigation header name and email
        navigationUserName.text = response.name
        navigationEmail.text = response.email

        // store user data in shared preferences
        sharedPreferences.store("USER:name", response.name!!)
        sharedPreferences.store("USER:email", response.email!!)
    }

    /**
     * Start the loading spinner
     */
    private fun showSpinner() {
        loading = true

        // show loading spinner
        edit_user_overlay.visibility = View.VISIBLE

        // block UI
        btnEditUser.isEnabled = false
        btnDeleteUser.isEnabled = false
    }

    /**
     * Stop the loading spinner
     */
    private fun hideSpinner() {
        loading = false

        // hide loading spinner
        edit_user_overlay.visibility = View.GONE

        // enable UI
        btnEditUser.isEnabled = true
        btnDeleteUser.isEnabled = true
    }

    /**
     * Clear errors
     */
    private fun clearErrors() {
        tilEditUserName.error = null
        tilEditEmail.error = null
        tilEditPassword.error = null
        tvUpdateErrorMessage.visibility = View.GONE
        tvUpdateErrorMessage.text = null
    }

    /**
     * Clear focus
     */
    private fun clearFocus() {
        tilEditUserName.clearFocus()
        tilEditEmail.clearFocus()
        tilEditPassword.clearFocus()
    }

    /**
     * Handle update errors
     */
    private fun handleUpdateErrors(errorBody: ErrorResponse) {
        // check if there is a message error
        if (errorBody.message != null) {
            tvUpdateErrorMessage.text = errorBody.message
            tvUpdateErrorMessage.visibility = View.VISIBLE
        }

        if (errorBody.errors != null) {
            // loop through errors
            for ((key, value) in errorBody.errors) {
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