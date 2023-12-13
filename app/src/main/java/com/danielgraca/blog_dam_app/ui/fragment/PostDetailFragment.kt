package com.danielgraca.blog_dam_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.AuthorResponse
import com.danielgraca.blog_dam_app.model.response.ErrorResponse
import com.danielgraca.blog_dam_app.model.response.GenericResponse
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.activity.AuthActivity
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
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

    private lateinit var postAuthor: AuthorResponse

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
     * Create the toolbar menu with the buttons
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        // Find the menu items by their IDs
        val editItem = menu.findItem(R.id.toolbar_action_edit)
        val deleteItem = menu.findItem(R.id.toolbar_action_delete)

        // Set the visibility of the menu items
        editItem.isVisible = false // TODO: CHANGE TO TRUE WHEN MAKING EDIT ACTION
        deleteItem.isVisible = true

        // Set color of the menu items
        // TODO: Implement EDIT action if feeling like it
        // editItem.icon?.setTint(resources.getColor(R.color.white))
        deleteItem.icon?.setTint(resources.getColor(R.color.white))

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // TODO: Implement EDIT action if feeling like it
//            R.id.toolbar_action_edit -> {
//                // TODO: Handle edit action
//            }
            R.id.toolbar_action_delete -> {
                deletePost()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    /**
     * Delete post
     */
    private fun deletePost() {
        // Get token
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get post
        val call = RetrofitInitializer().postService()?.deletePost(token!!, arguments?.getInt(ARG_POST_ID)!!)

        // Make request
        call?.enqueue(object : Callback<GenericResponse?> {
            override fun onResponse(call: Call<GenericResponse?>, response: Response<GenericResponse?>) {
                if (response.isSuccessful) {
                    // Go to posts fragment
                    goToPostsFragment(response.body()?.message.toString())
                    return
                }
                if (response.code() == 401) {
                    // User is not authenticated
                    logout()
                    return
                }
                if (response.code() == 500) {
                    // Request is invalid, handle errors
                    // get error body
                    val errorBody = response.errorBody()?.string()
                    // parse error body to UserEditErrorResponse
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    // handle errors
                    handleDeleteError(errorResponse.message.toString())
                    return
                }
            }

            override fun onFailure(call: Call<GenericResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * Handle delete error
     */
    private fun handleDeleteError(message: String) {
        // Show dialog with "Ok" button
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Erro ao eliminar publicação")
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, which ->
                // Do Nothing
            }
            .show()
    }

    /**
     * Go to posts fragment
     */
    private fun goToPostsFragment(message: String) {
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, PostsFragment())
        transaction.commit()

        // Show Toast notification
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
                    // Sets post author
                    postAuthor = response.body()?.author!!

                    // Checks if the user is the author of the post
                    if (sharedPreferences.get("USER:email") == postAuthor.email) {
                        // show edit and delete buttons
                        setHasOptionsMenu(true)
                    }

                    tvPostDetailTitle.text = response.body()?.title
                    tvPostDetailAuthorName.text = response.body()?.author?.name
                    tvPostDetailBody.text = response.body()?.body

                    // Set image using Picasso library
                    Picasso.get().load(RetrofitInitializer().getBaseUrl() + "storage/" + response.body()?.image?.toUri()).into(ivPostDetailImage)
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