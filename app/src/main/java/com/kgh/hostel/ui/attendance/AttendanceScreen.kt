package com.kgh.hostel.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private fun today(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    val today = today()

    val rows = combine(
        studentRepository.observeActive(),
        attendanceRepository.observeForDate(today)
    ) { students, marks ->
        val markMap = marks.associateBy { it.studentId }
        students.map { s -> s to markMap[s.id]?.status }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun mark(studentId: Long, status: AttendanceStatus) {
        viewModelScope.launch { attendanceRepository.mark(studentId, today, status) }
    }
}

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = hiltViewModel()) {
    val rows by viewModel.rows.collectAsState()
    val presentCount = rows.count { it.second == AttendanceStatus.PRESENT }
    val absentCount = rows.count { it.second == AttendanceStatus.ABSENT }

    Scaffold(topBar = { TopAppBar(title = { Text("Daily Attendance") }) }) { padding ->
        Column(Modifier.padding(padding).padding(12.dp)) {
            Text("Total: ${rows.size}   Present: $presentCount   Absent: $absentCount", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(rows) { (student: Student, status) ->
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
}
