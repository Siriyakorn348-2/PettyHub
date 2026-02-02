package com.neatroots.newdog.Model

import android.text.format.DateUtils

class Post {
    var postid: String = ""
    var postImages: List<String>? = null
    var publisher: String = ""
    var description: String = ""
    var username: String = ""
    var dateTime: Long = 0L
    var publisherImage: String = ""
    var publisherName: String = ""
    var timeAgoText: String = ""

    constructor()

    constructor(
        postid: String,
        postImages: List<String>?,
        publisher: String,
        description: String,
        username: String,
        dateTime: Long
    ) {
        this.postid = postid
        this.postImages = postImages
        this.publisher = publisher
        this.description = description
        this.username = username
        this.dateTime = dateTime
    }

    fun getTimeAgo(): String {
        return DateUtils.getRelativeTimeSpanString(
            dateTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}