package com.kgh.hostel.data.repository

import com.kgh.hostel.data.local.dao.RoomBedDao
import com.kgh.hostel.data.local.dao.StudentDao
import com.kgh.hostel.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val roomBedDao: RoomBedDao
) {
    fun observeActive(): Flow<List<Student>> = studentDao.observeByStatus(StudentStatus.ACTIVE)
    fun observeAll(): Flow<List<Student>> = studentDao.observeAll()
    fun observeById(id: Long): Flow<Student?> = studentDao.observeById(id)
    fun search(query: String): Flow<List<Student>> = studentDao.search(query)
    fun observeDocuments(studentId: Long) = studentDao.observeDocuments(studentId)
    fun observeActiveCount() = studentDao.observeActiveCount()
    fun observeOnLeaveCount() = studentDao.observeOnLeaveCount()

    private suspend fun nextAdmissionNumber(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val nextId = (studentDao.getMaxId() ?: 0) + 1
        return "KGH-$year-%04d".format(nextId)
    }

    /**
     * Full admission flow: creates the student record AND assigns the chosen
     * bed atomically, so a student is never saved without a valid bed link
     * (or explicitly with none, if bedId is null e.g. waitlisted).
     */
    suspend fun admitStudent(student: Student, roomId: Long?, bedId: Long?): Long {
        val withNumber = student.copy(
            admissionNumber = nextAdmissionNumber(),
            currentRoomId = roomId,
            currentBedId = bedId
        )
        val newId = studentDao.insert(withNumber)
        if (roomId != null && bedId != null) {
            roomBedDao.assignStudentToBed(
                studentId = newId,
                oldBedId = null,
                oldRoomId = null,
                newBedId = bedId,
                newRoomId = roomId,
                reason = "Initial admission"
            )
        }
        return newId
    }

    suspend fun updateStudent(student: Student) =
        studentDao.update(student.copy(updatedAt = System.currentTimeMillis()))

    suspend fun changeRoom(student: Student, newRoomId: Long, newBedId: Long, reason: String?) {
        roomBedDao.assignStudentToBed(
            studentId = student.id,
            oldBedId = student.currentBedId,
            oldRoomId = student.currentRoomId,
            newBedId = newBedId,
            newRoomId = newRoomId,
            reason = reason
        )
        studentDao.update(student.copy(currentRoomId = newRoomId, currentBedId = newBedId, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addDocument(document: StudentDocument) = studentDao.insertDocument(document)
    suspend fun deleteDocument(document: StudentDocument) = studentDao.deleteDocument(document)
}
