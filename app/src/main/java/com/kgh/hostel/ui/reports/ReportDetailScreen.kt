package com.kgh.hostel.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.kgh.hostel.data.local.entity.*
import com.kgh.hostel.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

private fun todayMidnight(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun formatDate(millis: Long?): String =
    if (millis == null) "-" else java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))

val REPORT_TITLES = mapOf(
    "student" to "Student Report",
    "room_occupancy" to "Room Occupancy Report",
    "vacant_bed" to "Vacant Bed Report",
    "attendance" to "Attendance Report (Today)",
    "absent" to "Absent Student Report (Today)",
    "leave" to "Leave Report",
    "rent_due" to "Rent Due Report",
    "payment" to "Payment Report",
    "hostel_leaving" to "Hostel Leaving Report"
)

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    studentRepository: StudentRepository,
    roomRepository: RoomRepository,
    attendanceRepository: AttendanceRepository,
    leaveRepository: LeaveRepository,
    paymentRepository: PaymentRepository
) : ViewModel() {
    val allStudents = studentRepository.observeAll()
    val activeStudents = studentRepository.observeActive()
    val rooms = roomRepository.observeRooms()
    val allBeds = roomRepository.observeAllBeds()
    val vacantBeds = roomRepository.observeVacantBeds()
    val todayAttendance = attendanceRepository.observeForDate(todayMidnight())
    val allLeave = leaveRepository.observeAllLeave()
    val allHostelLeaving = leaveRepository.observeAllHostelLeaving()
    val rentDue = paymentRepository.observeDue()
    val allPayments = paymentRepository.observeAll()
}

@Composable
fun ReportDetailScreen(reportType: String, viewModel: ReportDetailViewModel = hiltViewModel()) {
    val title = REPORT_TITLES[reportType] ?: "Report"

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Box(Modifier.padding(padding).padding(12.dp).fillMaxSize()) {
            when (reportType) {
                "student" -> StudentReport(viewModel)
                "room_occupancy" -> RoomOccupancyReport(viewModel)
                "vacant_bed" -> VacantBedReport(viewModel)
                "attendance" -> AttendanceReport(viewModel)
                "absent" -> AbsentReport(viewModel)
                "leave" -> LeaveReport(viewModel)
                "rent_due" -> RentDueReport(viewModel)
                "payment" -> PaymentReport(viewModel)
                "hostel_leaving" -> HostelLeavingReport(viewModel)
                else -> Text("Unknown report.")
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String = "No records yet.") {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun StudentReport(viewModel: ReportDetailViewModel) {
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    if (students.isEmpty()) { EmptyNote(); return }
    LazyColumn {
        items(students) { s ->
            ListItem(
                headlineContent = { Text(s.name) },
                supportingContent = { Text("${s.admissionNumber} • Room ${s.currentRoomId ?: "-"} • ${s.status} • ${s.phoneStudent}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun RoomOccupancyReport(viewModel: ReportDetailViewModel) {
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())
    val beds by viewModel.allBeds.collectAsState(initial = emptyList())
    if (rooms.isEmpty()) { EmptyNote(); return }
    LazyColumn {
        items(rooms) { room ->
            val roomBeds = beds.filter { it.roomId == room.id }
            val occupied = roomBeds.count { it.status == BedStatus.OCCUPIED }
            ListItem(
                headlineContent = { Text("Room ${room.roomNumber}") },
                supportingContent = { Text("Floor ${room.floor} • $occupied / ${roomBeds.size} beds occupied") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun VacantBedReport(viewModel: ReportDetailViewModel) {
    val beds by viewModel.vacantBeds.collectAsState(initial = emptyList())
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())
    if (beds.isEmpty()) { EmptyNote("No vacant beds."); return }
    val roomMap = rooms.associateBy { it.id }
    LazyColumn {
        items(beds) { bed ->
            val room = roomMap[bed.roomId]
            ListItem(
                headlineContent = { Text("Room ${room?.roomNumber ?: bed.roomId} — Bed ${bed.bedNumber}") },
                supportingContent = { Text("Floor ${room?.floor ?: "-"} • Rent ₹${room?.monthlyRent ?: 0.0}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun AttendanceReport(viewModel: ReportDetailViewModel) {
    val attendance by viewModel.todayAttendance.collectAsState(initial = emptyList())
    val students by viewModel.activeStudents.collectAsState(initial = emptyList())
    if (students.isEmpty()) { EmptyNote(); return }
    val markMap = attendance.associateBy { it.studentId }
    LazyColumn {
        items(students) { s ->
            val status = markMap[s.id]?.status?.name ?: "Not marked"
            ListItem(
                headlineContent = { Text(s.name) },
                supportingContent = { Text("Room ${s.currentRoomId ?: "-"} • $status") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun AbsentReport(viewModel: ReportDetailViewModel) {
    val attendance by viewModel.todayAttendance.collectAsState(initial = emptyList())
    val students by viewModel.activeStudents.collectAsState(initial = emptyList())
    val absentIds = attendance.filter { it.status == AttendanceStatus.ABSENT }.map { it.studentId }.toSet()
    val absentStudents = students.filter { it.id in absentIds }
    if (absentStudents.isEmpty()) { EmptyNote("No absent students today."); return }
    LazyColumn {
        items(absentStudents) { s ->
            ListItem(
                headlineContent = { Text(s.name) },
                supportingContent = { Text("Room ${s.currentRoomId ?: "-"} • ${s.phoneStudent} • Parent: ${s.parentMobile}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun LeaveReport(viewModel: ReportDetailViewModel) {
    val leaves by viewModel.allLeave.collectAsState(initial = emptyList())
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    if (leaves.isEmpty()) { EmptyNote(); return }
    val nameMap = students.associateBy { it.id }
    LazyColumn {
        items(leaves) { leave ->
            ListItem(
                headlineContent = { Text(nameMap[leave.studentId]?.name ?: "Student #${leave.studentId}") },
                supportingContent = { Text("${formatDate(leave.fromDate)} to ${formatDate(leave.toDate)} • ${leave.status}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun RentDueReport(viewModel: ReportDetailViewModel) {
    val due by viewModel.rentDue.collectAsState(initial = emptyList())
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    if (due.isEmpty()) { EmptyNote("No pending rent."); return }
    val nameMap = students.associateBy { it.id }
    LazyColumn {
        items(due) { payment ->
            ListItem(
                headlineContent = { Text(nameMap[payment.studentId]?.name ?: "Student #${payment.studentId}") },
                supportingContent = { Text("${payment.month} • Due ₹${payment.amountDue} • Paid ₹${payment.amountPaid} • Balance ₹${payment.balance}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun PaymentReport(viewModel: ReportDetailViewModel) {
    val payments by viewModel.allPayments.collectAsState(initial = emptyList())
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    if (payments.isEmpty()) { EmptyNote(); return }
    val nameMap = students.associateBy { it.id }
    LazyColumn {
        items(payments) { payment ->
            ListItem(
                headlineContent = { Text(nameMap[payment.studentId]?.name ?: "Student #${payment.studentId}") },
                supportingContent = { Text("${payment.month} • ${payment.status} • Paid ₹${payment.amountPaid} of ₹${payment.amountDue}") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun HostelLeavingReport(viewModel: ReportDetailViewModel) {
    val records by viewModel.allHostelLeaving.collectAsState(initial = emptyList())
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    if (records.isEmpty()) { EmptyNote(); return }
    val nameMap = students.associateBy { it.id }
    LazyColumn {
        items(records) { record ->
            ListItem(
                headlineContent = { Text(nameMap[record.studentId]?.name ?: "Student #${record.studentId}") },
                supportingContent = { Text("Left on ${formatDate(record.leavingDate)} • Reason: ${record.reasonForLeaving}") }
            )
            HorizontalDivider()
        }
    }
}
