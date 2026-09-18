package com.kgh.hostel.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(
    onLeaveList: () -> Unit,
    onRentDue: () -> Unit,
    onReports: () -> Unit,
    onBackupRestore: () -> Unit,
    onClearData: () -> Unit,
    onAdminProfile: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("More") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            listOf(
                "Leave Applications" to onLeaveList,
                "Rent Due" to onRentDue,
                "Reports" to onReports,
                "Backup & Restore" to onBackupRestore,
                "Clear Data" to onClearData,
                "Admin Profile & Settings" to onAdminProfile
            ).forEach { (label, action) ->
                OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) { Text(label) }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
