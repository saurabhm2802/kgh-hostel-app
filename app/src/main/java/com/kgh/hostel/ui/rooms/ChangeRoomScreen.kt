package com.kgh.hostel.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.Bed
import com.kgh.hostel.data.local.entity.BedStatus
import com.kgh.hostel.data.repository.RoomRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRoomViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    val roomRepository: RoomRepository
) : ViewModel() {
    fun observeStudent(studentId: Long) = studentRepository.observeById(studentId)

    fun changeRoom(studentId: Long, newRoomId: Long, newBedId: Long, reason: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val current = studentRepository.observeById(studentId).first() ?: return@launch
            studentRepository.changeRoom(current, newRoomId, newBedId, reason)
            onDone()
        }
    }
}

@Composable
fun ChangeRoomScreen(studentId: Long, onDone: () -> Unit, viewModel: ChangeRoomViewModel = hiltViewModel()) {
    val student by viewModel.observeStudent(studentId).collectAsState(initial = null)
    val rooms by viewModel.roomRepository.observeRooms().collectAsState(initial = emptyList())
    var selectedRoomId by remember { mutableStateOf<Long?>(null) }
    var bedsForRoom by remember { mutableStateOf<List<Bed>>(emptyList()) }
    var selectedBedId by remember { mutableStateOf<Long?>(null) }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(selectedRoomId) {
        selectedRoomId?.let { roomId -> viewModel.roomRepository.observeBedsForRoom(roomId).collect { bedsForRoom = it } }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Change Room / Bed") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Student: ${student?.name ?: "..."}")
            Text("Current: Room ${student?.currentRoomId ?: "-"} — Bed ${student?.currentBedId ?: "-"}")
            Spacer(Modifier.height(16.dp))
            Text("Change To Room:", style = MaterialTheme.typography.titleSmall)
            Row {
                rooms.forEach { room ->
                    FilterChip(
                        selected = selectedRoomId == room.id,
                        onClick = { selectedRoomId = room.id; selectedBedId = null },
                        label = { Text(room.roomNumber) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
            if (selectedRoomId != null) {
                Spacer(Modifier.height(8.dp))
                Text("Select Bed:", style = MaterialTheme.typography.titleSmall)
                Row {
                    bedsForRoom.forEach { bed ->
                        FilterChip(
                            selected = selectedBedId == bed.id,
                            enabled = bed.status == BedStatus.VACANT,
                            onClick = { selectedBedId = bed.id },
                            label = { Text("Bed ${bed.bedNumber}") },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(reason, { reason = it }, label = { Text("Reason for change") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = selectedRoomId != null && selectedBedId != null,
                onClick = { viewModel.changeRoom(studentId, selectedRoomId!!, selectedBedId!!, reason, onDone) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirm Room Change") }
        }
    }
}
