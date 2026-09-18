package com.kgh.hostel.ui.students

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {
    fun observe(studentId: Long) = repository.observeById(studentId)
}

@Composable
fun StudentProfileScreen(
    studentId: Long,
    onLeaveApplication: () -> Unit,
    onHostelLeaving: () -> Unit,
    onChangeRoom: () -> Unit,
    onPayments: () -> Unit,
    onEdit: () -> Unit,
    viewModel: StudentProfileViewModel = hiltViewModel()
) {
    val student by remember(studentId) { viewModel.observe(studentId) }.collectAsState(initial = null)

    Scaffold(topBar = { TopAppBar(title = { Text(student?.name ?: "Student Profile") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            student?.let { s: Student ->
                Text("Admission No: ${s.admissionNumber}")
                Text("Institute: ${s.instituteName}")
                Text("Father's Name: ${s.fatherName}")
                Text("Phone: ${s.phoneStudent}  •  Parent: ${s.parentMobile}")
                Text("Room: ${s.currentRoomId ?: "-"}  •  Bed: ${s.currentBedId ?: "-"}")
                Text("Status: ${s.status}")
                Spacer(Modifier.height(16.dp))

                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Edit Details") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onChangeRoom, modifier = Modifier.fillMaxWidth()) { Text("Change Room / Bed") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onPayments, modifier = Modifier.fillMaxWidth()) { Text("Rent / Payment History") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onLeaveApplication, modifier = Modifier.fillMaxWidth()) { Text("New Leave Application") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onHostelLeaving, modifier = Modifier.fillMaxWidth()) { Text("Hostel Leaving") }
            } ?: Text("Loading...")
        }
    }
}
