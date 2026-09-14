package com.example.sayra.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userQuery: String,
    val assistantResponse: String,
    val actionExecuted: String? = null,
    val isSuccess: Boolean = true
)
