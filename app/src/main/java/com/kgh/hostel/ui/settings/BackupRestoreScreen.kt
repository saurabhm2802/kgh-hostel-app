package com.kgh.hostel.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {
    var statusMessage by mutableStateOf<String?>(null)
        private set

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            try {
                backupManager.exportBackup(uri)
                statusMessage = "Backup created successfully."
            } catch (e: Exception) {
                statusMessage = "Backup failed: ${e.message}"
            }
        }
    }

    fun restoreFrom(uri: Uri) {
        viewModelScope.launch {
            val ok = backupManager.restoreBackup(uri)
            statusMessage = if (ok) "Restore completed. Restart the app to see restored data."
            else "Restore failed — your previous data has been kept."
        }
    }
}

@Composable
fun BackupRestoreScreen(viewModel: BackupRestoreViewModel = hiltViewModel()) {
    var showRestoreWarning by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { viewModel.exportTo(it) }
    }
    val openDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreWarning = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Backup & Restore") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Create a complete backup of students, photos, documents, rooms, attendance, leave, payments and settings.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { createDocLauncher.launch("KGH_Backup_${System.currentTimeMillis()}.kghbackup") }, modifier = Modifier.fillMaxWidth()) {
                Text("BACKUP DATA")
            }
            Spacer(Modifier.height(24.dp))
            Text("Restoring a backup may replace the current data. Please create a backup of your current data before continuing.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { openDocLauncher.launch(arrayOf("application/octet-stream", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("RESTORE DATA")
            }

            viewModel.statusMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it)
            }
        }
    }

    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false },
            title = { Text("Restore Backup") },
            text = { Text("Restoring a backup may replace the current data. Please make sure you have created a backup of your current data before continuing.") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreWarning = false
                    pendingRestoreUri?.let { viewModel.restoreFrom(it) }
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showRestoreWarning = false }) { Text("Cancel") } }
        )
    }
}
