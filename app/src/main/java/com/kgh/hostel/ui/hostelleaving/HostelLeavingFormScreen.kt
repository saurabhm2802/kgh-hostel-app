package com.kgh.hostel.ui.hostelleaving

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.HostelLeaving
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.LeaveRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HostelLeavingState(
    val student: Student? = null,
    val durationStayed: String = "",
    val reasonForLeaving: String = "",
    val leavingDate: Long = System.currentTimeMillis(),
    val confirmationCallMade: Boolean = false,
    val parentRegisteredNumber: String = "",
    val securityDeposit: String = "0",
    val overstayCharges: String = "0",
    val earlyLeavingCharges: String = "0",
    val electricalPenalty: String = "0",
    val otherCharges: String = "0",
    val error: String? = null,
    val saved: Boolean = false
) {
    val totalPayable: Double
        get() = (securityDeposit.toDoubleOrNull() ?: 0.0) -
            (overstayCharges.toDoubleOrNull() ?: 0.0) -
            (earlyLeavingCharges.toDoubleOrNull() ?: 0.0) -
            (electricalPenalty.toDoubleOrNull() ?: 0.0) -
            (otherCharges.toDoubleOrNull() ?: 0.0)
}

@HiltViewModel
class HostelLeavingViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val leaveRepository: LeaveRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HostelLeavingState())
    val state: StateFlow<HostelLeavingState> = _state

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            studentRepository.observeById(studentId).collect { s ->
                _state.value = _state.value.copy(student = s, parentRegisteredNumber = s?.parentMobile ?: "")
            }
        }
    }

    fun update(transform: (HostelLeavingState) -> HostelLeavingState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save() {
        val s = _state.value
        val student = s.student ?: return
        if (s.reasonForLeaving.isBlank()) {
            _state.value = s.copy(error = "Please enter the reason for leaving.")
            return
        }
        viewModelScope.launch {
            leaveRepository.processHostelLeaving(
                HostelLeaving(
                    studentId = student.id,
                    applicationDate = System.currentTimeMillis(),
                    instituteName = student.instituteName,
                    roomNoAtApplication = student.currentRoomId?.toString() ?: "",
                    durationStayed = s.durationStayed,
                    reasonForLeaving = s.reasonForLeaving,
                    leavingDate = s.leavingDate,
                    confirmationCallMade = s.confirmationCallMade,
                    parentRegisteredNumber = s.parentRegisteredNumber,
                    securityDeposit = s.securityDeposit.toDoubleOrNull() ?: 0.0,
                    overstayCharges = s.overstayCharges.toDoubleOrNull() ?: 0.0,
                    earlyLeavingCharges = s.earlyLeavingCharges.toDoubleOrNull() ?: 0.0,
                    electricalPenalty = s.electricalPenalty.toDoubleOrNull() ?: 0.0,
                    otherCharges = s.otherCharges.toDoubleOrNull() ?: 0.0,
                    totalPayable = s.totalPayable
                )
            )
            _state.value = s.copy(saved = true)
        }
    }
}

@Composable
fun HostelLeavingFormScreen(
    studentId: Long,
    onSaved: () -> Unit,
    viewModel: HostelLeavingViewModel = hiltViewModel()
) {
    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(topBar = { TopAppBar(title = { Text("Hostel Leaving Form") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Khandelwal Girls Hostel", style = MaterialTheme.typography.titleMedium)
            Text("Student: ${state.student?.name ?: "..."}")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(state.durationStayed, { v -> viewModel.update { it.copy(durationStayed = v) } },
                label = { Text("Duration stayed at hostel") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(state.reasonForLeaving, { v -> viewModel.update { it.copy(reasonForLeaving = v) } },
                label = { Text("Reason for Leaving Hostel *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(state.parentRegisteredNumber, { v -> viewModel.update { it.copy(parentRegisteredNumber = v) } },
                label = { Text("Parent's Registered Mobile") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.confirmationCallMade, onCheckedChange = { v -> viewModel.update { it.copy(confirmationCallMade = v) } })
                Text("Confirmation call has been made to parent")
            }

            Spacer(Modifier.height(16.dp))
            Text("For Office Use Only — Refund of Security Deposit", style = MaterialTheme.typography.titleSmall)
            RefundRow("Security Deposit of Student", state.securityDeposit) { v -> viewModel.update { it.copy(securityDeposit = v) } }
            RefundRow("Less: Charges for overstay", state.overstayCharges) { v -> viewModel.update { it.copy(overstayCharges = v) } }
            RefundRow("Less: Leaving before term specified", state.earlyLeavingCharges) { v -> viewModel.update { it.copy(earlyLeavingCharges = v) } }
            RefundRow("Less: Penalty for Electrical Appliances", state.electricalPenalty) { v -> viewModel.update { it.copy(electricalPenalty = v) } }
            RefundRow("Less: Any Other Charges", state.otherCharges) { v -> viewModel.update { it.copy(otherCharges = v) } }
            Spacer(Modifier.height(8.dp))
            Text("Total Amount Payable: ₹${"%.2f".format(state.totalPayable)}", style = MaterialTheme.typography.titleMedium)

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(16.dp))
            Text("Saving will mark the student as Hostel Left and free her bed. All history (attendance, leave, payments, documents) is preserved.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Confirm Hostel Leaving")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RefundRow(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(6.dp))
}
