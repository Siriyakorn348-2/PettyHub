package com.neatroots.newdog

import android.icu.text.CaseMap.Title
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogdata")
class DogData(
    var img: String,
    var title: String,
    var des: String,
    var breed: String,
    var age: String,
    var category: String
) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
