package com.danielgraca.blog_dam_app.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.LoginData
import com.danielgraca.blog_dam_app.model.AuthResponse
import com.danielgraca.blog_dam_app.model.RegisterData
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthActivity : AppCompatActivity() {

    // UI elements
    private lateinit var btnAction: Button;
    private lateinit var etName: EditText;
    private lateinit var etEmail: EditText;
    private lateinit var etPassword: EditText;
    private lateinit var etConfirmPassword: EditText;
    private lateinit var tvToggleMode: TextView;
    private lateinit var tvErrorMessage: TextView;
    private lateinit var tvNameError: TextView;
    private lateinit var tvEmailError: TextView;
    private lateinit var tvPasswordError: TextView;
    private lateinit var sharedPreferences: SharedPreferencesUtils;

    /**
     * Called when the activity is starting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Get references to UI elements
        btnAction = findViewById<Button>(R.id.btnAction)
        etName = findViewById<EditText>(R.id.etName)
        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.etPassword)
        etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        tvToggleMode = findViewById<TextView>(R.id.tvToggleMode)
        tvErrorMessage = findViewById<TextView>(R.id.tvErrorMessage)
        tvNameError = findViewById<TextView>(R.id.tvNameError)
        tvEmailError = findViewById<TextView>(R.id.tvEmailError)
        tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)


        // Set click listeners
        btnAction.setOnClickListener { performAction() }
        tvToggleMode.setOnClickListener { toggleMode() }

        // Initialize and set the toolbar
        val toolbar = findViewById<Toolbar>(R.id.auth_toolbar)
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
            etName.visibility = View.VISIBLE
            etConfirmPassword.visibility = View.VISIBLE
            btnAction.text = getString(R.string.register)
            tvToggleMode.text = getString(R.string.switch_to_login)
        } else {
            // Switch to login mode
            etName.visibility = View.GONE
            etConfirmPassword.visibility = View.GONE
            btnAction.text = getString(R.string.login)
            tvToggleMode.text = getString(R.string.switch_to_register)
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
        tvErrorMessage.text = msg
        tvErrorMessage.visibility = if (code == 1) View.VISIBLE else View.GONE
    }

    /**
     * Perform login action
     */
    private fun login() {
        // clear error messages
        tvNameError.visibility = View.GONE
        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE

        // clear error message
        errorMessage("")

        // Create Login object
        val data = LoginData(
            etEmail.text.toString(),
            etPassword.text.toString()
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
        tvNameError.visibility = View.GONE
        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE

        // clear error message
        errorMessage("")

        // Create Login object
        val data = RegisterData(
            etName.text.toString(),
            etEmail.text.toString(),
            etPassword.text.toString(),
            etConfirmPassword.text.toString()
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
                tvNameError.text = errorList[0]
                tvNameError.visibility = View.VISIBLE
            } else if (field == "email") {
                tvEmailError.text = errorList[0]
                tvEmailError.visibility = View.VISIBLE
            } else if (field == "password") {
                tvPasswordError.text = errorList[0]
                tvPasswordError.visibility = View.VISIBLE
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
        return etName.visibility == View.GONE
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