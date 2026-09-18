package com.kgh.hostel.ui.admission

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
import com.kgh.hostel.data.local.entity.BedStatus

private const val DECLARATION_TEXT = """
1. I declare that I have read all the Rules & Regulations of the Hostel and I shall abide by them and make payment as prescribed by the hostel administration.
2. I further declare that, on admission to the hostel, I shall submit myself to the disciplinary jurisdiction of the in-charge and other authorities.
3. The seat in the hostel reserved by me is for the entire duration selected above. I give my acceptance for the same.
4. I shall not claim any refund of hostel fee paid & caution money deposited with the hostel, if I leave before completion of the duration due to any reason whatsoever.
5. I indemnify the hostel authorities against any casualties or risk to me.
6. I agree that the Management's decision in all matters concerning accommodation, discipline and conduct will be final and binding on me.
7. I accept that if deemed fit as per rules the hostel authorities have the right to ask me to vacate the accommodation without assigning any reason.
8. Before leaving the hostel I shall clear my dues in full and handover to the in-charge the furniture and other items intact.
9. I shall not associate myself in any activity considered undesirable by the authorities.
10. I have read the rules and undertake to abide by them.
"""

@Composable
fun AdmissionFormScreen(
    onSaved: () -> Unit,
    viewModel: AdmissionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val rooms by viewModel.roomRepository.observeRooms().collectAsState(initial = emptyList())
    var bedsForRoom by remember { mutableStateOf<List<com.kgh.hostel.data.local.entity.Bed>>(emptyList()) }

    LaunchedEffect(state.selectedRoomId) {
        state.selectedRoomId?.let { roomId ->
            viewModel.roomRepository.observeBedsForRoom(roomId).collect { bedsForRoom = it }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("New Admission") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Khandelwal Girls Hostel — Admission Form", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(state.name, { v -> viewModel.update { it.copy(name = v) } },
                label = { Text("Name of applicant *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.fatherName, { v -> viewModel.update { it.copy(fatherName = v) } },
                label = { Text("Father's Name *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.permanentAddress, { v -> viewModel.update { it.copy(permanentAddress = v) } },
                label = { Text("Permanent Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.phoneStudent, { v -> viewModel.update { it.copy(phoneStudent = v) } },
                label = { Text("Phone No (Student) *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.parentMobile, { v -> viewModel.update { it.copy(parentMobile = v) } },
                label = { Text("Parent Mobile *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.alternateMobile, { v -> viewModel.update { it.copy(alternateMobile = v) } },
                label = { Text("Alternate Mobile") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.instituteName, { v -> viewModel.update { it.copy(instituteName = v) } },
                label = { Text("Name of Institute *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.localGuardianName, { v -> viewModel.update { it.copy(localGuardianName = v) } },
                label = { Text("Local Guardian's Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.localGuardianAddress, { v -> viewModel.update { it.copy(localGuardianAddress = v) } },
                label = { Text("Local Guardian's Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(state.monthlyRent, { v -> viewModel.update { it.copy(monthlyRent = v) } },
                label = { Text("Monthly Rent (₹)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(4.dp))
            Text("(Duration of hostel, Date of admission, DOB: use the date pickers — wired to a date-picker dialog component in the full build.)",
                style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(16.dp))
            Text("Select Room *", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.horizontalScrollPadding()) {
                rooms.forEach { room ->
                    FilterChip(
                        selected = state.selectedRoomId == room.id,
                        onClick = { viewModel.update { it.copy(selectedRoomId = room.id, selectedBedId = null) } },
                        label = { Text(room.roomNumber) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            if (state.selectedRoomId != null) {
                Spacer(Modifier.height(8.dp))
                Text("Select Bed *", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.horizontalScrollPadding()) {
                    bedsForRoom.forEach { bed ->
                        FilterChip(
                            selected = state.selectedBedId == bed.id,
                            enabled = bed.status == BedStatus.VACANT,
                            onClick = { viewModel.update { it.copy(selectedBedId = bed.id) } },
                            label = { Text("Bed ${bed.bedNumber}${if (bed.status != BedStatus.VACANT) " (occupied)" else ""}") },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Applicant's Declaration and Undertaking", style = MaterialTheme.typography.titleSmall)
            Text(DECLARATION_TEXT, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.declarationAccepted, onCheckedChange = { v -> viewModel.update { it.copy(declarationAccepted = v) } })
                Text("I/We accept the above declaration and undertaking")
            }
            OutlinedTextField(state.declarationAcceptedBy, { v -> viewModel.update { it.copy(declarationAcceptedBy = v) } },
                label = { Text("Accepted by (Parent/Guardian & Student name)") }, modifier = Modifier.fillMaxWidth())

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Admission")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun Modifier.horizontalScrollPadding() = this
