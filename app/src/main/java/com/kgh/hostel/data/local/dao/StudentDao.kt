package com.kgh.hostel.data.local.dao

import androidx.room.*
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.local.entity.StudentDocument
import com.kgh.hostel.data.local.entity.StudentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Query("SELECT * FROM students WHERE status = :status ORDER BY name ASC")
    fun observeByStatus(status: StudentStatus): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun observeAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun observeById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): Student?

    @Query(
        """SELECT * FROM students WHERE status != 'HOSTEL_LEFT' AND (
             name LIKE '%' || :query || '%' OR
             phoneStudent LIKE '%' || :query || '%' OR
             parentMobile LIKE '%' || :query || '%' OR
             admissionNumber LIKE '%' || :query || '%'
           ) ORDER BY name ASC"""
    )
    fun search(query: String): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE status = 'ON_LEAVE'")
    fun observeOnLeaveCount(): Flow<Int>

    @Query("SELECT MAX(id) FROM students")
    suspend fun getMaxId(): Long?

    // --- Documents ---
    @Insert
    suspend fun insertDocument(document: StudentDocument): Long

    @Delete
    suspend fun deleteDocument(document: StudentDocument)

    @Query("SELECT * FROM student_documents WHERE studentId = :studentId ORDER BY uploadDate DESC")
    fun observeDocuments(studentId: Long): Flow<List<StudentDocument>>
}
