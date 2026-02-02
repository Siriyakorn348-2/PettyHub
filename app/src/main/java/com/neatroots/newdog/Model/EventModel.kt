package com.neatroots.newdog.Model

data class EventModel(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val description: String = "",
    val notification: Boolean = false,
    val color: Int = 0
)
