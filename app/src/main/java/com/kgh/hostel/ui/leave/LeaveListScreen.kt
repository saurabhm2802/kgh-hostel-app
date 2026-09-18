package com.kgh.hostel.ui.leave

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
import com.kgh.hostel.data.local.entity.LeaveApplication
import com.kgh.hostel.data.local.entity.LeaveStatus
import com.kgh.hostel.data.repository.LeaveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveListViewModel @Inject constructor(
    val repository: LeaveRepository
) : ViewModel() {
    fun approve(leave: LeaveApplication, inChargeName: String) {
        viewModelScope.launch {
            repository.saveLeave(
                leave.copy(
                    permissionStatus = "Granted",
                    status = LeaveStatus.ON_LEAVE,
                    officeCallDate = System.currentTimeMillis(),
                    hostelInChargeName = inChargeName
                )
            )
        }
    }

    fun reject(leave: LeaveApplication, inChargeName: String) {
        viewModelScope.launch {
            repository.saveLeave(
                leave.copy(
                    permissionStatus = "Rejected",
                    status = LeaveStatus.REJECTED,
                    officeCallDate = System.currentTimeMillis(),
                    hostelInChargeName = inChargeName
                )
            )
        }
    }

    fun markReturned(leave: LeaveApplication) {
        viewModelScope.launch { repository.saveLeave(leave.copy(status = LeaveStatus.RETURNED)) }
    }
}

@Composable
fun LeaveListScreen(viewModel: LeaveListViewModel = hiltViewModel()) {
    val leaves by viewModel.repository.observeAllLeave().collectAsState(initial = emptyList())
    var actioningLeave by remember { mutableStateOf<LeaveApplication?>(null) }
    var inChargeName by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Leave Applications") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(leaves) { leave ->
                ListItem(
                    headlineContent = { Text("Student #${leave.studentId} — ${leave.status}") },
                    supportingContent = { Text("Room ${leave.roomNoAtApplication} • To: ${leave.guardianAddressVisiting}") },
                    trailingContent = {
                        Row {
                            if (leave.status == LeaveStatus.PENDING) {
                                TextButton(onClick = { actioningLeave = leave }) { Text("Review") }
                            } else if (leave.status == LeaveStatus.ON_LEAVE) {
                                TextButton(onClick = { viewModel.markReturned(leave) }) { Text("Mark Returned") }
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    actioningLeave?.let { leave ->
        AlertDialog(
            onDismissRequest = { actioningLeave = null },
            title = { Text("Office Use — Leave Decision") },
            text = {
                Column {
                    Text("Call has been made to the parent's registered number: ${leave.parentRegisteredNumber}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(inChargeName, { inChargeName = it }, label = { Text("Hostel In-Charge Name") })
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.approve(leave, inChargeName); actioningLeave = null }) { Text("Grant Permission") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.reject(leave, inChargeName); actioningLeave = null }) { Text("Reject") }
            }
        )
    }
}
