package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.ErrorResponse
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferencesUtils

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Called when the fragment's view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(requireContext(), "AUTH")

        // Get posts
        getPosts()
    }

    /**
     * Get a list of posts
     *
     * Not all posts come with this request, for it is paginated
     */
    private fun getPosts(page: Int = 1) {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().postService()?.getPosts(token, page)

        call?.enqueue(object : Callback<PostListResponse?> {
            override fun onResponse(call: Call<PostListResponse?>, response: Response<PostListResponse?>) {
                if (response.isSuccessful) {
                    // TODO: inject posts into view
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                } else if (response.code() == 422) {
                    // Request is invalid, handle errors
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                    // TODO: handle errors
                    // handleUpdateErrors(errorResponse)
                }
            }

            override fun onFailure(call: Call<PostListResponse?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Logout
     */
    private fun logout() {
        sharedPreferences.clear("TOKEN")
        goToAuthActivity()
    }

    /**
     * Go to auth activity
     */
    private fun goToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }
}