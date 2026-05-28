package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_profile WHERE email = :email LIMIT 1")
    fun getProfileFlow(email: String): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE email = :email LIMIT 1")
    suspend fun getProfile(email: String): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfile)

    @Update
    suspend fun updateProfile(profile: StudentProfile)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
}

@Dao
interface AcademicResultDao {
    @Query("SELECT * FROM academic_results")
    fun getAllResultsFlow(): Flow<List<AcademicResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<AcademicResult>)

    @Query("DELETE FROM academic_results")
    suspend fun clearResults()
}

@Dao
interface RecourseDao {
    @Query("SELECT * FROM recourses ORDER BY dateMillis DESC")
    fun getAllRecoursesFlow(): Flow<List<Recourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecourse(recourse: Recourse)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items")
    fun getAllSchedulesFlow(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleItem>)

    @Query("DELETE FROM schedule_items")
    suspend fun clearSchedules()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY dateMillis ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface AnnonceDao {
    @Query("SELECT * FROM annonces ORDER BY id DESC")
    fun getAllAnnoncesFlow(): Flow<List<Annonce>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnonce(annonce: Annonce)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnonces(annonces: List<Annonce>)
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_documents")
    fun getAllSyllabusFlow(): Flow<List<SyllabusDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabi(syllabus: List<SyllabusDocument>)

    @Query("UPDATE syllabus_documents SET isDownloaded = :isDownloaded, downloadPath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: Int, isDownloaded: Boolean, path: String)
}

@Dao
interface AttendanceScanDao {
    @Query("SELECT * FROM attendance_scans ORDER BY scanTimeMillis DESC")
    fun getAllScansFlow(): Flow<List<AttendanceScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: AttendanceScan)
}

@Dao
interface TpSubmissionDao {
    @Query("SELECT * FROM tp_submissions ORDER BY deadlineMillis ASC")
    fun getAllTpsFlow(): Flow<List<TpSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTps(tps: List<TpSubmission>)

    @Update
    suspend fun updateTp(tp: TpSubmission)
}

@Dao
interface ForumMessageDao {
    @Query("SELECT * FROM forum_messages WHERE courseModule = :module ORDER BY timestampMillis ASC")
    fun getMessagesForModuleFlow(module: String): Flow<List<ForumMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumMessage(msg: ForumMessage)
}

@Dao
interface StageOfferDao {
    @Query("SELECT * FROM stage_offers")
    fun getAllOffersFlow(): Flow<List<StageOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<StageOffer>)

    @Query("UPDATE stage_offers SET isApplied = :applied WHERE id = :id")
    suspend fun setApplied(id: Int, applied: Boolean)
}
