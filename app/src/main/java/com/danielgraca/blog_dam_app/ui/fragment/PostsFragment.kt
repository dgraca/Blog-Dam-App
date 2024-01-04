package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
    private lateinit var posts_overlay: RelativeLayout
    private lateinit var swipeRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    // Initialize variables
    private var page: Int = 1
    private var fetching: Boolean = true // if there are more posts to be fetched
    private var loading: Boolean = false // if the app is loading posts
    private var allPosts: MutableList<PostResponse>? = null

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_posts, container, false)

        // Get reference to recycler view
        recyclerView = view.findViewById(R.id.rv_posts)

        // Initialize the adapter
        val adapter = PostListAdapter(requireContext(), requireActivity())

        // Set the adapter to the RecyclerView
        recyclerView.adapter = adapter

        return view
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
        posts_overlay = requireActivity().findViewById(R.id.posts_overlay)

        // Set click listeners
        btnCreatePost.setOnClickListener { goToPostForm() }

        // Get reference to recycler view
        recyclerView = requireActivity().findViewById(R.id.rv_posts)

        // Set swipe to refresh component
        swipeRefresh = requireActivity().findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh_layout)

        swipeRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            reset()
            // Stop refreshing
            swipeRefresh.isRefreshing = false
        })

        // Get posts
        getPosts(page)

        // Set adapter for the RecyclerView
        recyclerView.adapter = PostListAdapter(requireContext(), requireActivity())

        // Set scroll listener to recycler view
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Called when the scroll state changes
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // If the user is at the bottom of the recycler view
                if (fetching && !loading && !recyclerView.canScrollVertically(1)) {
                    // Increment page
                    this@PostsFragment.page += 1
                    // Get posts with next page
                    getPosts(page)
                }
            }
        })
    }

    private fun reset() {
        // Reset page
        page = 1

        // Set fetching to true
        fetching = true

        // Initialize or clear posts
        if (allPosts == null) {
            allPosts = mutableListOf()
        } else {
            allPosts?.clear()
        }

        // Clear adapter
        recyclerView.adapter?.let {
            (it as PostListAdapter).setPosts(allPosts)
        }

        // Get posts
        getPosts(page)
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    /**
     * Get a list of posts
     *
     * Not all posts come with this request, for it is paginated
     */
    private fun getPosts(page: Int) {
        showSpinner()

        // Set loading to true
        loading = true

        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get reference to API
        val call = RetrofitInitializer().postService()?.getPosts(token, page)

        call?.enqueue(object : Callback<PostListResponse?> {
            override fun onResponse(call: Call<PostListResponse?>, response: Response<PostListResponse?>) {
                if (response.isSuccessful) {
                    if (response.body()?.data.isNullOrEmpty()) {
                        hideSpinner()
                        return
                    }

                    // Check if there are more posts to be fetched
                    fetching = response.body()?.currentPage!! < response.body()?.lastPage!!

                    // prepare posts (add them to an existing list or create a new one)
                    preparePosts(response)

                    // Set loading to false
                    loading = false
                } else if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                }

                // Unlike the other requests, this one hides the spinner AFTER it prepares the posts.
                // This happens because, after the posts are prepared, it will scroll down some pixels.
                // If the spinner is hidden first, it assumes that the user is at the bottom of the recycler view
                // and fetches the same data again.
                hideSpinner()
            }

            override fun onFailure(call: Call<PostListResponse?>, t: Throwable) {
                hideSpinner()
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
            recyclerView.smoothScrollBy(0, 800)
        }
    }

    /**
     * Configure posts to be shown in the view
     */
    private fun configureRecyclerView() {
        // Set layout manager which will handle the posts' layout in the view
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        // Set layout manager to recycler view
        recyclerView.layoutManager = layoutManager
    }

    /**
     * Start the loading spinner
     */
    private fun showSpinner() {
        loading = true

        // show loading spinner
        posts_overlay.visibility = View.VISIBLE

        // block UI
        btnCreatePost.isEnabled = false
    }

    /**
     * Stop the loading spinner
     */
    private fun hideSpinner() {
        loading = false

        // hide loading spinner
        posts_overlay.visibility = View.GONE

        // enable UI
        btnCreatePost.isEnabled = true
    }

    /**
     * Go to post form
     */
    private fun goToPostForm() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, PostFormFragment())
        transaction.addToBackStack(null) // Add the transaction to the back stack
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