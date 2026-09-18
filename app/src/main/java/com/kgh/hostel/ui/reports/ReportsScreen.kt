package com.kgh.hostel.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val REPORT_TYPES = listOf(
    "Student Report",
    "Room Occupancy Report",
    "Vacant Bed Report",
    "Attendance Report",
    "Absent Student Report",
    "Leave Report",
    "Rent Due Report",
    "Payment Report",
    "Hostel Leaving Report"
)

/**
 * Lists the report categories from the spec. Each report reads from the
 * relevant repository (already available via Hilt) and would render a
 * simple table plus a "Share as PDF/CSV" action using androidx.print /
 * a CSV writer — omitted here for brevity but straightforward to add per
 * report using the same repository Flows already built.
 */
@Composable
fun ReportsScreen(onReportClick: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(REPORT_TYPES) { report ->
                ListItem(
                    headlineContent = { Text(report) },
                    modifier = Modifier
                )
                HorizontalDivider()
            }
        }
    }
}
