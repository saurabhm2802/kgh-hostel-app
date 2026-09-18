package com.kgh.hostel.ui.students

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StudentListViewModel @Inject constructor(
    repository: StudentRepository
) : ViewModel() {
    val query = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val students: StateFlow<List<Student>> = query
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) repository.observeActive() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun StudentListScreen(
    onStudentClick: (Long) -> Unit,
    onNewAdmission: () -> Unit,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val students by viewModel.students.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Students") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewAdmission) { Icon(Icons.Default.Add, contentDescription = "New Admission") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.query.value = it },
                label = { Text("Search by name, mobile, room, admission no.") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(students) { student ->
                    ListItem(
                        headlineContent = { Text(student.name) },
                        supportingContent = { Text("Room ${student.currentRoomId ?: "-"} • ${student.phoneStudent}") },
                        modifier = Modifier.clickable { onStudentClick(student.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
