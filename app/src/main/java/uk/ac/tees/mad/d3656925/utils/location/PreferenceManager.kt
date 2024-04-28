package uk.ac.tees.mad.d3656925.utils.location

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    fun setIsActiveStatus(isActive: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isActive", isActive)
        editor.apply()
    }

    fun getIsActiveStatus(): Boolean {
        return sharedPreferences.getBoolean("isActive", false)
    }
}