package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.ui.adapter.PostListAdapter
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {
    // Get UI elements
    private lateinit var btnCreatePost: ExtendedFloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferencesUtils

    // Initialize variables
    private var page: Int = 1

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

        // Get UI elements
        btnCreatePost = requireActivity().findViewById(R.id.fabCreatePost)

        // Set click listeners
        btnCreatePost.setOnClickListener { goToPostForm() }

        // Get reference to recycler view
        recyclerView = requireActivity().findViewById(R.id.rv_posts)

        // Set scroll listener to recycler view
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Called when the scroll state changes
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // If the user is at the bottom of the recycler view
                if (!recyclerView.canScrollVertically(1)) {
                    // Get posts with next page
                    getPosts(page)
                }
            }
        })

        // Get posts
        getPosts(page)
    }

    /**
     * Go to post form
     */
    private fun goToPostForm() {
        // show a simple dialog sayin' hello world
        DialogFragment()
            .show(requireActivity().supportFragmentManager, "dialog")
    }

    /**
     * Get a list of posts
     *
     * Not all posts come with this request, for it is paginated
     */
    private fun getPosts(page: Int) {
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().postService()?.getPosts(token, page)

        call?.enqueue(object : Callback<PostListResponse?> {
            override fun onResponse(call: Call<PostListResponse?>, response: Response<PostListResponse?>) {
                if (response.isSuccessful) {
                    if (response.body()?.data.isNullOrEmpty()) {
                        // TODO: Handle no posts
                        return
                    }
                    // Increment page
                    this@HomeFragment.page = response.body()?.currentPage!! + 1
                    // Configure posts to be shown in the view
                    configurePosts(response.body()!!)
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                }
            }

            override fun onFailure(call: Call<PostListResponse?>, t: Throwable) {
                logout()
            }
        })
    }

    /**
     * Configure posts to be shown in the view
     */
    private fun configurePosts(posts: PostListResponse) {
        // Set adapter which will handle the posts
        recyclerView.adapter = PostListAdapter(posts, requireContext())
        // Set layout manager which will handle the posts' layout in the view
        val layoutManager = StaggeredGridLayoutManager( 1, StaggeredGridLayoutManager.VERTICAL)
        // Set layout manager to recycler view
        recyclerView.layoutManager = layoutManager
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