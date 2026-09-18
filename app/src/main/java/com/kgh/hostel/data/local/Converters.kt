package com.kgh.hostel.data.local

import androidx.room.TypeConverter
import com.kgh.hostel.data.local.entity.*

class Converters {
    @TypeConverter fun fromStudentStatus(v: StudentStatus): String = v.name
    @TypeConverter fun toStudentStatus(v: String): StudentStatus = StudentStatus.valueOf(v)

    @TypeConverter fun fromBedStatus(v: BedStatus): String = v.name
    @TypeConverter fun toBedStatus(v: String): BedStatus = BedStatus.valueOf(v)

    @TypeConverter fun fromAttendanceStatus(v: AttendanceStatus): String = v.name
    @TypeConverter fun toAttendanceStatus(v: String): AttendanceStatus = AttendanceStatus.valueOf(v)

    @TypeConverter fun fromLeaveStatus(v: LeaveStatus): String = v.name
    @TypeConverter fun toLeaveStatus(v: String): LeaveStatus = LeaveStatus.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod?): String? = v?.name
    @TypeConverter fun toPaymentMethod(v: String?): PaymentMethod? = v?.let { PaymentMethod.valueOf(it) }

    @TypeConverter fun fromPaymentStatus(v: PaymentStatus): String = v.name
    @TypeConverter fun toPaymentStatus(v: String): PaymentStatus = PaymentStatus.valueOf(v)
}
