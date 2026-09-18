package com.kgh.hostel.ui.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.kgh.hostel.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RentDueViewModel @Inject constructor(
    val repository: PaymentRepository
) : ViewModel()

@Composable
fun RentDueScreen(viewModel: RentDueViewModel = hiltViewModel()) {
    val due by viewModel.repository.observeDue().collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Rent Due") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp)) {
            items(due) { payment ->
                ListItem(
                    headlineContent = { Text("Student #${payment.studentId} — ${payment.month}") },
                    supportingContent = { Text("Due: ₹${payment.amountDue}  Paid: ₹${payment.amountPaid}  Balance: ₹${payment.balance}") },
                    trailingContent = {
                        TextButton(onClick = { /* Send WhatsApp Reminder — wired via ACTION_SEND to wa.me in full build */ }) {
                            Text("Remind")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
