package com.kgh.hostel.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kgh.hostel.ui.admission.AdmissionFormScreen
import com.kgh.hostel.ui.attendance.AttendanceHistoryScreen
import com.kgh.hostel.ui.attendance.AttendanceScreen
import com.kgh.hostel.ui.dashboard.DashboardScreen
import com.kgh.hostel.ui.hostelleaving.HostelLeavingFormScreen
import com.kgh.hostel.ui.leave.LeaveFormScreen
import com.kgh.hostel.ui.leave.LeaveListScreen
import com.kgh.hostel.ui.more.MoreScreen
import com.kgh.hostel.ui.payments.PaymentScreen
import com.kgh.hostel.ui.payments.RentDueScreen
import com.kgh.hostel.ui.reports.ReportDetailScreen
import com.kgh.hostel.ui.reports.ReportsScreen
import com.kgh.hostel.ui.rooms.ChangeRoomScreen
import com.kgh.hostel.ui.rooms.RoomListScreen
import com.kgh.hostel.ui.rooms.VacantBedsScreen
import com.kgh.hostel.ui.security.PinLockScreen
import com.kgh.hostel.ui.settings.AdminProfileScreen
import com.kgh.hostel.ui.settings.BackupRestoreScreen
import com.kgh.hostel.ui.settings.ClearDataScreen
import com.kgh.hostel.ui.students.StudentListScreen
import com.kgh.hostel.ui.students.StudentProfileScreen

/** Routes shown with the bottom navigation bar. Full-screen flows (forms, detail screens) hide it. */
private val routesWithBottomBar = bottomDestinations.map { it.route }.toSet()

@Composable
fun KGHNavHost() {
    val navController = rememberNavController()
    var unlocked by remember { mutableStateOf(false) }

    if (!unlocked) {
        PinLockScreen(onUnlocked = { unlocked = true })
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in routesWithBottomBar

    Scaffold(
        bottomBar = { if (showBottomBar) KGHBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNewAdmission = { navController.navigate(Screen.Admission.route) },
                    onAttendance = { navController.navigate(Screen.Attendance.route) },
                    onStudents = { navController.navigate(Screen.Students.route) },
                    onVacantBeds = { navController.navigate(Screen.VacantBeds.route) },
                    onRentDue = { navController.navigate(Screen.RentDue.route) },
                    onBackup = { navController.navigate(Screen.BackupRestore.route) }
                )
            }

            composable(Screen.Students.route) {
                StudentListScreen(
                    onStudentClick = { id -> navController.navigate(Screen.StudentProfile.createRoute(id)) },
                    onNewAdmission = { navController.navigate(Screen.Admission.route) }
                )
            }

            composable(
                Screen.StudentProfile.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { entry ->
                val studentId = entry.arguments?.getLong("studentId") ?: 0L
                StudentProfileScreen(
                    studentId = studentId,
                    onLeaveApplication = { navController.navigate(Screen.LeaveForm.createRoute(studentId)) },
                    onHostelLeaving = { navController.navigate(Screen.HostelLeavingForm.createRoute(studentId)) },
                    onChangeRoom = { navController.navigate(Screen.ChangeRoom.createRoute(studentId)) },
                    onPayments = { navController.navigate(Screen.Payments.createRoute(studentId)) },
                    onEdit = { /* Edit reuses AdmissionFormScreen pre-filled in the full build */ }
                )
            }

            composable(Screen.Admission.route) {
                AdmissionFormScreen(onSaved = { navController.popBackStack() })
            }

            composable(Screen.Rooms.route) {
                RoomListScreen(onRoomClick = { /* Room detail bed-grid screen */ })
            }

            composable(Screen.VacantBeds.route) { VacantBedsScreen() }

            composable(
                Screen.ChangeRoom.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { entry ->
                val studentId = entry.arguments?.getLong("studentId") ?: 0L
                ChangeRoomScreen(studentId = studentId, onDone = { navController.popBackStack() })
            }

            composable(Screen.Attendance.route) { AttendanceScreen() }
            composable(Screen.AttendanceHistory.route) { AttendanceHistoryScreen() }

            composable(
                Screen.LeaveForm.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { entry ->
                val studentId = entry.arguments?.getLong("studentId") ?: 0L
                LeaveFormScreen(studentId = studentId, onSaved = { navController.popBackStack() })
            }

            composable(Screen.LeaveList.route) { LeaveListScreen() }

            composable(
                Screen.HostelLeavingForm.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { entry ->
                val studentId = entry.arguments?.getLong("studentId") ?: 0L
                HostelLeavingFormScreen(studentId = studentId, onSaved = {
                    navController.popBackStack(Screen.Students.route, inclusive = false)
                })
            }

            composable(Screen.RentDue.route) { RentDueScreen() }

            composable(
                Screen.Payments.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { entry ->
                val studentId = entry.arguments?.getLong("studentId") ?: 0L
                PaymentScreen(studentId = studentId)
            }

            composable(Screen.Reports.route) {
                ReportsScreen(onReportClick = { slug -> navController.navigate(Screen.ReportDetail.createRoute(slug)) })
            }

            composable(
                Screen.ReportDetail.route,
                arguments = listOf(navArgument("reportType") { type = NavType.StringType })
            ) { entry ->
                val reportType = entry.arguments?.getString("reportType") ?: ""
                ReportDetailScreen(reportType = reportType)
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onLeaveList = { navController.navigate(Screen.LeaveList.route) },
                    onRentDue = { navController.navigate(Screen.RentDue.route) },
                    onReports = { navController.navigate(Screen.Reports.route) },
                    onBackupRestore = { navController.navigate(Screen.BackupRestore.route) },
                    onClearData = { navController.navigate(Screen.ClearData.route) },
                    onAdminProfile = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.BackupRestore.route) { BackupRestoreScreen() }
            composable(Screen.Settings.route) { AdminProfileScreen() }
            composable(Screen.ClearData.route) { com.kgh.hostel.ui.settings.ClearDataScreen() }
        }
    }
}
