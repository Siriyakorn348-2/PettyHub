package com.neatroots.newdog.Model

import com.google.firebase.database.DataSnapshot


class User {

    private var email: String = ""
    private var username: String = ""
    private var image: String = ""
    private var uid: String = ""
    private var password :String =""

    constructor()

    constructor(username: String, image: String, uid: String , email: String ,password : String)  {
       this.email = email
        this.username = username
        this.image = image
        this.uid = uid
        this.password = password
    }

    fun getEmail(): String {
        return email
    }
    fun setEmail(email: String) {
        this.email = email
    }



    fun getUsername(): String {
        return username
    }
    fun setUsername(username: String){
        this.username = username
    }

    fun getImage(): String {
        return image
    }
    fun setImage(image: String){
        this.image = image
    }

    fun getUID(): String {
        return uid
    }
    fun setUID(uid: String){
        this.uid = uid
    }

    fun getPassword(): String{
        return password
    }
    fun setPassword(password: String){
        this.password = password
    }
    fun isValid(): Boolean {
        return image.isNotEmpty() && uid.isNotEmpty() && email.isNotEmpty() && username.isNotEmpty()
    }

}