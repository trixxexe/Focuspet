package com.example.data.db

import androidx.room.*
import com.example.data.model.FocusPetState
import com.example.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusPetDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    suspend fun getAllSessions(): List<FocusSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)

    @Query("SELECT * FROM focus_pet_state WHERE id = 1 LIMIT 1")
    fun getPetStateFlow(): Flow<FocusPetState?>

    @Query("SELECT * FROM focus_pet_state WHERE id = 1 LIMIT 1")
    suspend fun getPetState(): FocusPetState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetState(state: FocusPetState)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM focus_pet_state")
    suspend fun clearPetState()
}
