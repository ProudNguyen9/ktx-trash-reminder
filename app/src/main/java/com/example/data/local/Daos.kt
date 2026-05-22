package com.example.data.local

import androidx.room.*
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY id ASC")
    fun getAllMembersFlow(): Flow<List<Member>>

    @Query("SELECT * FROM members ORDER BY id ASC")
    suspend fun getAllMembers(): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)
}

@Dao
interface TrashStateDao {
    @Query("SELECT * FROM trash_state WHERE id = 1")
    fun getTrashStateFlow(): Flow<TrashState?>

    @Query("SELECT * FROM trash_state WHERE id = 1")
    suspend fun getTrashState(): TrashState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashState(trashState: TrashState)
}

@Dao
interface HistoryLogDao {
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogsFlow(): Flow<List<HistoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HistoryLog)

    @Query("DELETE FROM history_logs")
    suspend fun clearAllLogs()
}
