package com.danielgraca.blog_dam_app.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doBeforeTextChanged
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.data.LoginData
import com.danielgraca.blog_dam_app.model.response.AuthResponse
import com.danielgraca.blog_dam_app.model.data.RegisterData
import com.danielgraca.blog_dam_app.model.response.ErrorResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthActivity : AppCompatActivity() {

    // UI elements
    private lateinit var btnAction: MaterialButton
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var tvAuthError: TextView
    private lateinit var mtvToggleMode: MaterialTextView
    private lateinit var sharedPreferences: SharedPreferencesUtils
    private lateinit var auth_overlay: RelativeLayout

    private var loading: Boolean = false

    /**
     * Called when the activity is starting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.toolbar)

        // Get references to UI elements
        btnAction = findViewById(R.id.btnAction)
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        tvAuthError = findViewById(R.id.tvAuthError)
        mtvToggleMode = findViewById(R.id.mtvToggleMode)
        auth_overlay = findViewById(R.id.auth_overlay)

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilName.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilName.error = null
            tvAuthError.visibility = View.GONE
            tvAuthError.text = null
        }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilEmail.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilEmail.error = null
            tvAuthError.visibility = View.GONE
            tvAuthError.text = null
        }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilPassword.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilPassword.error = null
            tvAuthError.visibility = View.GONE
            tvAuthError.text = null
        }

        /**
         * Clear errors when user starts typing
         *
         * _ is a placeholder for unused parameters
         */
        tilConfirmPassword.editText?.doBeforeTextChanged { _, _, _, _ ->
            tilConfirmPassword.error = null
            tvAuthError.visibility = View.GONE
            tvAuthError.text = null
        }


        // Set click listeners
        btnAction.setOnClickListener { performAction() }
        mtvToggleMode.setOnClickListener { toggleMode() }

        // Initialize and set the toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.auth_toolbar)
        setSupportActionBar(toolbar)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(this, "AUTH")
    }

    /**
     * Called when the activity is becoming visible to the user
     */
    override fun onStart() {
        super.onStart()

        // Check if there is a token
        if (sharedPreferences.get("TOKEN") != null) {
            goToMainActivity()
        }
    }

    /**
     * Toggle between login and registration UI
     */
    private fun toggleMode() {
        // clear errors
        clearErrors()
        clearFields()
        clearFocus()

        if (isLoginMode()) {
            // Switch to registration mode
            tilName.visibility = View.VISIBLE
            tilConfirmPassword.visibility = View.VISIBLE
            btnAction.text = getString(R.string.register)
            mtvToggleMode.text = getString(R.string.switch_to_login)
        } else {
            // Switch to login mode
            tilName.visibility = View.GONE
            tilConfirmPassword.visibility = View.GONE
            btnAction.text = getString(R.string.login)
            mtvToggleMode.text = getString(R.string.switch_to_register)
        }
    }

    /**
     * Go to MainActivity
     *
     * Creates an intent to start MainActivity and finishes AuthActivity
     */
    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    /**
     * Perform login action
     */
    private fun login() {
        showSpinner()

        // Create Login object
        val data = LoginData(
            tilEmail.editText?.text.toString(),
            tilPassword.editText?.text.toString()
        )

        // Get reference to API
        val call = RetrofitInitializer().userAuthService()?.login(data)

        call?.enqueue(object : Callback<AuthResponse?> {
            override fun onResponse(call: Call<AuthResponse?>, response: Response<AuthResponse?>) {
                hideSpinner()
                if (response.isSuccessful) {
                    // Get response body
                    val userAuth = response.body()

                    // If there is a token, store it and go to main activity
                    userAuth?.let { auth ->
                        sharedPreferences.store("TOKEN", auth.token)
                        sharedPreferences.store("USER:name", auth.user.name)
                        sharedPreferences.store("USER:email", auth.user.email)
                        goToMainActivity()
                    }

                    return
                }

                // unauthorized or unprocessable content
                if (response.code() == 401 || response.code() == 422) {
                    /** Request is invalid, handle errors **/
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                    // handle errors
                    handleErrors(errorResponse)
                }
            }

            override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                hideSpinner()
            }
        })
    }

    /**
     * Handle update errors
     */
    private fun handleErrors(errorBody: ErrorResponse) {
        // check if there is a message error
        if (errorBody.message != null) {
            tvAuthError.visibility = View.VISIBLE
            tvAuthError.text = errorBody.message
        }

        // check if errorBody contains errors
        if (errorBody.errors != null) {
            // loop through errors
            for ((key, value) in errorBody.errors) {
                // check if key is name
                if (key == "name") {
                    // loop through name errors and append errors to error message
                    for (error in value) {
                        tilName.error = error
                    }
                }

                // check if key is email
                if (key == "email") {
                    // loop through email errors and append errors to error message
                    for (error in value) {
                        tilEmail.error = error
                    }
                }

                // check if key is password
                if (key == "password") {
                    // loop through password errors and append errors to error message
                    for (error in value) {
                        tilPassword.error = error
                    }
                }

                // check if key is password_confirmation
                if (key == "password") {
                    // loop through password errors and append errors to error message
                    for (error in value) {
                        tilConfirmPassword.error = error
                    }
                }
            }
        }
    }

    /**
     * Perform register action
     */
    private fun register() {
        showSpinner()

        // Create Login object
        val data = RegisterData(
            name = tilName.editText?.text.toString(),
            email = tilEmail.editText?.text.toString(),
            password = tilPassword.editText?.text.toString(),
            passwordConfirmation = tilConfirmPassword.editText?.text.toString()
        )

        // Get reference to API
        val call = RetrofitInitializer().userAuthService()?.register(data)

        call?.enqueue(object : Callback<AuthResponse?> {
            override fun onResponse(call: Call<AuthResponse?>, response: Response<AuthResponse?>) {
                hideSpinner()
                if (response.isSuccessful) {
                    // Get response body
                    val userAuth = response.body()

                    // If there is a token, store it and go to main activity
                    userAuth?.token?.let { token ->
                        sharedPreferences.store("TOKEN", token)
                        goToMainActivity()
                    }

                    return
                }

                // unauthorized or unprocessable content
                if (response.code() == 401 || response.code() == 422) {
                    // Request is invalid, handle errors
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                    // handle errors
                    handleErrors(errorResponse)
                }
            }

            override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                hideSpinner()
            }
        })
    }

    /**
     * Start the loading spinner
     */
    private fun showSpinner() {
        loading = true

        // show loading spinner
        auth_overlay.visibility = View.VISIBLE

        // block UI
        btnAction.isEnabled = false
    }

    /**
     * Stop the loading spinner
     */
    private fun hideSpinner() {
        loading = false

        // hide loading spinner
        auth_overlay.visibility = View.GONE

        // enable UI
        btnAction.isEnabled = true
    }

    /**
     * Clear errors
     */
    private fun clearErrors() {
        tilName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
        tvAuthError.visibility = View.GONE
        tvAuthError.text = null
    }

    /**
     * Clear fields
     */
    private fun clearFields() {
        tilName.editText?.text = null
        tilEmail.editText?.text = null
        tilPassword.editText?.text = null
        tilConfirmPassword.editText?.text = null
    }

    /**
     * Clear focus
     */
    private fun clearFocus() {
        tilName.clearFocus()
        tilEmail.clearFocus()
        tilPassword.clearFocus()
        tilConfirmPassword.clearFocus()
    }

    /**
     * Determine if the fragment is in login mode based on UI state
     *
     * @return true if the fragment is in login mode, false otherwise
     */
    private fun isLoginMode(): Boolean {
        return tilName.visibility == View.GONE
    }

    /**
     * Perform login or registration action based on UI state
     */
    private fun performAction() {
        // clear errors
        clearErrors()
        // clear focus
        clearFocus()

        if (isLoginMode()) {
            login()
        } else {
            register()
        }
    }
}