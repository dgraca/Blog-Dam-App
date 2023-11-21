package com.danielgraca.blog_dam_app.utils

import android.content.Context
import android.content.SharedPreferences


/**
 * Shared preferences utility class
 */
object SharedPreferencesUtils {

    // Shared preferences
    private var sharedPreferences: SharedPreferences? = null

    /**
     * Initialize shared preferences with context passed via parameter
     * @param context Context
     * @param name Name of the shared preferences
     */
    fun init(context: Context, name: String) {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * Get value from shared preferences
     * @param value Value to get
     *
     * @return Value from shared preferences
     */
    fun get(value: String): String? {
        return sharedPreferences!!.getString(value, null)
    }

    /**
     * Store key:value in shared preferences
     * @param key Key to store
     * @param value Value to store
     */
    fun store(key: String, value: String) {
        // Get reference to editor
        val editor = sharedPreferences!!.edit()

        // Store key:value in shared preferences
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Clear key:value from shared preferences
     * @param key Key to clear
     */
    fun clear(key: String) {
        // Get reference to editor
        val editor = sharedPreferences!!.edit()

        // Clear key:value from shared preferences
        editor.remove(key)
        editor.apply()
    }
}