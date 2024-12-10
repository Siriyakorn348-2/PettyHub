package com.neatroots.newdog.Model

class Comment {
    private var comment: String = ""
    private var publisher: String = ""
    private var commentId: String = ""

    constructor()

    constructor(comment: String, publisher: String, commentId: String) {
        this.comment = comment
        this.publisher = publisher
        this.commentId = commentId
    }

    fun getComment(): String {
        return comment
    }

    fun setComment(comment: String) {
        this.comment = comment
    }

    fun getPublisher(): String {
        return publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }

    fun getCommentId(): String {
        return commentId
    }

    fun setCommentId(commentId: String) {
        this.commentId = commentId
    }
}
