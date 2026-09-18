package com.kgh.hostel.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kgh.hostel.ui.components.QuickActionButton
import com.kgh.hostel.ui.components.StatCard

@Composable
fun DashboardScreen(
    onNewAdmission: () -> Unit,
    onAttendance: () -> Unit,
    onStudents: () -> Unit,
    onVacantBeds: () -> Unit,
    onRentDue: () -> Unit,
    onBackup: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("KGH Dashboard") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    listOf(
                        Triple("Total Students", state.totalStudents.toString(), Icons.Default.Groups),
                        Triple("Present Today", state.presentToday.toString(), Icons.Default.CheckCircle),
                        Triple("Absent Today", state.absentToday.toString(), Icons.Default.Cancel),
                        Triple("Total Rooms", state.totalRooms.toString(), Icons.Default.MeetingRoom),
                        Triple("Total Beds", state.totalBeds.toString(), Icons.Default.Bed),
                        Triple("Occupied Beds", state.occupiedBeds.toString(), Icons.Default.EventSeat),
                        Triple("Vacant Beds", state.vacantBeds.toString(), Icons.Default.EventAvailable),
                        Triple("Rent Due", state.rentDue.toString(), Icons.Default.CurrencyRupee),
                        Triple("On Leave Today", state.onLeaveToday.toString(), Icons.Default.FlightTakeoff)
                    )
                ) { (title, value, icon) ->
                    StatCard(title = title, value = value, icon = icon)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            QuickActionButton("New Admission", Icons.Default.PersonAdd, onNewAdmission)
            Spacer(Modifier.height(6.dp))
            QuickActionButton("Attendance", Icons.Default.FactCheck, onAttendance)
            Spacer(Modifier.height(6.dp))
            QuickActionButton("Students", Icons.Default.Groups, onStudents)
            Spacer(Modifier.height(6.dp))
            QuickActionButton("Vacant Beds", Icons.Default.EventAvailable, onVacantBeds)
            Spacer(Modifier.height(6.dp))
            QuickActionButton("Rent Due", Icons.Default.CurrencyRupee, onRentDue)
            Spacer(Modifier.height(6.dp))
            QuickActionButton("Backup", Icons.Default.Backup, onBackup)
        }
    }
}
