package com.antigravity.remote.data

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import com.antigravity.remote.model.AntigravityAccount
import com.antigravity.remote.model.UsageStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AccountManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("antigravity_accounts_pref", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ACCOUNTS = "key_accounts_list"
        private const val KEY_ACTIVE_ACCOUNT_ID = "key_active_account_id"
        private const val KEY_USAGE_STATS = "key_usage_stats"
        private const val BASE_URL = "https://antigravity.google.com/"
    }

    fun getAccounts(): List<AntigravityAccount> {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        val type = object : TypeToken<List<AntigravityAccount>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveAccount(account: AntigravityAccount) {
        val current = getAccounts().toMutableList()
        val index = current.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            current[index] = account
        } else {
            current.add(account)
        }
        prefs.edit().putString(KEY_ACCOUNTS, gson.toJson(current)).apply()
    }

    fun removeAccount(accountId: String) {
        val current = getAccounts().filterNot { it.id == accountId }
        prefs.edit().putString(KEY_ACCOUNTS, gson.toJson(current)).apply()
    }

    fun getActiveAccount(): AntigravityAccount? {
        val activeId = prefs.getString(KEY_ACTIVE_ACCOUNT_ID, null)
        val accounts = getAccounts()
        return accounts.firstOrNull { it.id == activeId } ?: accounts.firstOrNull()
    }

    fun setActiveAccount(accountId: String) {
        prefs.edit().putString(KEY_ACTIVE_ACCOUNT_ID, accountId).apply()
    }

    fun saveUsageStats(stats: UsageStats) {
        prefs.edit().putString(KEY_USAGE_STATS, gson.toJson(stats)).apply()
    }

    fun getUsageStats(): UsageStats {
        val json = prefs.getString(KEY_USAGE_STATS, null)
        return if (json != null) {
            gson.fromJson(json, UsageStats::class.java) ?: UsageStats()
        } else {
            UsageStats()
        }
    }

    /**
     * Save current cookies for the currently active account before switching
     */
    fun saveCurrentCookiesForActiveAccount() {
        val activeAccount = getActiveAccount() ?: return
        val cookieManager = CookieManager.getInstance()
        val currentCookies = cookieManager.getCookie(BASE_URL) ?: ""
        
        activeAccount.cookiesSerialized = currentCookies
        activeAccount.lastActiveTimestamp = System.currentTimeMillis()
        saveAccount(activeAccount)
    }

    /**
     * Restore cookies into CookieManager for the target active account
     */
    fun applyCookiesForAccount(account: AntigravityAccount) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(null, true)
        
        // Remove existing session cookies for isolation
        cookieManager.removeAllCookies(null)

        if (account.cookiesSerialized.isNotEmpty()) {
            val cookies = account.cookiesSerialized.split(";")
            for (cookie in cookies) {
                if (cookie.isNotBlank()) {
                    cookieManager.setCookie(BASE_URL, cookie.trim())
                }
            }
        }
        cookieManager.flush()
    }
}
