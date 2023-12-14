package com.danielgraca.blog_dam_app.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.ui.fragment.PostDetailFragment
import com.squareup.picasso.Picasso

/**
 * Adapter for the post list
 *
 * @param posts The list of posts
 * @param listener The click listener
 * @param context The context
 *
 *
 * DISCLAIMER:
 * How to implement setOnClickListener for RecyclerView.Adapter
 * followed from the following article
 * https://antonioleiva.com/recyclerview-listener/
 */
class PostListAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    // Holds the posts
    private var posts: MutableList<PostResponse>? = null

    /**
     * Creates a new view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the view holder to the post
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (itemCount > 0) {
            // Get post
            val post = posts?.get(position)

            // Bind view holder to post
            holder.bindView(post)
            // Sets click listener
            holder.itemView.setOnClickListener {
                // Go to post details with given id
                goToPostDetails(post?.id!!)
            }
        }
    }

    /**
     * Go to post details
     *
     * @param id The post's id
     */
    private fun goToPostDetails(id: Int) {
        // instantiate fragment transaction so we can send ID to the fragment
        val postDetailFragment = PostDetailFragment.newInstance(id)
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, postDetailFragment)
        transaction.commit()
    }

    /**
     * Returns the number of posts
     */
    override fun getItemCount(): Int {
        return posts!!.size
    }

    /**
     * Sets the posts
     *
     * When a new list of posts is fetched, the old list is cleared and the new one is added
     */
    fun setPosts(posts: MutableList<PostResponse>?) {
        this.posts = posts
        notifyDataSetChanged()
    }

    /**
     * Holds the views for each post
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * Binds the view holder to the post
         */
        fun bindView(post: PostResponse?) {
            // Get UI elements
            val title: TextView = itemView.findViewById(R.id.tvPostTitle)
            val authorName: TextView = itemView.findViewById(R.id.tvPostAuthorName)
            val body: TextView = itemView.findViewById(R.id.tvPostBody)
            val image: ImageView = itemView.findViewById(R.id.ivPostImage)

            // Set UI elements
            title.text = post?.title
            authorName.text = post?.author?.name
            body.text = post?.truncatedBody

            // Set image using Picasso library
            Picasso.get().load(RetrofitInitializer().getBaseUrl() + "storage/" + post?.image?.toUri()).into(image)
        }
    }
}