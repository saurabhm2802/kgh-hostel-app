package com.kgh.hostel.data.local.dao

import androidx.room.*
import com.kgh.hostel.data.local.entity.HostelSettings
import com.kgh.hostel.data.local.entity.Payment
import com.kgh.hostel.data.local.entity.PaymentAuditLog
import com.kgh.hostel.data.local.entity.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Insert
    suspend fun insertAudit(audit: PaymentAuditLog)

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY month DESC")
    fun observeForStudent(studentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status IN ('DUE','PARTIALLY_PAID','OVERDUE') ORDER BY month ASC")
    fun observeDue(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY month DESC")
    fun observeAll(): Flow<List<Payment>>

    @Query("SELECT COUNT(*) FROM payments WHERE status IN ('DUE','PARTIALLY_PAID','OVERDUE')")
    fun observeDueCount(): Flow<Int>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getById(id: Long): Payment?

    @Transaction
    suspend fun recordEdit(updated: Payment, oldAmountPaid: Double) {
        insertAudit(PaymentAuditLog(paymentId = updated.id, oldAmountPaid = oldAmountPaid, newAmountPaid = updated.amountPaid))
        update(updated)
    }
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM hostel_settings WHERE id = 0 LIMIT 1")
    fun observe(): Flow<HostelSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: HostelSettings)
}
