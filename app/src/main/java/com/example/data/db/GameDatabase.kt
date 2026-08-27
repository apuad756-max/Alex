package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.models.AccessoryType
import com.example.data.models.BodyShape
import com.example.data.models.ConversationEntity
import com.example.data.models.GameStatsEntity
import com.example.data.models.SkinEntity
import com.example.data.models.TrailType
import com.example.data.models.WeaponFx

class Converters {
    @TypeConverter
    fun fromBodyShape(value: BodyShape) = value.name

    @TypeConverter
    fun toBodyShape(value: String) = try { BodyShape.valueOf(value) } catch (e: Exception) { BodyShape.ORB }

    @TypeConverter
    fun fromTrailType(value: TrailType) = value.name

    @TypeConverter
    fun toTrailType(value: String) = try { TrailType.valueOf(value) } catch (e: Exception) { TrailType.CYBER_SPARKS }

    @TypeConverter
    fun fromAccessoryType(value: AccessoryType) = value.name

    @TypeConverter
    fun toAccessoryType(value: String) = try { AccessoryType.valueOf(value) } catch (e: Exception) { AccessoryType.NONE }

    @TypeConverter
    fun fromWeaponFx(value: WeaponFx) = value.name

    @TypeConverter
    fun toWeaponFx(value: String) = try { WeaponFx.valueOf(value) } catch (e: Exception) { WeaponFx.BLADE_SLASH }
}

@Database(
    entities = [SkinEntity::class, GameStatsEntity::class, ConversationEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun skinDao(): SkinDao
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "alex_diana_game.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
