package com.danielgraca.blog_dam_app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.danielgraca.blog_dam_app.R

class UserFragment : Fragment() {

    // UI elements
    private lateinit var etEditUserName: EditText;
    private lateinit var etEditEmail: EditText;
    private lateinit var etEditPassword: EditText;
    private lateinit var btnEditUser: Button;


    /**
     * Called when the activity is starting
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get references to UI elements
        etEditUserName = view?.findViewById<EditText>(R.id.etEditUserName)!!
        etEditEmail = view?.findViewById<EditText>(R.id.etEditEmail)!!
        etEditPassword = view?.findViewById<EditText>(R.id.etEditPassword)!!
        btnEditUser = view?.findViewById<Button>(R.id.btnEditUser)!!

        // TODO: Data binding from the user data from API

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    /**
     * Called when the activity is starting
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listeners
        btnEditUser.setOnClickListener { editUser() }
    }

    /**
     * Called when the user clicks the edit user button
     */
    private fun editUser() {
       Toast.makeText(context, "Edit user", Toast.LENGTH_SHORT).show()
    }
}