package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.ui.adapter.PostListAdapter
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PostsFragment : Fragment() {
    // Get UI elements
    private lateinit var btnCreatePost: ExtendedFloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferencesUtils

    // Initialize variables
    private var page: Int = 1
    private var fetching: Boolean = true
    private var loading: Boolean = false
    private var allPosts: MutableList<PostResponse>? = null

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false)
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

        // Get posts
        getPosts(page)

        // Set scroll listener to recycler view
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Called when the scroll state changes
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // If the user is at the bottom of the recycler view
                if (fetching && !loading && !recyclerView.canScrollVertically(1)) {
                    // Get posts with next page
                    getPosts(page)
                }
            }
        })
    }

    /**
     * Get a list of posts
     *
     * Not all posts come with this request, for it is paginated
     */
    private fun getPosts(page: Int) {
        // Set loading to true
        loading = true

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().postService()?.getPosts(token, page)

        call?.enqueue(object : Callback<PostListResponse?> {
            override fun onResponse(call: Call<PostListResponse?>, response: Response<PostListResponse?>) {
                if (response.isSuccessful) {
                    if (response.body()?.data.isNullOrEmpty()) {
                        return
                    }

                    // Check if there are more posts to be fetched
                    if (response.body()?.currentPage!! >= response.body()?.lastPage!!) {
                        fetching = false
                    }

                    // prepare posts (add them to an existing list or create a new one)
                    preparePosts(response)

                    // Set loading to false
                    loading = false
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                }
            }

            override fun onFailure(call: Call<PostListResponse?>, t: Throwable) {
                // TODO: handle on failure
            }
        })
    }

    /**
     * Prepare posts to be shown in the view
     */
    private fun preparePosts(response: Response<PostListResponse?>) {
        // Get current page from response
        val requestPage = response.body()?.currentPage!!

        // If it's the first page, create a new list
        if (requestPage == 1) {
            configureRecyclerView()
            allPosts = mutableListOf()
        }

        // Loop through the list of posts
        for (post in response.body()!!.data!!) {
            // Add post to the list
            allPosts?.add(post)
        }

        // Set posts to recycler view
        recyclerView.adapter?.let {
            (it as PostListAdapter).setPosts(allPosts)
        }

        if (requestPage > 1) {
            // Scroll from current position to 200 pixels down
            // This way the user knows that there are more posts
            recyclerView.smoothScrollBy(0, 1000)
        }

        // Increment page
        this@PostsFragment.page = requestPage + 1
    }

    /**
     * Configure posts to be shown in the view
     */
    private fun configureRecyclerView() {
        // Set adapter which will handle the posts with it's item clicker listener
        recyclerView.adapter = PostListAdapter(requireContext(), requireActivity())
        // Set layout manager which will handle the posts' layout in the view
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        // Set layout manager to recycler view
        recyclerView.layoutManager = layoutManager
    }

    /**
     * Go to post details
     *
     * @param id The post's id
     */
    private fun goToPostForm() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, PostFormFragment())
        transaction.commit()
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