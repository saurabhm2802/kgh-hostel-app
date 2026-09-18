package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class HostelRoom(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: String,
    val floor: String,
    val roomType: String,
    val totalBeds: Int,
    val monthlyRent: Double,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BedStatus { VACANT, OCCUPIED, UNAVAILABLE }

@Entity(
    tableName = "beds",
    foreignKeys = [
        ForeignKey(
            entity = HostelRoom::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roomId", "bedNumber"], unique = true)]
)
data class Bed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val bedNumber: String,
    val status: BedStatus = BedStatus.VACANT,
    val currentStudentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "room_change_history")
data class RoomChangeHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val oldRoomId: Long?,
    val oldBedId: Long?,
    val newRoomId: Long,
    val newBedId: Long,
    val changeDate: Long = System.currentTimeMillis(),
    val reason: String? = null
)
