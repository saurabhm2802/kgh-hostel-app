package com.kgh.hostel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hostel_settings")
data class HostelSettings(
    @PrimaryKey val id: Int = 0, // single row
    val hostelName: String = "My Khandelwal Girls Hostel",
    val address: String = "16, 'Ganesh Heights' Shardashram Colony, Paithan Gate to Nirala Bazar Road, Aurangabad",
    val contactNumber: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val wardenName: String = "",
    val logoUri: String? = null
)

@Entity(tableName = "audit_log")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
