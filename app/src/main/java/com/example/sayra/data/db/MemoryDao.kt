package com.example.sayra.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM sayra_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM sayra_memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesList(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("DELETE FROM sayra_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM sayra_memories")
    suspend fun clearAllMemories()
}
