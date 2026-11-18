package com.stb.sirabas.data.remote

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    companion object {
        private const val PREF_NAME = "sirabas_api_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
