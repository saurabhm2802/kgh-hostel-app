package com.kgh.hostel.data.repository

import com.kgh.hostel.data.local.dao.PaymentDao
import com.kgh.hostel.data.local.dao.SettingsDao
import com.kgh.hostel.data.local.entity.HostelSettings
import com.kgh.hostel.data.local.entity.Payment
import com.kgh.hostel.data.local.entity.PaymentStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val dao: PaymentDao
) {
    fun observeForStudent(studentId: Long) = dao.observeForStudent(studentId)
    fun observeDue() = dao.observeDue()
    fun observeDueCount() = dao.observeDueCount()

    suspend fun save(payment: Payment) {
        val status = when {
            payment.amountPaid >= payment.amountDue -> PaymentStatus.PAID
            payment.amountPaid > 0 -> PaymentStatus.PARTIALLY_PAID
            else -> PaymentStatus.DUE
        }
        val withStatus = payment.copy(status = status, updatedAt = System.currentTimeMillis())
        if (payment.id == 0L) {
            dao.insert(withStatus)
        } else {
            val old = dao.getById(payment.id)
            if (old != null && old.amountPaid != payment.amountPaid) {
                dao.recordEdit(withStatus, old.amountPaid)
            } else {
                dao.update(withStatus)
            }
        }
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dao: SettingsDao
) {
    fun observe() = dao.observe()
    suspend fun save(settings: HostelSettings) = dao.save(settings)
}
