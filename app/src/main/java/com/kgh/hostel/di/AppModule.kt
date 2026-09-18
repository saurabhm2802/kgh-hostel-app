package com.kgh.hostel.di

import android.content.Context
import androidx.room.Room
import com.kgh.hostel.data.local.KGHDatabase
import com.kgh.hostel.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KGHDatabase =
        Room.databaseBuilder(context, KGHDatabase::class.java, KGHDatabase.DATABASE_NAME)
            // No destructive fallback: schema changes must ship a real Migration
            // so a normal app update never wipes hostel data.
            .build()

    @Provides fun provideStudentDao(db: KGHDatabase): StudentDao = db.studentDao()
    @Provides fun provideRoomBedDao(db: KGHDatabase): RoomBedDao = db.roomBedDao()
    @Provides fun provideAttendanceDao(db: KGHDatabase): AttendanceDao = db.attendanceDao()
    @Provides fun provideLeaveDao(db: KGHDatabase): LeaveDao = db.leaveDao()
    @Provides fun provideHostelLeavingDao(db: KGHDatabase): HostelLeavingDao = db.hostelLeavingDao()
    @Provides fun providePaymentDao(db: KGHDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideSettingsDao(db: KGHDatabase): SettingsDao = db.settingsDao()
}
