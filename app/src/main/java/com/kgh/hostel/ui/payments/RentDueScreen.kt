package com.kgh.hostel.ui.payments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.Payment
import com.kgh.hostel.data.local.entity.PaymentMethod
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.PaymentRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentDueViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    val dueWithStudents = combine(
        paymentRepository.observeDue(),
        studentRepository.observeAll()
    ) { due, students ->
        val nameMap = students.associateBy { it.id }
        due.map { payment -> payment to nameMap[payment.studentId] }
    }

    fun updatePayment(payment: Payment, newAmountPaid: Double, method: PaymentMethod, ref: String, notes: String) {
        viewModelScope.launch {
            paymentRepository.save(
                payment.copy(
                    amountPaid = newAmountPaid,
                    method = method,
                    transactionRef = ref.ifBlank { null },
                    notes = notes.ifBlank { null },
                    paymentDate = System.currentTimeMillis()
                )
            )
        }
    }
}

@Composable
fun RentDueScreen(viewModel: RentDueViewModel = hiltViewModel()) {
    val dueList by viewModel.dueWithStudents.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var editingPayment by remember { mutableStateOf<Pair<Payment, Student?>?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Rent Due") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(dueList) { (payment, student) ->
                ListItem(
                    headlineContent = { Text(student?.name ?: "Student #${payment.studentId}") },
                    supportingContent = { Text("${payment.month} • Due ₹${payment.amountDue}  Paid ₹${payment.amountPaid}  Balance ₹${payment.balance}") },
                    modifier = Modifier.clickable { editingPayment = payment to student },
                    trailingContent = {
                        TextButton(onClick = {
                            val phone = student?.parentMobile?.ifBlank { student.phoneStudent } ?: student?.phoneStudent
                            if (!phone.isNullOrBlank()) {
                                val digits = phone.filter { it.isDigit() }
                                val withCountryCode = if (digits.length == 10) "91$digits" else digits
                                val message = "Dear ${student?.name ?: "Student"}, this is a reminder that your hostel rent of ₹${payment.balance} for ${payment.month} is due. Kindly clear the dues at the earliest. - Khandelwal Girls Hostel"
                                val uri = Uri.parse("https://wa.me/$withCountryCode?text=${Uri.encode(message)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }) {
                            Text("Remind")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    editingPayment?.let { (payment, student) ->
        var paid by remember { mutableStateOf(payment.amountPaid.toString()) }
        var ref by remember { mutableStateOf(payment.transactionRef ?: "") }
        var notes by remember { mutableStateOf(payment.notes ?: "") }
        var method by remember { mutableStateOf(payment.method ?: PaymentMethod.CASH) }

        AlertDialog(
            onDismissRequest = { editingPayment = null },
            title = { Text("Edit Payment — ${student?.name ?: "Student #${payment.studentId}"}") },
            text = {
                Column {
                    Text("Month: ${payment.month}  •  Due: ₹${payment.amountDue}")
                    Spacer(Modifier.height(8.dp))
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
                    viewModel.updatePayment(payment, paid.toDoubleOrNull() ?: payment.amountPaid, method, ref, notes)
                    editingPayment = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingPayment = null }) { Text("Cancel") } }
        )
    }
}
