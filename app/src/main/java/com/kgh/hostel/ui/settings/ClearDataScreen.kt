package com.kgh.hostel.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.KGHDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CONFIRM_PHRASE = "CLEAR KGH DATA"

enum class ClearScope { ATTENDANCE_ONLY, OLD_TEMP_DATA, EVERYTHING }

@HiltViewModel
class ClearDataViewModel @Inject constructor(
    private val database: KGHDatabase
) : ViewModel() {
    private fun delete(table: String) {
        database.openHelper.writableDatabase.execSQL("DELETE FROM $table")
    }

    fun clear(scope: ClearScope, onDone: () -> Unit) {
        viewModelScope.launch {
            database.runInTransaction {
                when (scope) {
                    ClearScope.ATTENDANCE_ONLY -> delete("attendance")
                    ClearScope.OLD_TEMP_DATA -> delete("audit_log")
                    ClearScope.EVERYTHING -> {
                        listOf(
                            "attendance", "leave_applications", "hostel_leaving", "payments",
                            "payment_audit_log", "room_change_history", "student_documents",
                            "beds", "rooms", "students", "audit_log"
                        ).forEach { table -> delete(table) }
                    }
                }
            }
            onDone()
        }
    }
}

@Composable
fun ClearDataScreen(viewModel: ClearDataViewModel = hiltViewModel()) {
    var selectedScope by remember { mutableStateOf<ClearScope?>(null) }
    var typedConfirmation by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Clear Data") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("This permanently deletes application records. Uploaded photos/documents are only removed if you choose \"Clear all application data\".",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            listOf(
                ClearScope.ATTENDANCE_ONLY to "Clear attendance only",
                ClearScope.OLD_TEMP_DATA to "Clear old temporary data",
                ClearScope.EVERYTHING to "Clear all application data"
            ).forEach { (scope, label) ->
                OutlinedButton(onClick = { selectedScope = scope; typedConfirmation = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (done) {
                Spacer(Modifier.height(16.dp))
                Text("Data cleared.")
            }
        }
    }

    selectedScope?.let { scope ->
        AlertDialog(
            onDismissRequest = { selectedScope = null },
            title = { Text("Confirm: $scope") },
            text = {
                Column {
                    Text("This action cannot be undone. Please create a backup before continuing.")
                    Spacer(Modifier.height(8.dp))
                    Text("Type \"$CONFIRM_PHRASE\" to confirm:")
                    OutlinedTextField(typedConfirmation, { typedConfirmation = it }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    enabled = typedConfirmation == CONFIRM_PHRASE,
                    onClick = {
                        viewModel.clear(scope) { done = true }
                        selectedScope = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { selectedScope = null }) { Text("Cancel") } }
        )
    }
}
