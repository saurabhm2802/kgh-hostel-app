package com.kgh.hostel.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.AttendanceStatus
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.AttendanceRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private fun todayMidnight(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun normalizeToMidnight(millis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun formatDate(millis: Long): String =
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val selectedDate = MutableStateFlow(todayMidnight())

    @OptIn(ExperimentalCoroutinesApi::class)
    val rows: StateFlow<List<Pair<Student, AttendanceStatus?>>> = selectedDate
        .flatMapLatest { date ->
            combine(
                studentRepository.observeActive(),
                attendanceRepository.observeForDate(date)
            ) { students, marks ->
                val markMap = marks.associateBy { it.studentId }
                students.map { s -> s to markMap[s.id]?.status }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(millis: Long) {
        selectedDate.value = normalizeToMidnight(millis)
    }

    fun mark(studentId: Long, status: AttendanceStatus) {
        viewModelScope.launch { attendanceRepository.mark(studentId, selectedDate.value, status) }
    }
}

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = hiltViewModel()) {
    val rows by viewModel.rows.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showListFor by remember { mutableStateOf<AttendanceStatus?>(null) }

    val presentStudents = rows.filter { it.second == AttendanceStatus.PRESENT }.map { it.first }
    val absentStudents = rows.filter { it.second == AttendanceStatus.ABSENT }.map { it.first }

    Scaffold(topBar = { TopAppBar(title = { Text("Attendance") }) }) { padding ->
        Column(Modifier.padding(padding).padding(12.dp)) {

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: ${formatDate(selectedDate)} (tap to change)")
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendanceSummaryCard(
                    label = "Total Students",
                    value = rows.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                AttendanceSummaryCard(
                    label = "Present",
                    value = presentStudents.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { showListFor = AttendanceStatus.PRESENT }
                )
                AttendanceSummaryCard(
                    label = "Absent",
                    value = absentStudents.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { showListFor = AttendanceStatus.ABSENT }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Mark attendance for this date", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))

            LazyColumn {
                items(rows) { (student, status) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(student.name, modifier = Modifier.weight(1f))
                        FilterChip(
                            selected = status == AttendanceStatus.PRESENT,
                            onClick = { viewModel.mark(student.id, AttendanceStatus.PRESENT) },
                            label = { Text("Present") }
                        )
                        Spacer(Modifier.width(6.dp))
                        FilterChip(
                            selected = status == AttendanceStatus.ABSENT,
                            onClick = { viewModel.mark(student.id, AttendanceStatus.ABSENT) },
                            label = { Text("Absent") }
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.selectDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    showListFor?.let { status ->
        val list = if (status == AttendanceStatus.PRESENT) presentStudents else absentStudents
        AlertDialog(
            onDismissRequest = { showListFor = null },
            title = { Text(if (status == AttendanceStatus.PRESENT) "Present Students" else "Absent Students") },
            text = {
                if (list.isEmpty()) {
                    Text("No students in this list for the selected date.")
                } else {
                    Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                        list.forEach { s ->
                            Text("${s.name} — Room ${s.currentRoomId ?: "-"} — ${s.phoneStudent}")
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showListFor = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun AttendanceSummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
