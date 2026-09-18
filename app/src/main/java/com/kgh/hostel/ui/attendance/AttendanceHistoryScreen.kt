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
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.AttendanceRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    var summaries by mutableStateOf<Map<Long, Triple<Int, Int, Int>>>(emptyMap())
        private set

    fun loadCurrentMonth(students: List<Student>) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1); cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis

            val result = mutableMapOf<Long, Triple<Int, Int, Int>>()
            students.forEach { s -> result[s.id] = attendanceRepository.monthlySummary(s.id, start, end) }
            summaries = result
        }
    }
}

@Composable
fun AttendanceHistoryScreen(viewModel: AttendanceHistoryViewModel = hiltViewModel()) {
    val students by viewModel.studentRepository.observeActive().collectAsState(initial = emptyList())
    LaunchedEffect(students) { if (students.isNotEmpty()) viewModel.loadCurrentMonth(students) }

    Scaffold(topBar = { TopAppBar(title = { Text("Attendance History — This Month") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(students) { student ->
                val (present, absent, leave) = viewModel.summaries[student.id] ?: Triple(0, 0, 0)
                ListItem(
                    headlineContent = { Text(student.name) },
                    supportingContent = { Text("Present: $present   Absent: $absent   Leave: $leave") }
                )
                HorizontalDivider()
            }
        }
    }
}
