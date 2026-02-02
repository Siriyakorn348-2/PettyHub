package com.neatroots.newdog.Model

data class Dog(
    private var dogId: String = "",
    private var dogName: String = "",
    private var dogImage: String = "",
    private var dogBreed: String = "",
    private var dogAge: String = "",
    private var dogGender: String = "",
    private var userId: String = ""
) {
    constructor() : this("", "", "", "", "", "", "")

    fun getDogId() = dogId
    fun getDogName() = dogName
    fun getDogImage() = dogImage
    fun getDogBreed() = dogBreed
    fun getDogAge() = dogAge
    fun getDogGender() = dogGender
    fun getUserId() = userId

    fun setDogId(value: String) { dogId = value }
    fun setDogName(value: String) { dogName = value }
    fun setDogImage(value: String) { dogImage = value }
    fun setDogBreed(value: String) { dogBreed = value }
    fun setDogAge(value: String) { dogAge = value }
    fun setDogGrender(value: String) { dogGender = value }
    fun setUserId(value: String) { userId = value }
}