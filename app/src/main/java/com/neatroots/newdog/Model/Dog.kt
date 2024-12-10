package com.neatroots.newdog.Model

class Dog {
    private var dogid : String = ""
    private var dogname : String = ""
    private var dogimage : String = ""
    private var dogbreed : String = ""
    private var dogage : String = ""
    private var doggrender: String =""
    private var userid: String = ""


    constructor()
    constructor(
        dogid: String,
        dogname: String,
        dogimage: String,
        dogbreed: String,
        dogage: String,
        doggrender: String,
        userid: String
    ) {
        this.dogid = dogid
        this.dogname = dogname
        this.dogimage = dogimage
        this.dogbreed = dogbreed
        this.dogage = dogage
        this.doggrender = doggrender
        this.userid = userid
    }
    fun getDogId() : String{
        return dogid
    }
    fun setDogId(dogid: String){
        this.dogid = dogid
    }

    fun getDogName() : String{
        return dogname
    }
    fun setDogName(dogname: String){
        this.dogname = dogname
    }

    fun getDogImage() : String{
        return dogimage
    }
    fun setDogImage(dogimage: String){
        this.dogimage = dogimage
    }

    fun getDogAge() : String{
        return dogage
    }
    fun setDogAge(dogage: String){
        this.dogage = dogage
    }
    fun getDogBreed() : String{
        return dogbreed
    }
    fun setDogBreed(dogbreed: String){
        this.dogbreed = dogbreed
    }
    fun getDogGrender() : String{
        return doggrender
    }
    fun setDogGrender(doggrender: String){
        this.doggrender = doggrender
    }
    fun getUserId(): String {
        return userid
    }

    fun setUserId(userid: String) {
        this.userid = userid
    }



}