package com.danielgraca.blog_dam_app.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.LoginData
import com.danielgraca.blog_dam_app.model.AuthResponse
import com.danielgraca.blog_dam_app.model.RegisterData
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthActivity : AppCompatActivity() {

    // UI elements
    private lateinit var btnAction: MaterialButton;
    private lateinit var tilName: TextInputLayout;
    private lateinit var tilEmail: TextInputLayout;
    private lateinit var tilPassword: TextInputLayout;
    private lateinit var tilConfirmPassword: TextInputLayout;
    private lateinit var mtvToggleMode: MaterialTextView;
    private lateinit var mtvErrorMessage: MaterialTextView;
    private lateinit var mtvNameError: MaterialTextView;
    private lateinit var mtvEmailError: MaterialTextView;
    private lateinit var mtvPasswordError: MaterialTextView;
    private lateinit var sharedPreferences: SharedPreferencesUtils;

    /**
     * Called when the activity is starting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Get references to UI elements
        btnAction = findViewById(R.id.btnAction)
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        mtvToggleMode = findViewById(R.id.mtvToggleMode)
        mtvErrorMessage = findViewById(R.id.mtvErrorMessage)
        mtvNameError = findViewById(R.id.mtvNameError)
        mtvEmailError = findViewById(R.id.mtvEmailError)
        mtvPasswordError = findViewById(R.id.mtvPasswordError)


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

    private fun errorMessage(msg: String, code: Int = 0) {
        mtvErrorMessage.text = msg
        mtvErrorMessage.visibility = if (code == 1) View.VISIBLE else View.GONE
    }

    /**
     * Perform login action
     */
    private fun login() {
        // clear error messages
        mtvNameError.visibility = View.GONE
        mtvEmailError.visibility = View.GONE
        mtvPasswordError.visibility = View.GONE

        // clear error message
        errorMessage("")

        // Create Login object
        val data = LoginData(
            tilEmail.editText?.text.toString(),
            tilPassword.editText?.text.toString()
        )

        // Get reference to API
        val call = RetrofitInitializer().userAuthService()?.login(data)

        call?.enqueue(object : Callback<AuthResponse?> {
            override fun onResponse(call: Call<AuthResponse?>, response: Response<AuthResponse?>) {
                if (response.isSuccessful) {
                    // Get response body
                    val userAuth = response.body()

                    // If there is a token, store it and go to main activity
                    userAuth?.token?.let { token ->
                        sharedPreferences.store("TOKEN", token)
                        goToMainActivity()
                    }

                    // Check if there is a message and display it
                    userAuth?.message?.let { message ->
                        errorMessage(message, 1)
                    }

                    // Check if there are errors and display them
                    userAuth?.errors?.let { errors ->
                        handleErrors(errors)
                    }
                } else {
                    errorMessage("Error: ${response.code()}", 1)
                }
            }

            override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                errorMessage("Critical error: ${t.cause}", 1)
            }
        })
    }

    /**
     * Perform register action
     */
    private fun register() {
        // clear error messages
        mtvNameError.visibility = View.GONE
        mtvEmailError.visibility = View.GONE
        mtvPasswordError.visibility = View.GONE

        // clear error message
        errorMessage("")

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
                if (response.isSuccessful) {
                    // Get response body
                    val userAuth = response.body()

                    // If there is a token, store it and go to main activity
                    userAuth?.token?.let { token ->
                        sharedPreferences.store("TOKEN", token)
                        goToMainActivity()
                    }

                    // Check if there is a message and display it
                    userAuth?.message?.let { message ->
                        errorMessage(message, 1)
                    }

                    // Check if there are errors and display them
                    userAuth?.errors?.let { errors ->
                        handleErrors(errors)
                    }
                } else {
                    errorMessage("Error: ${response.code()}", 1)
                }
            }

            override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                errorMessage("Critical error: ${t.cause}", 1)
            }
        })
    }

    /**
     * Handle auth request errors
     */
    private fun handleErrors(errors: Map<String, List<String>>) {
        errors.forEach { (field, errorList) ->
            if (field == "name") {
                mtvNameError.text = errorList[0]
                mtvNameError.visibility = View.VISIBLE
            } else if (field == "email") {
                mtvEmailError.text = errorList[0]
                mtvEmailError.visibility = View.VISIBLE
            } else if (field == "password") {
                mtvPasswordError.text = errorList[0]
                mtvPasswordError.visibility = View.VISIBLE
            }
            errorList.forEach { error ->
                println("  Error: $error")
            }
        }
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
        if (isLoginMode()) {
            login()
        } else {
            // make sure there is no token in shared preferences
            sharedPreferences.clear("TOKEN")
            register()
        }
    }
}