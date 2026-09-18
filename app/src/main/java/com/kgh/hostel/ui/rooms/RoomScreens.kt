package com.kgh.hostel.ui.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.BedStatus
import com.kgh.hostel.data.local.entity.HostelRoom
import com.kgh.hostel.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomListViewModel @Inject constructor(
    val repository: RoomRepository
) : ViewModel() {
    fun addRoom(number: String, floor: String, type: String, beds: Int, rent: Double) {
        viewModelScope.launch {
            repository.addRoomWithBeds(
                HostelRoom(roomNumber = number, floor = floor, roomType = type, totalBeds = beds, monthlyRent = rent)
            )
        }
    }
}

@Composable
fun RoomListScreen(onRoomClick: (Long) -> Unit, viewModel: RoomListViewModel = hiltViewModel()) {
    val rooms by viewModel.repository.observeRooms().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rooms") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(rooms) { room ->
                ListItem(
                    headlineContent = { Text("Room ${room.roomNumber}") },
                    supportingContent = { Text("Floor ${room.floor} • ${room.totalBeds} beds • ₹${room.monthlyRent}/month") },
                    modifier = Modifier.clickable { onRoomClick(room.id) }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAdd) {
        AddRoomDialog(onDismiss = { showAdd = false }, onSave = { n, f, t, b, r -> viewModel.addRoom(n, f, t, b, r); showAdd = false })
    }
}

@Composable
private fun AddRoomDialog(onDismiss: () -> Unit, onSave: (String, String, String, Int, Double) -> Unit) {
    var number by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Standard") }
    var beds by remember { mutableStateOf("4") }
    var rent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room") },
        text = {
            Column {
                OutlinedTextField(number, { number = it }, label = { Text("Room Number") })
                OutlinedTextField(floor, { floor = it }, label = { Text("Floor") })
                OutlinedTextField(type, { type = it }, label = { Text("Room Type") })
                OutlinedTextField(beds, { beds = it }, label = { Text("Number of Beds") })
                OutlinedTextField(rent, { rent = it }, label = { Text("Monthly Rent") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(number, floor, type, beds.toIntOrNull() ?: 1, rent.toDoubleOrNull() ?: 0.0)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun VacantBedsScreen(viewModel: RoomListViewModel = hiltViewModel()) {
    val beds by viewModel.repository.observeVacantBeds().collectAsState(initial = emptyList())
    Scaffold(topBar = { TopAppBar(title = { Text("Vacant Beds") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(beds) { bed ->
                ListItem(
                    headlineContent = { Text("Room ${bed.roomId} — Bed ${bed.bedNumber}") },
                    supportingContent = { Text(bed.status.name) }
                )
                HorizontalDivider()
            }
        }
    }
}
