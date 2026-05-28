package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class AppRepository(private val db: AppDatabase) {
    val studentDao = db.studentDao()
    val paymentDao = db.paymentDao()
    val resultDao = db.academicResultDao()
    val recourseDao = db.recourseDao()
    val scheduleDao = db.scheduleDao()
    val chatDao = db.chatDao()
    val annonceDao = db.annonceDao()
    val syllabusDao = db.syllabusDao()
    val attendanceScanDao = db.attendanceScanDao()
    val tpSubmissionDao = db.tpSubmissionDao()
    val forumMessageDao = db.forumMessageDao()
    val stageOfferDao = db.stageOfferDao()

    fun getProfileFlow(email: String): Flow<StudentProfile?> = studentDao.getProfileFlow(email)
    suspend fun getProfile(email: String): StudentProfile? = studentDao.getProfile(email)
    suspend fun insertProfile(profile: StudentProfile) = studentDao.insertProfile(profile)
    suspend fun updateProfile(profile: StudentProfile) = studentDao.updateProfile(profile)

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPaymentsFlow()
    suspend fun insertPayment(payment: Payment) = paymentDao.insertPayment(payment)

    val allResults: Flow<List<AcademicResult>> = resultDao.getAllResultsFlow()
    suspend fun insertResults(results: List<AcademicResult>) = resultDao.insertResults(results)

    val allRecourses: Flow<List<Recourse>> = recourseDao.getAllRecoursesFlow()
    suspend fun insertRecourse(recourse: Recourse) = recourseDao.insertRecourse(recourse)

    val allSchedules: Flow<List<ScheduleItem>> = scheduleDao.getAllSchedulesFlow()
    suspend fun insertSchedules(schedules: List<ScheduleItem>) = scheduleDao.insertSchedules(schedules)

    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessagesFlow()
    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)

    val allAnnonces: Flow<List<Annonce>> = annonceDao.getAllAnnoncesFlow()
    suspend fun insertAnnonce(annonce: Annonce) = annonceDao.insertAnnonce(annonce)

    val allSyllabi: Flow<List<SyllabusDocument>> = syllabusDao.getAllSyllabusFlow()
    suspend fun insertSyllabi(syllabusList: List<SyllabusDocument>) = syllabusDao.insertSyllabi(syllabusList)
    suspend fun updateSyllabusDownload(id: Int, isDownloaded: Boolean, path: String) = syllabusDao.updateDownloadStatus(id, isDownloaded, path)

    val allScans: Flow<List<AttendanceScan>> = attendanceScanDao.getAllScansFlow()
    suspend fun insertScan(scan: AttendanceScan) = attendanceScanDao.insertScan(scan)

    val allTps: Flow<List<TpSubmission>> = tpSubmissionDao.getAllTpsFlow()
    suspend fun insertTps(tps: List<TpSubmission>) = tpSubmissionDao.insertTps(tps)
    suspend fun updateTp(tp: TpSubmission) = tpSubmissionDao.updateTp(tp)

    fun getForumMessagesForModule(module: String): Flow<List<ForumMessage>> = forumMessageDao.getMessagesForModuleFlow(module)
    suspend fun insertForumMessage(msg: ForumMessage) = forumMessageDao.insertForumMessage(msg)

    val allOffers: Flow<List<StageOffer>> = stageOfferDao.getAllOffersFlow()
    suspend fun insertOffers(offers: List<StageOffer>) = stageOfferDao.insertOffers(offers)
    suspend fun setOfferApplied(id: Int, applied: Boolean) = stageOfferDao.setApplied(id, applied)

    // Prepopulate offline database with realistic academic data if empty
    suspend fun prepopulateIfEmpty() {
        val sList = allSchedules.first()
        if (sList.size < 10) {
            scheduleDao.clearSchedules()
            val defaultSchedules = listOf(
                // Lundi
                ScheduleItem(dayOfWeek = "Lundi", courseName = "Compilation & Traducteurs", professor = "Prof. Mukendi", classroom = "Auditoire A1", timeSlot = "08:15 - 10:45"),
                ScheduleItem(dayOfWeek = "Lundi", courseName = "Recherche Opérationnelle", professor = "Dr. Kabamba", classroom = "Auditoire A1", timeSlot = "11:00 - 13:30"),
                ScheduleItem(dayOfWeek = "Lundi", courseName = "Travaux Dirigés: Recherche Opérationnelle", professor = "Assistant Mutombo", classroom = "Labo Math", timeSlot = "14:00 - 16:30"),
                
                // Mardi
                ScheduleItem(dayOfWeek = "Mardi", courseName = "Génie Logiciel II", professor = "Prof. Kasongo", classroom = "Auditoire A1", timeSlot = "08:15 - 10:45"),
                ScheduleItem(dayOfWeek = "Mardi", courseName = "Systèmes Distribués", professor = "Dr. Kabamba", classroom = "Auditoire A1", timeSlot = "11:00 - 13:30"),
                ScheduleItem(dayOfWeek = "Mardi", courseName = "Sécurité Informatique", professor = "Prof. Mwamba", classroom = "Labo Réseau", timeSlot = "14:00 - 16:30"),
                
                // Mercredi
                ScheduleItem(dayOfWeek = "Mercredi", courseName = "Intelligence Artificielle", professor = "Dr. Ilunga", classroom = "Auditoire A1", timeSlot = "08:15 - 10:45"),
                ScheduleItem(dayOfWeek = "Mercredi", courseName = "Administration de Réseaux", professor = "Prof. Mwamba", classroom = "Labo Réseau", timeSlot = "11:00 - 13:30"),
                ScheduleItem(dayOfWeek = "Mercredi", courseName = "Web Sémantique & Ontologies", professor = "Prof. Tshimanga", classroom = "Auditoire A2", timeSlot = "14:00 - 16:30"),
                
                // Jeudi
                ScheduleItem(dayOfWeek = "Jeudi", courseName = "Méthodologie de Recherche Sci.", professor = "Dr. Lukusa", classroom = "Auditoire A1", timeSlot = "08:15 - 10:45"),
                ScheduleItem(dayOfWeek = "Jeudi", courseName = "Développement d'Applications Mobiles", professor = "Prof. Tshimanga", classroom = "Labo Mobile", timeSlot = "11:00 - 13:30"),
                ScheduleItem(dayOfWeek = "Jeudi", courseName = "Travaux Pratiques: Android Compose", professor = "Assistant Banza", classroom = "Labo Mobile", timeSlot = "14:00 - 16:30"),
                
                // Vendredi
                ScheduleItem(dayOfWeek = "Vendredi", courseName = "Éthique & Déontologie Professionnelle", professor = "Mme. Kahinga", classroom = "Auditoire A1", timeSlot = "08:15 - 10:45"),
                ScheduleItem(dayOfWeek = "Vendredi", courseName = "Anglais Technique II", professor = "Mme. Roberts", classroom = "Centenary Hall", timeSlot = "11:00 - 13:30"),
                ScheduleItem(dayOfWeek = "Vendredi", courseName = "Gestion des Projets IT", professor = "Dr. Kabamba", classroom = "Auditoire A2", timeSlot = "14:00 - 16:30"),
                
                // Samedi
                ScheduleItem(dayOfWeek = "Samedi", courseName = "Séminaire de Recherche & Conférences", professor = "Chefs d'Entreprises", classroom = "Auditoire Congo", timeSlot = "09:00 - 12:00")
            )
            scheduleDao.insertSchedules(defaultSchedules)
        }

        val rList = allResults.first()
        if (rList.size < 10) {
            resultDao.clearResults()
            val defaultResults = listOf(
                AcademicResult(courseCode = "INF401", courseName = "Compilation & Algorithmique", grade = 16.5, credits = 5, professor = "Prof. Mukendi"),
                AcademicResult(courseCode = "INF402", courseName = "Systèmes Distribués", grade = 14.0, credits = 4, professor = "Dr. Kabamba"),
                AcademicResult(courseCode = "INF403", courseName = "Base de Données Avancées", grade = 11.5, credits = 4, professor = "Mme. Kahinga"),
                AcademicResult(courseCode = "INF404", courseName = "Génie Logiciel I", grade = 15.0, credits = 5, professor = "Prof. Kasongo"),
                AcademicResult(courseCode = "INF405", courseName = "Réseaux d'Entreprises", grade = 17.0, credits = 4, professor = "Prof. Mwamba"),
                AcademicResult(courseCode = "INF406", courseName = "Sécurité & Cryptographie", grade = 10.0, credits = 3, professor = "Prof. Mwamba"),
                AcademicResult(courseCode = "INF407", courseName = "Interaction Homme-Machine (IHM)", grade = 15.5, credits = 3, professor = "Dr. Ilunga"),
                AcademicResult(courseCode = "INF408", courseName = "Éthique Informatique & Droit IT", grade = 13.0, credits = 2, professor = "Mme. Kahinga"),
                AcademicResult(courseCode = "INF409", courseName = "Intelligence Artificielle", grade = 8.0, credits = 4, professor = "Dr. Ilunga"),
                AcademicResult(courseCode = "INF410", courseName = "Recherche Opérationnelle", grade = 12.0, credits = 4, professor = "Dr. Kabamba"),
                AcademicResult(courseCode = "INF411", courseName = "Administration Unix/Linux", grade = 14.5, credits = 3, professor = "Prof. Tshimanga"),
                AcademicResult(courseCode = "INF412", courseName = "Technologies Web & Cloud", grade = 18.0, credits = 4, professor = "Prof. Kasongo")
            )
            resultDao.insertResults(defaultResults)
        }

        val aList = allAnnonces.first()
        if (aList.isEmpty()) {
            val defaultAnnonces = listOf(
                Annonce(
                    title = "Communiqué: Deuxième Tranche des Frais Académiques",
                    content = "Chers étudiants de l'UDBL, le comité de gestion rappelle que la date limite de paiement de la 2e tranche est fixée au 15 Juin 2026. Veuillez utiliser les canaux mobiles (M-Pesa, Airtel Money, Orange Money) intégrés dans l'application UDBL Vision pour éviter les files d'attente à la banque.",
                    dateString = "25 Mai 2026",
                    category = "Urgent"
                ),
                Annonce(
                    title = "Session d'Examens - Mi-Semestre",
                    content = "L'horaire officiel des examens de mi-semestre a été publié et synchronisé en mode hors-ligne. Veuillez vérifier vos dates respectives par auditoire.",
                    dateString = "22 Mai 2026",
                    category = "Académique"
                ),
                Annonce(
                    title = "Lancement de la Plateforme UDBL Vision",
                    content = "Nous sommes heureux de vous présenter l'application mobile officielle UDBL Vision pour faciliter votre parcours académique. Vous pouvez désormais chatter en direct avec nos agents administratifs pour soumettre vos revendications de notes de manière sûre et rapide.",
                    dateString = "20 Mai 2026",
                    category = "Général"
                )
            )
            annonceDao.insertAnnonces(defaultAnnonces)
        }

        val pList = allPayments.first()
        if (pList.isEmpty()) {
            val defaultPayments = listOf(
                Payment(
                    amount = 150.0,
                    currency = "USD",
                    walletType = "M-Pesa",
                    phoneNumber = "+243812345678",
                    paymentType = "Première Tranche Académique",
                    reference = "UDBL-MP-98741",
                    dateMillis = System.currentTimeMillis() - 86400000 * 5, // 5 days ago
                    isSynced = true
                ),
                Payment(
                    amount = 25.0,
                    currency = "USD",
                    walletType = "Airtel Money",
                    phoneNumber = "+243972345678",
                    paymentType = "Enrôlement Examens",
                    reference = "UDBL-AM-41002",
                    dateMillis = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                    isSynced = true
                )
            )
            for (p in defaultPayments) {
                paymentDao.insertPayment(p)
            }
        }

        val mList = chatMessages.first()
        if (mList.isEmpty()) {
            val defaultMessages = listOf(
                ChatMessage(sender = "ADMIN", content = "Bienvenue sur le service de chat en temps réel d'UDBL VISION. Posez-nous vos questions concernant vos cours, frais ou recours académiques !", dateMillis = System.currentTimeMillis() - 3600000),
                ChatMessage(sender = "STUDENT", content = "Bonjour, j'aimerais savoir si mon recours pour le cours de Compilation a été traité par le secrétariat de l'UDBL.", dateMillis = System.currentTimeMillis() - 1800000),
                ChatMessage(sender = "ADMIN", content = "Bonjour ! Oui, nous avons bien reçu votre recours. Les professeurs ont jusqu'à vendredi pour valider les fiches de notes corrigées. Vous recevrez une alerte de mise à jour s'il y a un changement.", dateMillis = System.currentTimeMillis() - 900000)
            )
            for (m in defaultMessages) {
                chatDao.insertMessage(m)
            }
        }

        // Prepopulate Syllabi
        val syllFlowList = allSyllabi.first()
        if (syllFlowList.isEmpty()) {
            val defaultSyllabi = listOf(
                SyllabusDocument(title = "Syllabus de Compilation & Traducteurs", courseCode = "INF401", professor = "Prof. Mukendi", description = "Analyse lexicale, syntaxique (LL, LR), génération de code intermédiaire et optimisation.", fileSize = "4.2 MB"),
                SyllabusDocument(title = "Syllabus de Systèmes Distribués", courseCode = "INF402", professor = "Dr. Kabamba", description = "Protocoles de consensus, horloges logiques, RPC, et architectures de microservices.", fileSize = "3.8 MB"),
                SyllabusDocument(title = "Syllabus d'Intelligence Artificielle", courseCode = "INF409", professor = "Dr. Ilunga", description = "Réseaux de neurones, recherche heuristique, algorithmes génétiques et logique floue.", fileSize = "5.1 MB"),
                SyllabusDocument(title = "Syllabus de Génie Logiciel II", courseCode = "INF404", professor = "Prof. Kasongo", description = "Patrons de conception (Design Patterns), méthodologies agiles (Scrum/Kanban) et architectures d'entreprise.", fileSize = "4.6 MB")
            )
            syllabusDao.insertSyllabi(defaultSyllabi)
        }

        // Prepopulate TP submissions
        val tpFlowList = allTps.first()
        if (tpFlowList.isEmpty()) {
            val defaultTps = listOf(
                TpSubmission(
                    courseName = "Compilation & Traducteurs",
                    title = "TP 1: Analyseur Lexical en Kotlin",
                    instructions = "Écrire un analyseur lexical capable de reconnaître les jetons (tokens) d'un sous-ensemble du langage Pascal/Kotlin. Soumettre le fichier source .kt ou un .zip.",
                    deadlineMillis = System.currentTimeMillis() + 3600000 * 2, // 2 hours from now
                    isSubmitted = false
                ),
                TpSubmission(
                    courseName = "Développement d'Applications Mobiles",
                    title = "TP 2: Layout Jetpack Compose",
                    instructions = "Créer une interface utilisateur adaptative avec Scaffold, LazyColumn et BoxWithConstraints pour une application universitaire.",
                    deadlineMillis = System.currentTimeMillis() + 86400000 * 3, // 3 days from now
                    isSubmitted = false
                ),
                TpSubmission(
                    courseName = "Systèmes Distribués",
                    title = "TP 3: Implémentation de RPC",
                    instructions = "Mettre en œuvre un protocole d'appel de procédure à distance simple en Java ou Kotlin.",
                    deadlineMillis = System.currentTimeMillis() - 86400000, // 1 day ago (passed)
                    isSubmitted = true,
                    submittedFileName = "tp3_rpc_finished.zip",
                    submittedDateMillis = System.currentTimeMillis() - 95000000,
                    submissionContent = "Code source complet avec sockets UDP/TCP mettant en œuvre le protocole RPC pour le calcul distribué.",
                    grade = 17.5
                )
            )
            tpSubmissionDao.insertTps(defaultTps)
        }

        // Prepopulate Stage Offers
        val stageFlowList = allOffers.first()
        if (stageFlowList.isEmpty()) {
            val defaultOffers = listOf(
                StageOffer(
                    title = "Stagiaire Développeur Mobile Android/Kotlin",
                    companyName = "Vodacom Congo (Lubumbashi)",
                    location = "Lubumbashi",
                    type = "Stage",
                    description = "Intégrez l'équipe de développement de Lubumbashi pour concevoir et faire évoluer des modules de l'application M-Pesa. Travail sur Kotlin et les API REST.",
                    requirements = "Étudiant en L3 ou Master en Informatique, bases solides en programmation orientée objet, connaissance de base de Git.",
                    contactEmail = "careers.lubumbashi@vodacom.cd"
                ),
                StageOffer(
                    title = "Consultant Junior Sécurité des Systèmes",
                    companyName = "Rawbank (Direction Régionale)",
                    location = "Lubumbashi",
                    type = "Emploi Junior",
                    description = "Accompagnez nos analystes de sécurité dans le contrôle des accès et l'audit de sécurité des applications bancaires régionales.",
                    requirements = "Diplômé de l'UDBL, notions en administration Linux, firewall, protocoles SSL/TLS, et tests de pénétration basiques.",
                    contactEmail = "recrutement.katanga@rawbank.cd"
                ),
                StageOffer(
                    title = "Sujet de Mémoire (TFE): Optimisation du dispatching minier par IA",
                    companyName = "Tenke Fungurume Mining",
                    location = "Kolwezi / Lubumbashi",
                    type = "TFE",
                    description = "Projet de fin d'études parrainé visant à développer des algorithmes de recherche opérationnelle et d'IA pour optimiser les mouvements des camions bennes dans la mine.",
                    requirements = "Étudiant en Master II Sciences Informatiques, excellent niveau en mathématiques discrètes, Python et algorithmes d'optimisation.",
                    contactEmail = "parrainage.tfe@tfm.cd"
                )
            )
            stageOfferDao.insertOffers(defaultOffers)
        }
    }
}
