package com.example.sayra.data.db

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun getAllMemoriesList(): List<MemoryEntity> = memoryDao.getAllMemoriesList()

    suspend fun saveMemory(fact: String, category: String = "general"): Long {
        val memory = MemoryEntity(
            fact = fact.trim(),
            category = category.trim().lowercase(),
            timestamp = System.currentTimeMillis()
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(id: Long, newFact: String, newCategory: String = "general") {
        val updated = MemoryEntity(
            id = id,
            fact = newFact.trim(),
            category = newCategory.trim().lowercase(),
            timestamp = System.currentTimeMillis()
        )
        memoryDao.updateMemory(updated)
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }
}
