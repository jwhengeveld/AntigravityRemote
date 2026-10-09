package com.antigravity.remote.model

data class UsageStats(
    var activeInstancesCount: Int = 1,
    var runningSubagentsCount: Int = 0,
    var tokenUsagePercentage: Int = 15,
    var dailyQuotaPercentage: Int = 32,
    var activeWorkspaceName: String = "Android Developer",
    var activeModelName: String = "Gemini 3.6 Flash (High)",
    var lastUpdatedTime: Long = System.currentTimeMillis()
)
