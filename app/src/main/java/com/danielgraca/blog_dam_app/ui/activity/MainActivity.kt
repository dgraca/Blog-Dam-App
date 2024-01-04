package com.danielgraca.blog_dam_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.UserEditResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.fragment.AboutMeFragment
import com.danielgraca.blog_dam_app.ui.fragment.PostsFragment
import com.danielgraca.blog_dam_app.ui.fragment.UserFragment
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Main activity
 *
 * DISCLAIMER:
 * The navigation drawer is implemented using the following tutorial:
 * https://www.youtube.com/watch?v=zBETkYi9Z4E
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferencesUtils

    /**
     * Called when the activity is starting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get reference to drawer layout
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.toolbar)

        // Initialize and set the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize and set the navigation view
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Create and add a toggle for the navigation drawer
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation, R.string.close_navigation)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set the home fragment as the default fragment
        if (savedInstanceState == null) {
            replaceFragment(PostsFragment())
            navigationView.setCheckedItem(R.id.navigation_home)
        }

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(this, "AUTH")
    }

    /**
     * Called when an item in the navigation menu is selected
     */
    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        // Handle navigation view item clicks here
        when (item.itemId) {
            R.id.navigation_home -> {
                // if fragment is already the home fragment, do nothing
                if (supportFragmentManager.findFragmentById(R.id.fragment_container) is PostsFragment) {
                    // close navigation drawer
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }
                replaceFragment(PostsFragment())
            }
            R.id.navigation_user -> {
                replaceFragment(UserFragment())
            }
            R.id.navigation_about -> {
                replaceFragment(AboutMeFragment())
            }
            R.id.navigation_logout -> {
                logout()
                finish()
            }
        }

        // Close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    /**
     * Called when the user presses the back button
     */
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // If the navigation drawer is open, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (fragment is PostsFragment) {
            // If the current fragment is the home fragment, close the app
            finish()
        } else {
            // If the current fragment is not the home fragment, go back on the fragment stack
            super.onBackPressed()
        }
    }



    /**
     * Replaces the current fragment with the provided fragment and adds it to the back stack
     */
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // Add the transaction to the back stack
        transaction.commit()
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
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().userService()?.get(token)

        call?.enqueue(object : Callback<UserEditResponse?> {
            override fun onResponse(call: Call<UserEditResponse?>, response: Response<UserEditResponse?>) {
                if (response.isSuccessful) {
                    // set navigation_username with name from response
                    findViewById<TextView>(R.id.tvUserName).text = response.body()?.name
                    // set navigation_email with email from response
                    findViewById<TextView>(R.id.tvUserEmail).text = response.body()?.email
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
        sharedPreferences.clear("TOKEN")
        goToAuthActivity()
    }
}