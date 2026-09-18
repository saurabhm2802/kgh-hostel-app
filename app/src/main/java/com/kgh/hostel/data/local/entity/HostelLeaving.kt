package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the physical "Hostel Leaving Form", including the office-use
 * refund table (security deposit less deductions = total payable).
 */
@Entity(tableName = "hostel_leaving")
data class HostelLeaving(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,

    // --- From the form ---
    val applicationDate: Long,
    val instituteName: String,
    val roomNoAtApplication: String,
    val durationStayed: String,
    val reasonForLeaving: String,
    val leavingDate: Long,
    val confirmationCallMade: Boolean,
    val parentRegisteredNumber: String,

    // Office use only
    val officeCallDate: Long? = null,
    val hostelInChargeName: String? = null,

    // Refund table
    val securityDeposit: Double = 0.0,
    val overstayCharges: Double = 0.0,
    val earlyLeavingCharges: Double = 0.0,
    val electricalPenalty: Double = 0.0,
    val otherCharges: Double = 0.0,
    val totalPayable: Double = 0.0,

    val createdAt: Long = System.currentTimeMillis()
)
