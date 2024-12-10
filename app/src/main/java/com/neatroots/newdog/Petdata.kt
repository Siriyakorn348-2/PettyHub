package com.neatroots.newdog

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "petdata")
class Petdata (
    var img:String,
    var title:String,
    var des:String,
    var age:String,
    var category:String,
    var breed:String
){
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var rid: Int=1
}