package com.neatroots.newdog

import androidx.room.Dao
import androidx.room.Query

@Dao
interface Dao {
    @Query("SELECT * FROM dogdata")
    fun getAll():List<DogData?>
}