package com.example.pomodoro

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int) {
        prefs.edit().putInt("KEY_USER_ID", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("KEY_USER_ID", -1)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun saveActiveTaskId(taskId: Int) {
        prefs.edit().putInt("ACTIVE_TASK_ID", taskId).apply()
    }

    fun getActiveTaskId(): Int {
        return prefs.getInt("ACTIVE_TASK_ID", -1)
    }
}