package com.ensiie.justine.parcoursup.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFormationDao {

    @Query("SELECT * FROM favorite_formations ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteFormation>>

    @Query("SELECT * FROM favorite_formations WHERE id = :id")
    suspend fun getFavoriteById(id: String): FavoriteFormation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteFormation)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteFormation)

    @Query("DELETE FROM favorite_formations WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_formations WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>
}
