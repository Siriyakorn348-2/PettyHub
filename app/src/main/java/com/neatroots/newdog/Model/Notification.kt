package com.neatroots.newdog.Model

class Notification {
    private var userid: String = ""
    private var text: String = ""
    private var postid: String = ""
    private var ispost: Boolean = false
    private var timestamp: Long = System.currentTimeMillis()

    constructor()

    constructor(userid: String, text: String, postid: String, ispost: Boolean, timestamp: Long = System.currentTimeMillis()) {
        this.userid = userid
        this.text = text
        this.postid = postid
        this.ispost = ispost
        this.timestamp = if (timestamp > 0) timestamp else System.currentTimeMillis()
    }

    fun getUserId(): String = userid
    fun setUserId(userid: String) { this.userid = userid }

    fun getText(): String = text
    fun setText(text: String) { this.text = text }

    fun getPostId(): String = postid
    fun setPostId(postid: String) { this.postid = postid }

    fun isIspost(): Boolean = ispost
    fun setIspost(ispost: Boolean) { this.ispost = ispost }

    fun getTimestamp(): Long = timestamp
    fun setTimestamp(timestamp: Long) {
        this.timestamp = if (timestamp > 0) timestamp else System.currentTimeMillis()
    }

    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "เมื่อสักครู่"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} นาทีที่แล้ว"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ชั่วโมงที่แล้ว"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} วันที่แล้ว"
            diff < 30 * 24 * 60 * 60 * 1000 -> "${diff / (7 * 24 * 60 * 60 * 1000)} สัปดาห์ที่แล้ว"
            diff < 12 * 30 * 24 * 60 * 60 * 1000L -> "${diff / (30 * 24 * 60 * 60 * 1000)} เดือนที่แล้ว"
            else -> "${diff / (12 * 30 * 24 * 60 * 60 * 1000)} ปีที่แล้ว"
        }
    }
}