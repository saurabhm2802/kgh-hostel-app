package com.kgh.hostel.data.local.dao

import androidx.room.*
import com.kgh.hostel.data.local.entity.HostelLeaving
import com.kgh.hostel.data.local.entity.LeaveApplication
import com.kgh.hostel.data.local.entity.LeaveStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Insert
    suspend fun insert(leave: LeaveApplication): Long

    @Update
    suspend fun update(leave: LeaveApplication)

    @Delete
    suspend fun delete(leave: LeaveApplication)

    @Query("SELECT * FROM leave_applications ORDER BY applicationDate DESC")
    fun observeAll(): Flow<List<LeaveApplication>>

    @Query("SELECT * FROM leave_applications WHERE studentId = :studentId ORDER BY applicationDate DESC")
    fun observeForStudent(studentId: Long): Flow<List<LeaveApplication>>

    @Query("SELECT COUNT(*) FROM leave_applications WHERE status = :status AND :today BETWEEN fromDate AND toDate")
    fun observeCountOnDate(status: LeaveStatus, today: Long): Flow<Int>
}

@Dao
interface HostelLeavingDao {
    @Insert
    suspend fun insert(record: HostelLeaving): Long

    @Update
    suspend fun update(record: HostelLeaving)

    @Query("SELECT * FROM hostel_leaving ORDER BY leavingDate DESC")
    fun observeAll(): Flow<List<HostelLeaving>>

    @Query("SELECT * FROM hostel_leaving WHERE studentId = :studentId LIMIT 1")
    suspend fun getForStudent(studentId: Long): HostelLeaving?
}
