package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus { PRESENT, ABSENT, LEAVE }

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: Long,               // normalized to midnight epoch millis
    val status: AttendanceStatus,
    val markedAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val editedNote: String? = null
)
