package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentProfile::class,
        Payment::class,
        AcademicResult::class,
        Recourse::class,
        ScheduleItem::class,
        ChatMessage::class,
        Annonce::class,
        SyllabusDocument::class,
        AttendanceScan::class,
        TpSubmission::class,
        ForumMessage::class,
        StageOffer::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun academicResultDao(): AcademicResultDao
    abstract fun recourseDao(): RecourseDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun chatDao(): ChatDao
    abstract fun annonceDao(): AnnonceDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun attendanceScanDao(): AttendanceScanDao
    abstract fun tpSubmissionDao(): TpSubmissionDao
    abstract fun forumMessageDao(): ForumMessageDao
    abstract fun stageOfferDao(): StageOfferDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "udbl_vision_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
