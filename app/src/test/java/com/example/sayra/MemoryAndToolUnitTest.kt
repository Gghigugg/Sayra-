package com.example.sayra

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.sayra.data.db.MemoryEntity
import com.example.sayra.data.db.SayraDatabase
import com.example.sayra.data.storage.PreferencesManager
import com.example.sayra.engine.AndroidActionController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MemoryAndToolUnitTest {

    private lateinit var context: Context
    private lateinit var database: SayraDatabase
    private lateinit var prefsManager: PreferencesManager
    private lateinit var actionController: AndroidActionController

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SayraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefsManager = PreferencesManager(context)
        actionController = AndroidActionController(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testMemoryInsertionAndRetrieval() = runBlocking {
        val memoryDao = database.memoryDao()
        val memory = MemoryEntity(
            fact = "User prefers Hinglish responses",
            category = "preference"
        )
        val id = memoryDao.insertMemory(memory)
        assertTrue(id > 0)

        val allMemories = memoryDao.getAllMemories().first()
        assertEquals(1, allMemories.size)
        assertEquals("User prefers Hinglish responses", allMemories[0].fact)
        assertEquals("preference", allMemories[0].category)

        // Update memory
        val updatedMemory = allMemories[0].copy(fact = "User prefers English responses")
        memoryDao.updateMemory(updatedMemory)
        val updatedList = memoryDao.getAllMemoriesList()
        assertEquals(1, updatedList.size)
        assertEquals("User prefers English responses", updatedList[0].fact)

        // Delete memory
        memoryDao.deleteMemory(allMemories[0].id)
        val afterDelete = memoryDao.getAllMemories().first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testPrivacyModeToggle() {
        // By default Privacy Mode is false
        prefsManager.setPrivacyModeEnabled(false)
        assertFalse(prefsManager.isPrivacyModeEnabled())

        // Toggle on
        prefsManager.setPrivacyModeEnabled(true)
        assertTrue(prefsManager.isPrivacyModeEnabled())

        // Toggle back off
        prefsManager.setPrivacyModeEnabled(false)
        assertFalse(prefsManager.isPrivacyModeEnabled())
    }

    @Test
    fun testActionControllerOpenUrl() {
        val result = actionController.openUrl("https://ai.google.dev")
        assertTrue(result.isSuccess)
        assertTrue(result.details.contains("Opening link") || result.details.contains("ai.google.dev"))
    }

    @Test
    fun testActionControllerControlBrightness() {
        val result = actionController.controlBrightness(75)
        assertNotNull(result.details)
    }
}
