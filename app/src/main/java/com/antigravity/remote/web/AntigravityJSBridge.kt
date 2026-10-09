package com.antigravity.remote.web

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.antigravity.remote.data.AccountManager
import com.antigravity.remote.model.UsageStats
import com.antigravity.remote.notification.NotificationHelper

class AntigravityJSBridge(
    private val context: Context,
    private val accountManager: AccountManager,
    private val notificationHelper: NotificationHelper,
    private val onUsageStatsUpdated: ((UsageStats) -> Unit)? = null
) {

    @JavascriptInterface
    fun notifyTaskFinished(title: String, message: String, targetUrl: String?) {
        vibrate(100)
        notificationHelper.showInstanceAlert(
            title = if (title.isBlank()) "Antigravity Remote" else title,
            message = if (message.isBlank()) "Task execution completed" else message,
            targetUrl = targetUrl
        )
    }

    @JavascriptInterface
    fun updateUsageStats(
        instances: Int,
        subagents: Int,
        tokenPercent: Int,
        quotaPercent: Int,
        workspaceName: String?,
        modelName: String?
    ) {
        val stats = UsageStats(
            activeInstancesCount = instances,
            runningSubagentsCount = subagents,
            tokenUsagePercentage = tokenPercent,
            dailyQuotaPercentage = quotaPercent,
            activeWorkspaceName = workspaceName ?: "Android Workspace",
            activeModelName = modelName ?: "Gemini 3.6 Flash (High)"
        )
        accountManager.saveUsageStats(stats)
        onUsageStatsUpdated?.invoke(stats)
    }

    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Antigravity Prompt", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun vibrate(milliseconds: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(milliseconds)
            }
        } catch (e: Exception) {
            // Vibrate permission or hardware check
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
