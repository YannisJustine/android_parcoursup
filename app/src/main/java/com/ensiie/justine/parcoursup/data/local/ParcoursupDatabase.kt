package com.ensiie.justine.parcoursup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation

@Database(
    entities = [FavoriteFormation::class],
    version = 1,
    exportSchema = false
)
abstract class ParcoursupDatabase : RoomDatabase() {
    abstract fun favoriteFormationDao(): FavoriteFormationDao
}
