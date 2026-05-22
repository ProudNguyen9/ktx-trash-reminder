package com.example.data.local

import androidx.room.*
import com.example.data.model.DormRoom
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import kotlinx.coroutines.flow.Flow

@Dao
interface DormRoomDao {
    @Query("SELECT * FROM dorm_rooms ORDER BY roomName ASC")
    fun getAllRoomsFlow(): Flow<List<DormRoom>>

    @Query("SELECT * FROM dorm_rooms ORDER BY roomName ASC")
    suspend fun getAllRooms(): List<DormRoom>

    @Query("SELECT * FROM dorm_rooms WHERE roomName = :roomName LIMIT 1")
    suspend fun getRoom(roomName: String): DormRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: DormRoom)

    @Delete
    suspend fun deleteRoom(room: DormRoom)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE roomName = :roomName ORDER BY id ASC")
    fun getMembersByRoomFlow(roomName: String): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE roomName = :roomName ORDER BY id ASC")
    suspend fun getMembersByRoom(roomName: String): List<Member>

    @Query("SELECT * FROM members ORDER BY id ASC")
    suspend fun getAllMembers(): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("DELETE FROM members WHERE roomName = :roomName")
    suspend fun deleteAllMembersInRoom(roomName: String)
}

@Dao
interface TrashStateDao {
    @Query("SELECT * FROM trash_state WHERE roomName = :roomName LIMIT 1")
    fun getTrashStateFlow(roomName: String): Flow<TrashState?>

    @Query("SELECT * FROM trash_state WHERE roomName = :roomName LIMIT 1")
    suspend fun getTrashState(roomName: String): TrashState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashState(trashState: TrashState)
}

@Dao
interface HistoryLogDao {
    @Query("SELECT * FROM history_logs WHERE roomName = :roomName ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogsFlow(roomName: String): Flow<List<HistoryLog>>

    @Query("SELECT COUNT(*) FROM history_logs WHERE roomName = :roomName")
    suspend fun getLogsCount(roomName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HistoryLog)

    @Query("DELETE FROM history_logs WHERE roomName = :roomName")
    suspend fun clearAllLogs(roomName: String)
}
