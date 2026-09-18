package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_documents",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId")]
)
data class StudentDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val docType: String,       // Aadhaar, PAN, Passport, College ID, Parent ID, Address Proof, Other
    val docName: String,
    val fileUri: String,
    val uploadDate: Long = System.currentTimeMillis()
)
