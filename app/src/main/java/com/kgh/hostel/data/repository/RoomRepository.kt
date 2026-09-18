package com.kgh.hostel.data.repository

import com.kgh.hostel.data.local.dao.RoomBedDao
import com.kgh.hostel.data.local.entity.Bed
import com.kgh.hostel.data.local.entity.BedStatus
import com.kgh.hostel.data.local.entity.HostelRoom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val dao: RoomBedDao
) {
    fun observeRooms() = dao.observeRooms()
    fun observeBedsForRoom(roomId: Long) = dao.observeBedsForRoom(roomId)
    fun observeVacantBeds() = dao.observeVacantBeds()
    fun observeAllBeds() = dao.observeAllBeds()
    fun observeTotalBeds() = dao.observeTotalBeds()
    fun observeOccupiedBeds() = dao.observeOccupiedBeds()
    fun observeVacantBedCount() = dao.observeVacantBedCount()
    fun observeHistoryForStudent(studentId: Long) = dao.observeHistoryForStudent(studentId)

    suspend fun addRoomWithBeds(room: HostelRoom): Long {
        val roomId = dao.insertRoom(room)
        for (i in 1..room.totalBeds) {
            dao.insertBed(Bed(roomId = roomId, bedNumber = i.toString(), status = BedStatus.VACANT))
        }
        return roomId
    }

    suspend fun updateRoom(room: HostelRoom) = dao.updateRoom(room)
    suspend fun updateBed(bed: Bed) = dao.updateBed(bed)
    suspend fun freeBed(bedId: Long) = dao.freeBed(bedId)
}
