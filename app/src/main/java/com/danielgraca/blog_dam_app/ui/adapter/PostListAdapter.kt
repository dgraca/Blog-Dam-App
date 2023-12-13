package com.danielgraca.blog_dam_app.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostListResponse
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
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
    private val posts: MutableList<PostResponse>?,
    private val context: Context,
    private val clickListener: (PostResponse) -> Unit,
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    private var allPosts: MutableList<PostResponse>? = null

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
                clickListener(post!!)
            }
        }
    }

    /**
     * Returns the number of posts
     */
    override fun getItemCount(): Int {
        return posts!!.size
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