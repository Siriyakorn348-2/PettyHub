package com.neatroots.newdog.Model

data class Comment(
    private var comment: String = "",
    private var publisher: String = "",
    private var commentId: String = "",
    private var postId: String = "",
    private var timestamp: Long = 0
) {
    fun getComment(): String = comment
    fun setComment(comment: String) { this.comment = comment }
    fun getPublisher(): String = publisher
    fun setPublisher(publisher: String) { this.publisher = publisher }
    fun getCommentId(): String = commentId
    fun setCommentId(commentId: String) { this.commentId = commentId }
    fun getPostId(): String = postId
    fun setPostId(postId: String) { this.postId = postId }
    fun getTimestamp(): Long = timestamp
    fun setTimestamp(timestamp: Long) { this.timestamp = timestamp }
}