package com.antigravity.remote.data

import android.content.Context
import android.content.SharedPreferences
import com.antigravity.remote.model.AntigravitySession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("antigravity_sessions_pref", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SESSIONS = "key_sessions_list"
        private const val KEY_ACTIVE_SESSION_ID = "key_active_session_id"
        private const val BASE_URL = "https://antigravity.google.com/"
    }

    fun getSessionsForAccount(accountId: String): List<AntigravitySession> {
        val all = getAllSessions()
        return all.filter { it.accountId == accountId }
    }

    fun getAllSessions(): List<AntigravitySession> {
        val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<AntigravitySession>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveSession(session: AntigravitySession) {
        val current = getAllSessions().toMutableList()
        val index = current.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            current[index] = session
        } else {
            current.add(0, session)
        }
        prefs.edit().putString(KEY_SESSIONS, gson.toJson(current)).apply()
    }

    fun removeSession(sessionId: String) {
        val current = getAllSessions().filterNot { it.id == sessionId }
        prefs.edit().putString(KEY_SESSIONS, gson.toJson(current)).apply()
    }

    fun getActiveSession(accountId: String): AntigravitySession {
        val activeId = prefs.getString(KEY_ACTIVE_SESSION_ID, null)
        val accountSessions = getSessionsForAccount(accountId)
        
        return accountSessions.firstOrNull { it.id == activeId }
            ?: accountSessions.firstOrNull()
            ?: createDefaultSession(accountId)
    }

    fun setActiveSession(sessionId: String) {
        prefs.edit().putString(KEY_ACTIVE_SESSION_ID, sessionId).apply()
    }

    fun createDefaultSession(accountId: String): AntigravitySession {
        val session = AntigravitySession(
            title = "Main Remote Session",
            workspaceName = "Android Workspace",
            accountId = accountId,
            url = BASE_URL
        )
        saveSession(session)
        setActiveSession(session.id)
        return session
    }
}
