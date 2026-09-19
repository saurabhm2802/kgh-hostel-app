package com.kgh.hostel.ui.navigation

sealed class Screen(val route: String) {
    data object PinLock : Screen("pin_lock")
    data object Dashboard : Screen("dashboard")
    data object Students : Screen("students")
    data object StudentProfile : Screen("student_profile/{studentId}") {
        fun createRoute(studentId: Long) = "student_profile/$studentId"
    }
    data object Admission : Screen("admission")
    data object Rooms : Screen("rooms")
    data object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
    data object ChangeRoom : Screen("change_room/{studentId}") {
        fun createRoute(studentId: Long) = "change_room/$studentId"
    }
    data object VacantBeds : Screen("vacant_beds")
    data object Attendance : Screen("attendance")
    data object AttendanceHistory : Screen("attendance_history")
    data object More : Screen("more")
    data object LeaveForm : Screen("leave_form/{studentId}") {
        fun createRoute(studentId: Long) = "leave_form/$studentId"
    }
    data object LeaveList : Screen("leave_list")
    data object HostelLeavingForm : Screen("hostel_leaving_form/{studentId}") {
        fun createRoute(studentId: Long) = "hostel_leaving_form/$studentId"
    }
    data object RentDue : Screen("rent_due")
    data object Payments : Screen("payments/{studentId}") {
        fun createRoute(studentId: Long) = "payments/$studentId"
    }
    data object Reports : Screen("reports")
    data object ReportDetail : Screen("report_detail/{reportType}") {
        fun createRoute(reportType: String) = "report_detail/$reportType"
    }
    data object Settings : Screen("settings")
    data object BackupRestore : Screen("backup_restore")
    data object ClearData : Screen("clear_data")
}
