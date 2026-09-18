package com.kgh.hostel.data.repository

import com.kgh.hostel.data.local.dao.AttendanceDao
import com.kgh.hostel.data.local.entity.Attendance
import com.kgh.hostel.data.local.entity.AttendanceStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val dao: AttendanceDao
) {
    fun observeForDate(date: Long) = dao.observeForDate(date)
    fun observeForStudent(studentId: Long) = dao.observeForStudent(studentId)
    fun observePresentCount(date: Long) = dao.observePresentCount(date)
    fun observeAbsentCount(date: Long) = dao.observeAbsentCount(date)

    suspend fun mark(studentId: Long, date: Long, status: AttendanceStatus) {
        val existing = dao.getForStudentAndDate(studentId, date)
        dao.upsert(
            Attendance(
                id = existing?.id ?: 0,
                studentId = studentId,
                date = date,
                status = status,
                editedAt = if (existing != null) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun monthlySummary(studentId: Long, monthStart: Long, monthEnd: Long): Triple<Int, Int, Int> {
        val entries = dao.getForStudentInRange(studentId, monthStart, monthEnd)
        val present = entries.count { it.status == AttendanceStatus.PRESENT }
        val absent = entries.count { it.status == AttendanceStatus.ABSENT }
        val leave = entries.count { it.status == AttendanceStatus.LEAVE }
        return Triple(present, absent, leave)
    }
}
