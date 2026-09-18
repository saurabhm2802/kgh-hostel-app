package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod { CASH, UPI, BANK_TRANSFER, OTHER }
enum class PaymentStatus { PAID, PARTIALLY_PAID, DUE, OVERDUE }

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val month: String,              // e.g. "2026-09"
    val amountDue: Double,
    val amountPaid: Double,
    val paymentDate: Long?,
    val method: PaymentMethod?,
    val transactionRef: String? = null,
    val notes: String? = null,
    val status: PaymentStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balance: Double get() = amountDue - amountPaid
}

@Entity(tableName = "payment_audit_log")
data class PaymentAuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentId: Long,
    val oldAmountPaid: Double,
    val newAmountPaid: Double,
    val changedAt: Long = System.currentTimeMillis()
)
