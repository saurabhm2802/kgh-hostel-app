package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StudentStatus { ACTIVE, ON_LEAVE, HOSTEL_LEFT }

/**
 * Fields mirror the physical Khandelwal Girls Hostel Admission Form exactly,
 * plus system-generated fields needed for app operation (admissionNumber,
 * photo, current room/bed, status, timestamps).
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Auto-generated (not on the paper form, needed for fast search)
    val admissionNumber: String,

    // --- Fields from the Admission Form ---
    val name: String,
    val dob: Long?,                      // epoch millis, nullable if unknown
    val fatherName: String,
    val permanentAddress: String,
    val phoneStudent: String,
    val parentMobile: String,
    val alternateMobile: String?,
    val localGuardianName: String?,
    val localGuardianAddress: String?,
    val instituteName: String,
    val durationFrom: Long,
    val durationTo: Long,
    val admissionDate: Long,
    val declarationAccepted: Boolean,    // ties to the 10-point undertaking on the form
    val declarationAcceptedBy: String,   // "Parent/Guardian name + Student name" free text

    // --- System fields ---
    val photoUri: String? = null,
    val monthlyRent: Double = 0.0,
    val currentRoomId: Long? = null,
    val currentBedId: Long? = null,
    val status: StudentStatus = StudentStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
