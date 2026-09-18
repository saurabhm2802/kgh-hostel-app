package com.kgh.hostel.data.repository

import com.kgh.hostel.data.local.dao.HostelLeavingDao
import com.kgh.hostel.data.local.dao.LeaveDao
import com.kgh.hostel.data.local.dao.RoomBedDao
import com.kgh.hostel.data.local.dao.StudentDao
import com.kgh.hostel.data.local.entity.HostelLeaving
import com.kgh.hostel.data.local.entity.LeaveApplication
import com.kgh.hostel.data.local.entity.LeaveStatus
import com.kgh.hostel.data.local.entity.StudentStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val leaveDao: LeaveDao,
    private val hostelLeavingDao: HostelLeavingDao,
    private val studentDao: StudentDao,
    private val roomBedDao: RoomBedDao
) {
    fun observeAllLeave() = leaveDao.observeAll()
    fun observeLeaveForStudent(studentId: Long) = leaveDao.observeForStudent(studentId)
    fun observeOnLeaveTodayCount(today: Long) = leaveDao.observeCountOnDate(LeaveStatus.ON_LEAVE, today)
    fun observeAllHostelLeaving() = hostelLeavingDao.observeAll()

    suspend fun saveLeave(leave: LeaveApplication): Long =
        if (leave.id == 0L) leaveDao.insert(leave) else { leaveDao.update(leave); leave.id }

    suspend fun deleteLeave(leave: LeaveApplication) = leaveDao.delete(leave)

    /**
     * Permanent hostel-leaving: saves the leaving record, frees the student's
     * bed, and marks the student HOSTEL_LEFT — never deletes the student row,
     * so all attendance/leave/payment/document history stays intact.
     */
    suspend fun processHostelLeaving(record: HostelLeaving) {
        hostelLeavingDao.insert(record)
        val student = studentDao.getById(record.studentId) ?: return
        student.currentBedId?.let { roomBedDao.freeBed(it) }
        studentDao.update(
            student.copy(
                status = StudentStatus.HOSTEL_LEFT,
                currentRoomId = null,
                currentBedId = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
