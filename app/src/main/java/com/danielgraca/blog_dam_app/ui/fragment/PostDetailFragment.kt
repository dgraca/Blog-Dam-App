package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Arguments passed into the fragment
private const val ARG_POST_ID = "postId"

class PostDetailFragment(postId: Int) : Fragment() {
    // Get UI elements
    private lateinit var ivPostDetailImage: ImageView
    private lateinit var tvPostDetailTitle: TextView
    private lateinit var tvPostDetailAuthorName: TextView
    private lateinit var tvPostDetailBody: TextView
    private lateinit var sharedPreferences: SharedPreferencesUtils

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    /**
     * Called when the fragment's view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences utils
        sharedPreferences = SharedPreferencesUtils
        sharedPreferences.init(requireContext(), "AUTH")

        // Get arguments
        val postId = arguments?.getInt(ARG_POST_ID)

        // Get UI elements
        ivPostDetailImage = view.findViewById(R.id.ivPostDetailImage)
        tvPostDetailTitle = view.findViewById(R.id.tvPostDetailTitle)
        tvPostDetailAuthorName = view.findViewById(R.id.tvPostDetailAuthorName)
        tvPostDetailBody = view.findViewById(R.id.tvPostDetailBody)

        // Get post
        getPost(postId!!)
    }

    /**
     * Get post data
     */
    private fun getPost(postId: Int) {
        // Get token
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get post
        val call = RetrofitInitializer().postService()?.getPost(token!!, postId)

        // Make request
        call?.enqueue(object : Callback<PostResponse?> {
            override fun onResponse(call: Call<PostResponse?>, response: Response<PostResponse?>) {
                if (response.isSuccessful) {
                    tvPostDetailTitle.text = response.body()?.title
                    tvPostDetailAuthorName.text = response.body()?.author?.name
                    tvPostDetailBody.text = response.body()?.body
                    // Set image using Picasso library
                    Picasso.get().load(response.body()?.image?.toUri()).into(ivPostDetailImage)
                } else if (response.code() == 401) {
                    // User is not authenticated
                    // logout()
                }
            }

            override fun onFailure(call: Call<PostResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * Logout
     */
    private fun logout() {
        // Clear token
        sharedPreferences.clear("TOKEN")
        // Go to login
        goToAuthActivity()
    }

    /**
     * Go to auth activity
     */
    private fun goToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }

    /**
     * New instance of the fragment
     */
    companion object {
        @JvmStatic
        fun newInstance(postId: Int) =
            PostDetailFragment(postId).apply {
                arguments = Bundle().apply {
                    putInt(ARG_POST_ID, postId)
                }
            }
    }
}