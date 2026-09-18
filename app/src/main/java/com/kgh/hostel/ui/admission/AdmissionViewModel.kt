package com.kgh.hostel.ui.admission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.local.entity.Student
import com.kgh.hostel.data.local.entity.StudentStatus
import com.kgh.hostel.data.repository.RoomRepository
import com.kgh.hostel.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdmissionFormState(
    val name: String = "",
    val dob: Long? = null,
    val fatherName: String = "",
    val permanentAddress: String = "",
    val phoneStudent: String = "",
    val parentMobile: String = "",
    val alternateMobile: String = "",
    val localGuardianName: String = "",
    val localGuardianAddress: String = "",
    val instituteName: String = "",
    val durationFrom: Long? = null,
    val durationTo: Long? = null,
    val admissionDate: Long = System.currentTimeMillis(),
    val monthlyRent: String = "",
    val photoUri: String? = null,
    val declarationAccepted: Boolean = false,
    val declarationAcceptedBy: String = "",
    val selectedRoomId: Long? = null,
    val selectedBedId: Long? = null,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class AdmissionViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    val roomRepository: RoomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdmissionFormState())
    val state: StateFlow<AdmissionFormState> = _state

    fun update(transform: (AdmissionFormState) -> AdmissionFormState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.fatherName.isBlank() || s.phoneStudent.isBlank() ||
            s.parentMobile.isBlank() || s.instituteName.isBlank() ||
            s.durationFrom == null || s.durationTo == null
        ) {
            _state.value = s.copy(error = "Please fill all required fields (marked *).")
            return
        }
        if (!s.declarationAccepted) {
            _state.value = s.copy(error = "The declaration & undertaking must be accepted before saving.")
            return
        }
        if (s.selectedRoomId == null || s.selectedBedId == null) {
            _state.value = s.copy(error = "Please select a room and bed for the student.")
            return
        }
        viewModelScope.launch {
            val student = Student(
                admissionNumber = "", // generated in repository
                name = s.name,
                dob = s.dob,
                fatherName = s.fatherName,
                permanentAddress = s.permanentAddress,
                phoneStudent = s.phoneStudent,
                parentMobile = s.parentMobile,
                alternateMobile = s.alternateMobile.ifBlank { null },
                localGuardianName = s.localGuardianName.ifBlank { null },
                localGuardianAddress = s.localGuardianAddress.ifBlank { null },
                instituteName = s.instituteName,
                durationFrom = s.durationFrom,
                durationTo = s.durationTo,
                admissionDate = s.admissionDate,
                declarationAccepted = s.declarationAccepted,
                declarationAcceptedBy = s.declarationAcceptedBy,
                photoUri = s.photoUri,
                monthlyRent = s.monthlyRent.toDoubleOrNull() ?: 0.0,
                status = StudentStatus.ACTIVE
            )
            studentRepository.admitStudent(student, s.selectedRoomId, s.selectedBedId)
            _state.value = s.copy(saved = true)
        }
    }
}
