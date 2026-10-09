package com.antigravity.remote.model

import java.util.UUID

data class AntigravityAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    var cookiesSerialized: String = "",
    val customUrl: String = "https://antigravity.google.com/",
    var avatarColorHex: String = "#1A73E8",
    var lastActiveTimestamp: Long = System.currentTimeMillis()
)
