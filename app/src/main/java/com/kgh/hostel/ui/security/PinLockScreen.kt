package com.kgh.hostel.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.kgh.hostel.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {
    fun isPinSet() = pinManager.isPinSet()
    fun setPin(pin: String) = pinManager.setPin(pin)
    fun verify(pin: String) = pinManager.verifyPin(pin)
}

@Composable
fun PinLockScreen(onUnlocked: () -> Unit, viewModel: PinViewModel = hiltViewModel()) {
    val pinAlreadySet = remember { viewModel.isPinSet() }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("KGH") }) }) { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("My Khandelwal Girls Hostel", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            if (pinAlreadySet) {
                Text("Enter Admin PIN")
                OutlinedTextField(
                    value = pin, onValueChange = { pin = it }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    if (viewModel.verify(pin)) onUnlocked() else error = "Incorrect PIN."
                }, modifier = Modifier.fillMaxWidth()) { Text("Unlock") }
            } else {
                Text("Create an Admin PIN to protect student data")
                OutlinedTextField(
                    value = pin, onValueChange = { pin = it }, singleLine = true, label = { Text("New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPin, onValueChange = { confirmPin = it }, singleLine = true, label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    if (pin.length < 4) { error = "PIN must be at least 4 digits." }
                    else if (pin != confirmPin) { error = "PINs do not match." }
                    else { viewModel.setPin(pin); onUnlocked() }
                }, modifier = Modifier.fillMaxWidth()) { Text("Set PIN & Continue") }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
