package com.neatroots.newdog.Model

data class NotiDog(
    val id: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    var isRead: Boolean = false
)