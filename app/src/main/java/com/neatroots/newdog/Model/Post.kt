package com.neatroots.newdog.Model

class Post {
    private var postid : String = ""
    private var postimage : String = ""
    private var publisher : String = ""
    private var description : String = ""
    private var username: String = ""


    private var publisherImage: String = ""
    private var publisherName: String = ""

    constructor()
    constructor(postid: String, postimage: String, publisher: String, description: String, username: String) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.description = description
        this.username = username  // ตั้งค่า username
    }
    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }
    fun getPostid(): String {
        return postid
    }
    fun setPostid(postid: String) {
        this.postid = postid
    }
    fun getPostimage(): String {
        return postimage
    }
    fun setPostimage(postimage: String) {
        this.postimage = postimage
    }
    fun getPublisher(): String {
        return publisher
    }
    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }
    fun getDescription(): String {
        return description
    }
    fun setDescription(description: String) {
        this.description = description
    }
    fun getPublisherImage(): String {
        return publisherImage
    }

    fun setPublisherImage(publisherImage: String) {
        this.publisherImage = publisherImage
    }

    fun getPublisherName(): String {
        return publisherName
    }

    fun setPublisherName(publisherName: String) {
        this.publisherName = publisherName
    }
}