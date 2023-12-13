package com.danielgraca.blog_dam_app.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.danielgraca.blog_dam_app.R
import com.danielgraca.blog_dam_app.model.response.PostResponse
import com.danielgraca.blog_dam_app.retrofit.RetrofitInitializer
import com.danielgraca.blog_dam_app.utils.SharedPreferencesUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream



class PostFormFragment : Fragment() {
    // Get UI elements
    private lateinit var tiPostFormTitle: TextInputLayout
    private lateinit var tiPostFormContent: TextInputLayout
    private lateinit var tvPostImageError: TextView
    private lateinit var btnPhotoForm: MaterialButton
    private lateinit var btnSendForm: MaterialButton
    private lateinit var ivPostImageForm: ImageView
    private lateinit var sharedPreferences: SharedPreferencesUtils

    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_form, container, false)
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
        tiPostFormTitle = requireActivity().findViewById(R.id.tiPostFormTitle)
        tiPostFormContent = requireActivity().findViewById(R.id.tiPostFormContent)

        tvPostImageError = requireActivity().findViewById(R.id.tvPostImageError)
        tvPostImageError.text = getString(R.string.image_required)

        btnPhotoForm = requireActivity().findViewById(R.id.btnPhotoForm)
        btnSendForm = requireActivity().findViewById(R.id.btnSendForm)
        ivPostImageForm = requireActivity().findViewById(R.id.ivPostImageForm)

        // Set click listeners
        btnPhotoForm.setOnClickListener { capturePhoto() }
        btnSendForm.setOnClickListener { createPost() }
    }

    /**
     * Called when the user clicks the photo button
     */
    private fun capturePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(cameraIntent)
    }

    /**
     * Handles the result of the camera intent
     */
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            ivPostImageForm.setImageBitmap(data?.extras?.get("data") as Bitmap)
        }
    }

    /**
     * Create a post with the given data
     */
    private fun createPost() {
        // Checks if there are fields not filled
        if(checkForm()) return

        // Get user token
        val token = "Bearer ${sharedPreferences.get("TOKEN")}"

        // Get post data
        val title = tiPostFormTitle.editText?.text.toString()
        val body = tiPostFormContent.editText?.text.toString()

        // Get the bitmap from the ImageView
        val bitmap = (ivPostImageForm.drawable as BitmapDrawable).bitmap

        // Convert the bitmap to a file
        val imageFile = convertBitmapToFile(bitmap)

        // Create a MultipartBody.Part from the image file
        val requestFile: RequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val titlePart: RequestBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val bodyPart: RequestBody = body.toRequestBody("text/plain".toMediaTypeOrNull())

        // Create post
        val call = RetrofitInitializer().postService()?.create(token, titlePart, bodyPart, imagePart)

        // Set callback
        call?.enqueue(object : Callback<PostResponse?> {
            override fun onResponse(call: Call<PostResponse?>, response: Response<PostResponse?>) {
                // If the request is successful
                if (response.isSuccessful) {
                    // Return to posts fragment
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostsFragment()).commit()
                }
            }

            override fun onFailure(call: Call<PostResponse?>, t: Throwable) {
                // Show error message
                Toast.makeText(
                    requireContext(),
                    "Não foi possível criar o post",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Check if all fields are filled
     *
     * Fields: title, content, image
     *
     * @return true if there is an error, false otherwise
     */
    private fun checkForm(): Boolean {
        var error = false;
        tvPostImageError.visibility = View.GONE
        tiPostFormTitle.error = null
        tiPostFormContent.error = null

        // Check if title is filled
        if (tiPostFormTitle.editText?.text.isNullOrEmpty()) {
            tiPostFormTitle.error = getString(R.string.field_required)
            error = true
        }

        // Check if content is filled
        if (tiPostFormContent.editText?.text.isNullOrEmpty()) {
            tiPostFormContent.error = getString(R.string.field_required)
            error = true
        }

        // Check if image is filled
        if (ivPostImageForm.drawable == null) {
            tvPostImageError.visibility = View.VISIBLE
            error = true
        }

        return error;
    }

    /**
     * Converts a Bitmap to a File
     */
    private fun convertBitmapToFile(bitmap: Bitmap): File {
        // Create a file to write bitmap data
        val file = File(requireContext().cacheDir, "image.jpg")
        file.createNewFile()

        // Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData: ByteArray = bos.toByteArray()

        // Write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }
}