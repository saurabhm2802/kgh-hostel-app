package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LeaveStatus { PENDING, APPROVED, ON_LEAVE, RETURNED, CANCELLED, REJECTED }

/**
 * Mirrors the physical "Application for Leave from Hostel" form.
 * instituteName / roomNoAtApplication are auto-filled from the student's
 * current record but stored here too so the historical application always
 * reflects what was true at the time it was filed.
 */
@Entity(tableName = "leave_applications")
data class LeaveApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,

    // --- From the form ---
    val applicationDate: Long,
    val instituteName: String,
    val roomNoAtApplication: String,
    val guardianAddressVisiting: String,   // "Local Guardian/Parent/Friend Address"
    val guardianMobile: String,
    val fromDate: Long,
    val toDate: Long,
    val confirmationCallMade: Boolean,
    val parentRegisteredNumber: String,

    // Office use only
    val officeCallDate: Long? = null,
    val permissionStatus: String = "Pending", // Pending / Granted / Rejected
    val hostelInChargeName: String? = null,

    // App-level workflow status (drives dashboard "Leave Today")
    val status: LeaveStatus = LeaveStatus.PENDING,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
