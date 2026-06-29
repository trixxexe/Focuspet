package com.example.data.db

import com.example.data.model.FocusPetState
import com.example.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

class FocusPetRepository(private val dao: FocusPetDao) {

    val allSessionsFlow: Flow<List<FocusSession>> = dao.getAllSessionsFlow()
    val petStateFlow: Flow<FocusPetState?> = dao.getPetStateFlow()

    suspend fun getAllSessions(): List<FocusSession> = dao.getAllSessions()

    suspend fun getPetState(): FocusPetState? = dao.getPetState()

    suspend fun insertSession(session: FocusSession) {
        dao.insertSession(session)
    }

    suspend fun insertOrUpdatePetState(state: FocusPetState) {
        dao.insertPetState(state)
    }

    suspend fun resetAll() {
        dao.clearAllSessions()
        dao.clearPetState()
        // Re-initialize with default state
        dao.insertPetState(FocusPetState())
    }
}
