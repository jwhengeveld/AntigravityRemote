package com.antigravity.remote.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import com.antigravity.remote.R
import com.antigravity.remote.data.AccountManager
import com.google.android.material.bottomsheet.BottomSheetDialog

object UsageBottomSheet {

    fun showUsageAnalytics(
        context: Context,
        accountManager: AccountManager
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_usage_analytics, null)

        val activeAccount = accountManager.getActiveAccount()
        val stats = accountManager.getUsageStats()

        view.findViewById<TextView>(R.id.txtAccountName).text = activeAccount?.name ?: "Default Account"
        view.findViewById<TextView>(R.id.txtAccountEmail).text = activeAccount?.email ?: "user@gmail.com"
        view.findViewById<TextView>(R.id.txtWorkspaceName).text = stats.activeWorkspaceName
        view.findViewById<TextView>(R.id.txtActiveModel).text = stats.activeModelName
        view.findViewById<TextView>(R.id.txtActiveInstances).text = "${stats.activeInstancesCount} Instance(s) Active"
        view.findViewById<TextView>(R.id.txtSubagentsRunning).text = "${stats.runningSubagentsCount} Subagent Tasks Running"

        val progressToken = view.findViewById<ProgressBar>(R.id.progressTokenUsage)
        val txtToken = view.findViewById<TextView>(R.id.txtTokenPercent)
        progressToken.progress = stats.tokenUsagePercentage
        txtToken.text = "${stats.tokenUsagePercentage}%"

        val progressQuota = view.findViewById<ProgressBar>(R.id.progressQuotaUsage)
        val txtQuota = view.findViewById<TextView>(R.id.txtQuotaPercent)
        progressQuota.progress = stats.dailyQuotaPercentage
        txtQuota.text = "${stats.dailyQuotaPercentage}%"

        dialog.setContentView(view)
        dialog.show()
    }
}
