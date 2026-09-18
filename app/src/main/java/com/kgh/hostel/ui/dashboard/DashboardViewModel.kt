package com.kgh.hostel.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgh.hostel.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class DashboardState(
    val totalStudents: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val totalRooms: Int = 0,
    val totalBeds: Int = 0,
    val occupiedBeds: Int = 0,
    val vacantBeds: Int = 0,
    val rentDue: Int = 0,
    val onLeaveToday: Int = 0
)

private fun todayMidnight(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    studentRepository: StudentRepository,
    roomRepository: RoomRepository,
    attendanceRepository: AttendanceRepository,
    paymentRepository: PaymentRepository,
    leaveRepository: LeaveRepository
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        studentRepository.observeActiveCount(),
        attendanceRepository.observePresentCount(todayMidnight()),
        attendanceRepository.observeAbsentCount(todayMidnight()),
        roomRepository.observeRooms().map { it.size },
        roomRepository.observeTotalBeds(),
        roomRepository.observeOccupiedBeds(),
        roomRepository.observeVacantBedCount(),
        paymentRepository.observeDueCount(),
        leaveRepository.observeOnLeaveTodayCount(todayMidnight())
    ) { values ->
        DashboardState(
            totalStudents = values[0] as Int,
            presentToday = values[1] as Int,
            absentToday = values[2] as Int,
            totalRooms = values[3] as Int,
            totalBeds = values[4] as Int,
            occupiedBeds = values[5] as Int,
            vacantBeds = values[6] as Int,
            rentDue = values[7] as Int,
            onLeaveToday = values[8] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}
