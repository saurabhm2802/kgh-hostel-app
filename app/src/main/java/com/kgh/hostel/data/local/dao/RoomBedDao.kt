package com.kgh.hostel.data.local.dao

import androidx.room.*
import com.kgh.hostel.data.local.entity.Bed
import com.kgh.hostel.data.local.entity.BedStatus
import com.kgh.hostel.data.local.entity.HostelRoom
import com.kgh.hostel.data.local.entity.RoomChangeHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomBedDao {

    @Insert
    suspend fun insertRoom(room: HostelRoom): Long

    @Update
    suspend fun updateRoom(room: HostelRoom)

    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun observeRooms(): Flow<List<HostelRoom>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoom(id: Long): HostelRoom?

    @Insert
    suspend fun insertBed(bed: Bed): Long

    @Update
    suspend fun updateBed(bed: Bed)

    @Query("SELECT * FROM beds WHERE roomId = :roomId ORDER BY bedNumber ASC")
    fun observeBedsForRoom(roomId: Long): Flow<List<Bed>>

    @Query("SELECT * FROM beds WHERE status = 'VACANT' ORDER BY roomId ASC")
    fun observeVacantBeds(): Flow<List<Bed>>

    @Query("SELECT COUNT(*) FROM beds")
    fun observeTotalBeds(): Flow<Int>

    @Query("SELECT COUNT(*) FROM beds WHERE status = 'OCCUPIED'")
    fun observeOccupiedBeds(): Flow<Int>

    @Query("SELECT COUNT(*) FROM beds WHERE status = 'VACANT'")
    fun observeVacantBedCount(): Flow<Int>

    @Query("SELECT * FROM beds WHERE id = :id")
    suspend fun getBed(id: Long): Bed?

    @Insert
    suspend fun insertRoomChange(history: RoomChangeHistory)

    @Query("SELECT * FROM room_change_history WHERE studentId = :studentId ORDER BY changeDate DESC")
    fun observeHistoryForStudent(studentId: Long): Flow<List<RoomChangeHistory>>

    /**
     * Atomically moves a student into a new bed: frees the old bed (if any),
     * occupies the new bed, and records history. Runs in a single transaction
     * so a bed can never end up double-assigned.
     */
    @Transaction
    suspend fun assignStudentToBed(
        studentId: Long,
        oldBedId: Long?,
        oldRoomId: Long?,
        newBedId: Long,
        newRoomId: Long,
        reason: String?
    ) {
        oldBedId?.let { id ->
            getBed(id)?.let { updateBed(it.copy(status = BedStatus.VACANT, currentStudentId = null)) }
        }
        val newBed = getBed(newBedId)
        require(newBed != null && newBed.status == BedStatus.VACANT) { "Selected bed is not vacant" }
        updateBed(newBed.copy(status = BedStatus.OCCUPIED, currentStudentId = studentId))
        insertRoomChange(
            RoomChangeHistory(
                studentId = studentId,
                oldRoomId = oldRoomId,
                oldBedId = oldBedId,
                newRoomId = newRoomId,
                newBedId = newBedId,
                reason = reason
            )
        )
    }

    @Transaction
    suspend fun freeBed(bedId: Long) {
        getBed(bedId)?.let { updateBed(it.copy(status = BedStatus.VACANT, currentStudentId = null)) }
    }
}
