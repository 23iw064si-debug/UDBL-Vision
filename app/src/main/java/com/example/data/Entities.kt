package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val email: String,
    val name: String,
    val matricule: String,
    val promotion: String,
    val faculty: String,
    val currentBalancePaid: Double,
    val totalFeesRequired: Double,
    val biometricEnabled: Boolean = false,
    val dataSaverEnabled: Boolean = false,
    val pushNotificationsEnabled: Boolean = true
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val walletType: String, // "M-Pesa", "Orange Money", "Airtel Money"
    val phoneNumber: String,
    val paymentType: String, // "Frais Académiques", "Enrôlement", "Frais Connexes"
    val reference: String,
    val dateMillis: Long,
    val isSynced: Boolean = true
)

@Entity(tableName = "academic_results")
data class AcademicResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCode: String,
    val courseName: String,
    val grade: Double,
    val maxGrade: Double = 20.0,
    val credits: Int,
    val professor: String
)

@Entity(tableName = "recourses")
data class Recourse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCode: String,
    val courseName: String,
    val expectedGrade: String,
    val explanation: String,
    val status: String, // "Soumis", "En cours de traitement", "Validé", "Rejeté"
    val dateMillis: Long,
    val studentEmail: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "Lundi", "Mardi", etc.
    val courseName: String,
    val professor: String,
    val classroom: String,
    val timeSlot: String, // "08:00 - 10:30", "10:45 - 13:15", etc.
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "STUDENT", "ADMIN"
    val content: String,
    val dateMillis: Long,
    val isPending: Boolean = false // Off-line indicator
)

@Entity(tableName = "annonces")
data class Annonce(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val dateString: String,
    val category: String, // "Urgent", "Académique", "Général"
    val pushNotified: Boolean = false
)

@Entity(tableName = "syllabus_documents")
data class SyllabusDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val courseCode: String,
    val professor: String,
    val description: String,
    val fileSize: String,
    val isDownloaded: Boolean = false,
    val downloadPath: String = ""
)

@Entity(tableName = "attendance_scans")
data class AttendanceScan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseName: String,
    val verifiedProfessor: String,
    val scanTimeMillis: Long,
    val qrRawCode: String,
    val status: String = "Validé"
)

@Entity(tableName = "tp_submissions")
data class TpSubmission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseName: String,
    val title: String,
    val instructions: String,
    val deadlineMillis: Long,
    val submittedFileName: String = "",
    val submittedDateMillis: Long = 0,
    val submissionContent: String = "",
    val grade: Double = -1.0, // -1.0 means pending review
    val isSubmitted: Boolean = false
)

@Entity(tableName = "forum_messages")
data class ForumMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseModule: String, // e.g. "Compilation", "Systemes Distribues"
    val senderName: String,
    val messageContent: String,
    val timestampMillis: Long,
    val isAnonymous: Boolean = false
)

@Entity(tableName = "stage_offers")
data class StageOffer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val companyName: String,
    val location: String = "Lubumbashi",
    val type: String = "Stage", // "Stage", "TFE", "Emploi Junior"
    val description: String,
    val requirements: String,
    val contactEmail: String,
    val isApplied: Boolean = false
)
