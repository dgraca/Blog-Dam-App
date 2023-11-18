package com.danielgraca.blog_dam_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 * Use the [AuthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuthFragment : Fragment() {
    private var name: String? = null
    private var email: String? = null
    private var password: String? = null
    private var passwordConfirmation: String? = null

    /**
     * Called when the fragment is first attached to its context
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    /**
     * Create the view for this fragment, using the arguments given to it
     * This is only called once when the fragment is first created
     *
     * @return Return the View for the fragment's UI
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        // Get references to UI elements
        val btnAction = view.findViewById<Button>(R.id.btnAction)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val tvToggleMode = view.findViewById<TextView>(R.id.tvToggleMode)

        // Set up the appropriate UI and behavior for login or registration
        btnAction.setOnClickListener {
            if (isLoginMode()) {
                // Perform login action
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                // TODO: Handle login logic

            } else {
                // Perform registration action
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                // TODO: Handle registration logic
            }
        }

        // Toggle between login and registration UI
        tvToggleMode.setOnClickListener {
            toggleMode()
        }

        return view
    }

    /**
     * Toggle between login and registration UI
     */
    private fun toggleMode() {
        // Get references to UI elements
        val btnAction = view?.findViewById<Button>(R.id.btnAction)
        val etName = view?.findViewById<EditText>(R.id.etName)
        val etConfirmPassword = view?.findViewById<EditText>(R.id.etConfirmPassword)
        val tvToggleMode = view?.findViewById<TextView>(R.id.tvToggleMode)

        if (isLoginMode()) {
            // Switch to registration mode
            etName?.visibility = View.VISIBLE
            etConfirmPassword?.visibility = View.VISIBLE
            btnAction?.text = getString(R.string.register)
            tvToggleMode?.text = getString(R.string.switch_to_login)
        } else {
            // Switch to login mode
            etName?.visibility = View.GONE
            etConfirmPassword?.visibility = View.GONE
            btnAction?.text = getString(R.string.login)
            tvToggleMode?.text = getString(R.string.switch_to_register)
        }
    }

    /**
     * Determine if the fragment is in login mode based on UI state
     *
     * @return true if the fragment is in login mode, false otherwise
     */
    private fun isLoginMode(): Boolean {
        // Get references to UI elements
        val etName = view?.findViewById<EditText>(R.id.etName)

        // Determine if the fragment is in login mode based on UI state
        return etName?.visibility == View.GONE
    }

    /**
     * Companion object to create new instances of AuthFragment
     *
     * @return new instance of AuthFragment
     */
    companion object {
        fun newInstance(): AuthFragment {
            return AuthFragment()
        }
    }
}