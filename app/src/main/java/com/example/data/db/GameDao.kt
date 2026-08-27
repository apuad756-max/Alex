package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.models.GameStatsEntity
import com.example.data.models.SkinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinDao {
    @Query("SELECT * FROM skins ORDER BY dateCreated DESC")
    fun getAllSkins(): Flow<List<SkinEntity>>

    @Query("SELECT * FROM skins WHERE isEquipped = 1 LIMIT 1")
    fun getEquippedSkinFlow(): Flow<SkinEntity?>

    @Query("SELECT * FROM skins WHERE isEquipped = 1 LIMIT 1")
    suspend fun getEquippedSkin(): SkinEntity?

    @Query("SELECT * FROM skins WHERE id = :id LIMIT 1")
    suspend fun getSkinById(id: String): SkinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkin(skin: SkinEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkins(skins: List<SkinEntity>)

    @Update
    suspend fun updateSkin(skin: SkinEntity)

    @Query("DELETE FROM skins WHERE id = :id")
    suspend fun deleteSkinById(id: String)

    @Query("UPDATE skins SET isEquipped = 0")
    suspend fun clearEquipped()

    @Query("UPDATE skins SET isEquipped = 1 WHERE id = :id")
    suspend fun setEquipped(id: String)

    @Transaction
    suspend fun equipSkin(id: String) {
        clearEquipped()
        setEquipped(id)
    }

    @Query("SELECT COUNT(*) FROM skins")
    suspend fun getSkinCount(): Int
}

@Dao
interface GameStatsDao {
    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    fun getStatsFlow(): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    suspend fun getStats(): GameStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: GameStatsEntity)

    @Update
    suspend fun updateStats(stats: GameStatsEntity)
}
