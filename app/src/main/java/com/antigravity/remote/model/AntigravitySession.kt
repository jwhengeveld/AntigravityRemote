package com.antigravity.remote.model

import java.util.UUID

data class AntigravitySession(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var workspaceName: String = "Android Workspace",
    var accountId: String,
    var url: String = "https://antigravity.google.com/",
    var isRunningSubagent: Boolean = false,
    var lastActiveTimestamp: Long = System.currentTimeMillis()
)
