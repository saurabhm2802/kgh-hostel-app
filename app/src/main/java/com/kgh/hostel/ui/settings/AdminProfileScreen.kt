package com.kgh.hostel.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.HostelSettings
import com.kgh.hostel.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostelSettingsViewModel @Inject constructor(
    val repository: SettingsRepository
) : ViewModel() {
    fun save(settings: HostelSettings) {
        viewModelScope.launch { repository.save(settings) }
    }
}

@Composable
fun AdminProfileScreen(viewModel: HostelSettingsViewModel = hiltViewModel()) {
    val current by viewModel.repository.observe().collectAsState(initial = null)
    var name by remember(current) { mutableStateOf(current?.hostelName ?: "My Khandelwal Girls Hostel") }
    var address by remember(current) { mutableStateOf(current?.address ?: "") }
    var contact by remember(current) { mutableStateOf(current?.contactNumber ?: "") }
    var whatsapp by remember(current) { mutableStateOf(current?.whatsappNumber ?: "") }
    var email by remember(current) { mutableStateOf(current?.email ?: "") }
    var warden by remember(current) { mutableStateOf(current?.wardenName ?: "") }

    Scaffold(topBar = { TopAppBar(title = { Text("Admin Profile") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Hostel Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(address, { address = it }, label = { Text("Hostel Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(contact, { contact = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(whatsapp, { whatsapp = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(warden, { warden = it }, label = { Text("Warden/Admin Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("Logo upload: pick from gallery via a photo picker launcher (same pattern as the student photo field) and store the returned URI as logoUri below.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                viewModel.save(
                    HostelSettings(
                        hostelName = name, address = address, contactNumber = contact,
                        whatsappNumber = whatsapp, email = email, wardenName = warden,
                        logoUri = current?.logoUri
                    )
                )
            }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
        }
    }
}
