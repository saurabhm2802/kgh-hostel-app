package com.kgh.hostel.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val REPORT_TYPES = listOf(
    "student" to "Student Report",
    "room_occupancy" to "Room Occupancy Report",
    "vacant_bed" to "Vacant Bed Report",
    "attendance" to "Attendance Report",
    "absent" to "Absent Student Report",
    "leave" to "Leave Report",
    "rent_due" to "Rent Due Report",
    "payment" to "Payment Report",
    "hostel_leaving" to "Hostel Leaving Report"
)

@Composable
fun ReportsScreen(onReportClick: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(REPORT_TYPES) { (slug, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    modifier = Modifier.clickable { onReportClick(slug) }
                )
                HorizontalDivider()
            }
        }
    }
}
