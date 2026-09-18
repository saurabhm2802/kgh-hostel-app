package com.kgh.hostel.data.local.dao

import androidx.room.*
import com.kgh.hostel.data.local.entity.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun observeForDate(date: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun observeForStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getForStudentAndDate(studentId: Long, date: Long): Attendance?

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'PRESENT'")
    fun observePresentCount(date: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'ABSENT'")
    fun observeAbsentCount(date: Long): Flow<Int>

    @Query(
        """SELECT * FROM attendance WHERE studentId = :studentId
           AND date BETWEEN :monthStart AND :monthEnd"""
    )
    suspend fun getForStudentInRange(studentId: Long, monthStart: Long, monthEnd: Long): List<Attendance>
}
