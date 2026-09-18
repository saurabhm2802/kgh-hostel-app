package com.kgh.hostel.ui.leave

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.LeaveApplication
import com.kgh.hostel.data.local.entity.LeaveStatus
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.LeaveRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaveFormState(
    val student: Student? = null,
    val guardianAddressVisiting: String = "",
    val guardianMobile: String = "",
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val confirmationCallMade: Boolean = false,
    val parentRegisteredNumber: String = "",
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class LeaveFormViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val leaveRepository: LeaveRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LeaveFormState())
    val state: StateFlow<LeaveFormState> = _state

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            studentRepository.observeById(studentId).collect { student ->
                _state.value = _state.value.copy(
                    student = student,
                    parentRegisteredNumber = student?.parentMobile ?: ""
                )
            }
        }
    }

    fun update(transform: (LeaveFormState) -> LeaveFormState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save() {
        val s = _state.value
        val student = s.student ?: return
        if (s.fromDate == null || s.toDate == null || s.guardianAddressVisiting.isBlank()) {
            _state.value = s.copy(error = "Please fill the destination address and leave dates.")
            return
        }
        viewModelScope.launch {
            leaveRepository.saveLeave(
                LeaveApplication(
                    studentId = student.id,
                    applicationDate = System.currentTimeMillis(),
                    instituteName = student.instituteName,
                    roomNoAtApplication = student.currentRoomId?.toString() ?: "",
                    guardianAddressVisiting = s.guardianAddressVisiting,
                    guardianMobile = s.guardianMobile,
                    fromDate = s.fromDate,
                    toDate = s.toDate,
                    confirmationCallMade = s.confirmationCallMade,
                    parentRegisteredNumber = s.parentRegisteredNumber,
                    status = LeaveStatus.PENDING
                )
            )
            _state.value = s.copy(saved = true)
        }
    }
}

@Composable
fun LeaveFormScreen(
    studentId: Long,
    onSaved: () -> Unit,
    viewModel: LeaveFormViewModel = hiltViewModel()
) {
    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(topBar = { TopAppBar(title = { Text("Application for Leave from Hostel") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Khandelwal Girls Hostel", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Student: ${state.student?.name ?: "..."}  •  Room: ${state.student?.currentRoomId ?: "-"}")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(state.guardianAddressVisiting, { v -> viewModel.update { it.copy(guardianAddressVisiting = v) } },
                label = { Text("Local Guardian/Parent/Friend Address to visit *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(state.guardianMobile, { v -> viewModel.update { it.copy(guardianMobile = v) } },
                label = { Text("Mobile No. at that address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("From date and To date: pick via date-picker dialog (wired in the full build).", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(state.parentRegisteredNumber, { v -> viewModel.update { it.copy(parentRegisteredNumber = v) } },
                label = { Text("Parent's Registered Mobile (for confirmation call)") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.confirmationCallMade, onCheckedChange = { v -> viewModel.update { it.copy(confirmationCallMade = v) } })
                Text("Confirmation call has been made to parent")
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Leave Application")
            }
            Spacer(Modifier.height(8.dp))
            Text("The office-use section (call confirmation, Granted/Rejected, hostel in-charge) is completed from the Leave List screen once the administrator decides.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
