package com.kgh.hostel.ui.payments

import androidx.compose.foundation.clickable
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
import com.kgh.hostel.data.local.entity.Payment
import com.kgh.hostel.data.local.entity.PaymentMethod
import com.kgh.hostel.data.local.entity.PaymentStatus
import com.kgh.hostel.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentScreenViewModel @Inject constructor(
    private val repository: PaymentRepository
) : ViewModel() {
    fun observeForStudent(studentId: Long) = repository.observeForStudent(studentId)

    fun record(studentId: Long, month: String, due: Double, paid: Double, method: PaymentMethod, ref: String, notes: String) {
        viewModelScope.launch {
            repository.save(
                Payment(
                    studentId = studentId, month = month, amountDue = due, amountPaid = paid,
                    paymentDate = System.currentTimeMillis(), method = method,
                    transactionRef = ref.ifBlank { null }, notes = notes.ifBlank { null },
                    status = PaymentStatus.DUE
                )
            )
        }
    }

    fun update(payment: Payment, due: Double, paid: Double, method: PaymentMethod, ref: String, notes: String) {
        viewModelScope.launch {
            repository.save(
                payment.copy(
                    amountDue = due,
                    amountPaid = paid,
                    method = method,
                    transactionRef = ref.ifBlank { null },
                    notes = notes.ifBlank { null }
                )
            )
        }
    }
}

@Composable
fun PaymentScreen(studentId: Long, viewModel: PaymentScreenViewModel = hiltViewModel()) {
    val payments by viewModel.observeForStudent(studentId).collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rent / Payment History") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showAdd = true }, text = { Text("Record Payment") }, icon = {}) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(payments) { p ->
                ListItem(
                    headlineContent = { Text(p.month) },
                    supportingContent = { Text("Due ₹${p.amountDue}  Paid ₹${p.amountPaid}  Balance ₹${p.balance}  •  ${p.status}") },
                    modifier = Modifier.clickable { editingPayment = p },
                    trailingContent = { Text("Edit") }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAdd) {
        var month by remember { mutableStateOf("") }
        var due by remember { mutableStateOf("") }
        var paid by remember { mutableStateOf("") }
        var ref by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var method by remember { mutableStateOf(PaymentMethod.CASH) }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Record Payment") },
            text = {
                Column {
                    OutlinedTextField(month, { month = it }, label = { Text("Month (e.g. 2026-09)") })
                    OutlinedTextField(due, { due = it }, label = { Text("Amount Due") })
                    OutlinedTextField(paid, { paid = it }, label = { Text("Amount Paid") })
                    OutlinedTextField(ref, { ref = it }, label = { Text("Transaction Ref") })
                    OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
                    Row {
                        PaymentMethod.entries.forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.name) }, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.record(studentId, month, due.toDoubleOrNull() ?: 0.0, paid.toDoubleOrNull() ?: 0.0, method, ref, notes)
                    showAdd = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    editingPayment?.let { payment ->
        var due by remember { mutableStateOf(payment.amountDue.toString()) }
        var paid by remember { mutableStateOf(payment.amountPaid.toString()) }
        var ref by remember { mutableStateOf(payment.transactionRef ?: "") }
        var notes by remember { mutableStateOf(payment.notes ?: "") }
        var method by remember { mutableStateOf(payment.method ?: PaymentMethod.CASH) }

        AlertDialog(
            onDismissRequest = { editingPayment = null },
            title = { Text("Edit Payment — ${payment.month}") },
            text = {
                Column {
                    OutlinedTextField(due, { due = it }, label = { Text("Amount Due") })
                    OutlinedTextField(paid, { paid = it }, label = { Text("Amount Paid") })
                    OutlinedTextField(ref, { ref = it }, label = { Text("Transaction Ref") })
                    OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
                    Row {
                        PaymentMethod.entries.forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.name) }, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.update(payment, due.toDoubleOrNull() ?: payment.amountDue, paid.toDoubleOrNull() ?: payment.amountPaid, method, ref, notes)
                    editingPayment = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingPayment = null }) { Text("Cancel") } }
        )
    }
}
