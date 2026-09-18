package com.kgh.hostel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kgh.hostel.data.local.dao.*
import com.kgh.hostel.data.local.entity.*

@Database(
    entities = [
        Student::class,
        StudentDocument::class,
        HostelRoom::class,
        Bed::class,
        RoomChangeHistory::class,
        Attendance::class,
        LeaveApplication::class,
        HostelLeaving::class,
        Payment::class,
        PaymentAuditLog::class,
        HostelSettings::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KGHDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun roomBedDao(): RoomBedDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun hostelLeavingDao(): HostelLeavingDao
    abstract fun paymentDao(): PaymentDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "kgh_database.db"
    }
}
