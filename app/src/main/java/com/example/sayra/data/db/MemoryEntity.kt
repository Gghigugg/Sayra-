package com.example.sayra.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sayra_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fact: String,
    val category: String = "general",
    val timestamp: Long = System.currentTimeMillis()
)
