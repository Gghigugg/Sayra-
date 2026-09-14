package com.example.sayra.data.db

import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun saveConversation(
        userQuery: String,
        assistantResponse: String,
        actionExecuted: String? = null,
        isSuccess: Boolean = true
    ): Long {
        return conversationDao.insertConversation(
            ConversationEntity(
                userQuery = userQuery,
                assistantResponse = assistantResponse,
                actionExecuted = actionExecuted,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun deleteById(id: Long) {
        conversationDao.deleteById(id)
    }

    suspend fun clearHistory() {
        conversationDao.clearAll()
    }
}
