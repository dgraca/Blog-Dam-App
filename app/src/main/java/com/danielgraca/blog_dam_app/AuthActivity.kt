package com.danielgraca.blog_dam_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // If the activity is being created for the first time
        if (savedInstanceState == null) {
            // Create a new AuthFragment
            val authFragment = AuthFragment()
            // Add the fragment to the 'container' FrameLayout
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, authFragment)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    /**
     * Check if user has token stored in shared preferences
     *
     * @return true if user has token, false otherwise
     */
    fun hasToken(): Boolean {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null) != null
    }

    /**
     * Go to MainActivity
     *
     * Creates an intent to start MainActivity and finishes AuthActivity
     */
    fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}