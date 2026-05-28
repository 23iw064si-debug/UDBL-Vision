package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(database)

    // User Session States
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _biometricSetupEnabled = MutableStateFlow(false)
    val biometricSetupEnabled: StateFlow<Boolean> = _biometricSetupEnabled.asStateFlow()

    // Navigation State
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Preferences
    private val _selectedTheme = MutableStateFlow("System") // "System", "Clair", "Sombre"
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _dataSaverMode = MutableStateFlow(false)
    val dataSaverMode: StateFlow<Boolean> = _dataSaverMode.asStateFlow()

    private val _customPushAnnouncements = MutableStateFlow(true)
    val customPushAnnouncements: StateFlow<Boolean> = _customPushAnnouncements.asStateFlow()

    private val _customPushGrades = MutableStateFlow(true)
    val customPushGrades: StateFlow<Boolean> = _customPushGrades.asStateFlow()

    private val _customPushSchedule = MutableStateFlow(true)
    val customPushSchedule: StateFlow<Boolean> = _customPushSchedule.asStateFlow()

    // Live Database Flows
    val currentProfile: StateFlow<StudentProfile?> = _currentUserEmail
        .flatMapLatest { email ->
            if (email.isNotEmpty()) repository.getProfileFlow(email) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPaymentsState: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academicResultsState: StateFlow<List<AcademicResult>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoursesState: StateFlow<List<Recourse>> = repository.allRecourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleState: StateFlow<List<ScheduleItem>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesState: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val annoncesState: StateFlow<List<Annonce>> = repository.allAnnonces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syllabiState: StateFlow<List<SyllabusDocument>> = repository.allSyllabi
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scansState: StateFlow<List<AttendanceScan>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tpsState: StateFlow<List<TpSubmission>> = repository.allTps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offersState: StateFlow<List<StageOffer>> = repository.allOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCourseModuleForForum = MutableStateFlow("Compilation")

    val forumMessagesState: StateFlow<List<ForumMessage>> = selectedCourseModuleForForum
        .flatMapLatest { module -> repository.getForumMessagesForModule(module) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tutorMessagesState = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ADMIN",
                content = "Bonjour ! Je suis l'Assistant IA Tuteur Académique UDBL (Propulsé par Gemini). Posez-moi vos questions par rapport à la compilation, l'IA, le génie logiciel ou toute matière de votre cursus.",
                dateMillis = System.currentTimeMillis()
            )
        )
    )

    val tutorIsLoading = MutableStateFlow(false)

    val dynamicIdQrState = MutableStateFlow("UDBL-SECURE-CARD|PROTOTYPE")
    val secondsUntilQrRefresh = MutableStateFlow(60)

    val localCourseRemindersEnabled = MutableStateFlow(false)

    init {
        // Run database pre-population
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
        // Launch dynamic student card identity rotation loop
        viewModelScope.launch {
            while (true) {
                val email = _currentUserEmail.value.ifEmpty { "john.doe@udbl.ac.cd" }
                val randomSign = UUID.randomUUID().toString().substring(0, 8).uppercase()
                dynamicIdQrState.value = "UDBL-CARD|$email|$randomSign|${System.currentTimeMillis()}"
                for (i in 60 downTo 1) {
                    secondsUntilQrRefresh.value = i
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun login(email: String, simulateBiometrics: Boolean = false): Boolean {
        if (email.isEmpty()) return false
        viewModelScope.launch {
            var profile = repository.getProfile(email)
            if (profile == null) {
                // Initialize a default profile for the user
                profile = StudentProfile(
                    email = email,
                    name = "Mélanie Mwange",
                    matricule = "UDBL/INF/2026/0147",
                    promotion = "Licence 2 (L2)",
                    faculty = "Sciences Informatiques",
                    currentBalancePaid = 175.0,
                    totalFeesRequired = 500.0,
                    biometricEnabled = simulateBiometrics,
                    dataSaverEnabled = _dataSaverMode.value,
                    pushNotificationsEnabled = true
                )
                repository.insertProfile(profile)
            }
            _currentUserEmail.value = email
            _isLoggedIn.value = true
            _biometricSetupEnabled.value = profile.biometricEnabled
            _dataSaverMode.value = profile.dataSaverEnabled
        }
        return true
    }

    fun register(email: String, name: String, matricule: String, promo: String, faculty: String): Boolean {
        if (email.isEmpty() || name.isEmpty() || matricule.isEmpty()) return false
        viewModelScope.launch {
            val profile = StudentProfile(
                email = email,
                name = name,
                matricule = matricule,
                promotion = promo,
                faculty = faculty,
                currentBalancePaid = 0.0,
                totalFeesRequired = 500.0,
                biometricEnabled = false,
                dataSaverEnabled = false,
                pushNotificationsEnabled = true
            )
            repository.insertProfile(profile)
            _currentUserEmail.value = email
            _isLoggedIn.value = true
        }
        return true
    }

    fun updatePreferences(
        theme: String? = null,
        dataSaver: Boolean? = null,
        pushAnnouncements: Boolean? = null,
        pushGrades: Boolean? = null,
        pushSchedule: Boolean? = null,
        biometricSetup: Boolean? = null
    ) {
        viewModelScope.launch {
            theme?.let { _selectedTheme.value = it }
            dataSaver?.let {
                _dataSaverMode.value = it
                updateDatabaseProfile { copy(dataSaverEnabled = it) }
            }
            biometricSetup?.let {
                _biometricSetupEnabled.value = it
                updateDatabaseProfile { copy(biometricEnabled = it) }
            }
            pushAnnouncements?.let { _customPushAnnouncements.value = it }
            pushGrades?.let { _customPushGrades.value = it }
            pushSchedule?.let { _customPushSchedule.value = it }
        }
    }

    private suspend fun updateDatabaseProfile(transform: StudentProfile.() -> StudentProfile) {
        val email = _currentUserEmail.value
        if (email.isNotEmpty()) {
            repository.getProfile(email)?.let { current ->
                repository.updateProfile(current.transform())
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
        _currentTab.value = 0
    }

    // Process Academic Payment
    fun payFees(amount: Double, walletType: String, phone: String, paymentType: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val reference = "UDBL-" + walletType.substring(0, 2).uppercase() + "-" + (10000..99999).random()
            val newPayment = Payment(
                amount = amount,
                currency = "USD",
                walletType = walletType,
                phoneNumber = phone,
                paymentType = paymentType,
                reference = reference,
                dateMillis = System.currentTimeMillis()
            )
            repository.insertPayment(newPayment)

            // Update user balance paid
            val email = _currentUserEmail.value
            if (email.isNotEmpty()) {
                repository.getProfile(email)?.let { current ->
                    val updated = current.copy(currentBalancePaid = current.currentBalancePaid + amount)
                    repository.updateProfile(updated)
                }
            }

            // Simulate immediate Push Notification for Payment success
            if (_customPushAnnouncements.value) {
                repository.insertAnnonce(
                     Annonce(
                         title = "Reçu de paiement généré ! 💳",
                         content = "Votre paiement de $amount USD pour '$paymentType' a été validé avec succès par $walletType (Réf: $reference).",
                         dateString = "A l'instant",
                         category = "Général"
                     )
                )
            }

            onComplete()
        }
    }

    // Submit Academic Recourse (Dispute)
    fun submitRecourse(courseCode: String, courseName: String, expectedGrade: String, explanation: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val recourse = Recourse(
                courseCode = courseCode,
                courseName = courseName,
                expectedGrade = expectedGrade,
                explanation = explanation,
                status = "Soumis",
                dateMillis = System.currentTimeMillis(),
                studentEmail = _currentUserEmail.value
            )
            repository.insertRecourse(recourse)

            // Simulate instant Notification confirmation
            if (_customPushAnnouncements.value) {
                repository.insertAnnonce(
                    Annonce(
                        title = "Recours enregistré: $courseName 🎯",
                        content = "Votre réclamation concernant la note du cours $courseName ($courseCode) a bien été transmise à l'administration.",
                        dateString = "A l'instant",
                        category = "Académique"
                    )
                )
            }

            onComplete()
        }
    }

    // Post message to Live Chat with Admin Simulator
    fun sendChatMessage(content: String) {
        if (content.trim().isEmpty()) return
        viewModelScope.launch {
            val studentMessage = ChatMessage(
                sender = "STUDENT",
                content = content,
                dateMillis = System.currentTimeMillis(),
                isPending = false
            )
            repository.insertMessage(studentMessage)

            // Respond with Admin simulator after a short delay
            kotlinx.coroutines.delay(1200)

            val reply = when {
                content.contains("frais", ignoreCase = true) || content.contains("payer", ignoreCase = true) -> {
                    "Concernant les frais, tout paiement effectué par Mobile Money (M-Pesa, Airtel, Orange) s'enregistre immédiatement dans votre dossier académique locale."
                }
                content.contains("recours", ignoreCase = true) || content.contains("note", ignoreCase = true) || content.contains("reclamation", ignoreCase = true) -> {
                    "Les recours pour le semestre en cours sont traités à la fin de la semaine. Assurez-vous d'avoir rempli l'onglet 'Recours & Résultats' pour chaque note contestée."
                }
                content.contains("horaire", ignoreCase = true) || content.contains("calendrier", ignoreCase = true) || content.contains("cours", ignoreCase = true) -> {
                    "L'horaire de cours est mis à jour chaque lundi matin. Les informations restent accessibles en mode hors-ligne même sans connexion Internet."
                }
                else -> {
                    "Merci pour votre message. Un conseiller académique de l'UDBL répondra plus en détail de manière personnalisée sous peu."
                }
            }

            val adminMessage = ChatMessage(
                sender = "ADMIN",
                content = reply,
                dateMillis = System.currentTimeMillis(),
                isPending = false
            )
            repository.insertMessage(adminMessage)

            // Trigger customizable push notification for chat message if configured
            if (_customPushAnnouncements.value) {
                repository.insertAnnonce(
                    Annonce(
                        title = "Message d'administration reçu 💬",
                        content = reply,
                        dateString = "A l'instant",
                        category = "Général"
                    )
                )
            }
        }
    }

    fun setForumModule(module: String) {
        selectedCourseModuleForForum.value = module
    }

    fun simulateDownloadSyllabus(id: Int) {
        viewModelScope.launch {
            repository.updateSyllabusDownload(id, isDownloaded = true, path = "/storage/emulated/0/Android/data/com.example/files/syllabus_$id.pdf")
            
            // Add alerte of download completion
            repository.insertAnnonce(
                Annonce(
                    title = "Téléchargement Syllabus Complété 📥",
                    content = "Le syllabus a été mis en cache locale avec succès et reste consultable hors-ligne à tout moment sans consommer de données.",
                    dateString = "À l'instant",
                    category = "Académique"
                )
            )
        }
    }

    fun registerAttendance(rawCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (rawCode.isEmpty() || !rawCode.startsWith("UDBL-PRESENCE")) {
                onResult(false, "Code QR invalide ou non reconnu par l'UDBL. Veuillez positionner la caméra face au QR officiel de l'enseignant.")
                return@launch
            }
            
            val parts = rawCode.split("|")
            val course = parts.getOrNull(1) ?: "Cours Inconnu"
            val prof = parts.getOrNull(2) ?: "Professeur"
            
            val scan = AttendanceScan(
                courseName = course,
                verifiedProfessor = prof,
                scanTimeMillis = System.currentTimeMillis(),
                qrRawCode = rawCode,
                status = "Validé"
            )
            repository.insertScan(scan)
            
            repository.insertAnnonce(
                Annonce(
                    title = "Présence validée: $course 🕒",
                    content = "Votre présence a été enregistrée à la leçon de $course dispensée par $prof.",
                    dateString = "À l'instant",
                    category = "Général"
                )
            )
            
            onResult(true, "Présence confirmée avec succès pour le cours de $course avec $prof. Registre numérique synchronisé.")
        }
    }

    fun submitAssignment(tpId: Int, fileName: String, content: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val tpsList = repository.allTps.first()
            val tp = tpsList.find { it.id == tpId }
            if (tp != null) {
                val updatedTp = tp.copy(
                    isSubmitted = true,
                    submittedFileName = fileName,
                    submittedDateMillis = System.currentTimeMillis(),
                    submissionContent = content,
                    grade = -1.0 // Reset to pending review
                )
                repository.updateTp(updatedTp)
                
                repository.insertAnnonce(
                    Annonce(
                        title = "TP soumis: ${tp.title} 📤",
                        content = "Votre travail pratique a bien été transmis aux serveurs académiques de l'UDBL. Statut: En attente d'évaluation.",
                        dateString = "À l'instant",
                        category = "Académique"
                    )
                )
                onComplete()
            }
        }
    }

    fun sendForumMessage(module: String, text: String, isAnonymous: Boolean) {
        viewModelScope.launch {
            if (text.trim().isEmpty()) return@launch
            val profile = currentProfile.value
            val sender = if (isAnonymous) "Utilisateur Anonyme" else (profile?.name ?: "Mélanie Mwange")
            
            val msg = ForumMessage(
                courseModule = module,
                senderName = sender,
                messageContent = text,
                timestampMillis = System.currentTimeMillis(),
                isAnonymous = isAnonymous
            )
            repository.insertForumMessage(msg)
        }
    }

    fun applyToStageOffer(offerId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.setOfferApplied(offerId, applied = true)
            
            val offersList = repository.allOffers.first()
            val offer = offersList.find { it.id == offerId }
            offer?.let {
                repository.insertAnnonce(
                    Annonce(
                        title = "Candidature soumise à ${it.companyName} 💼",
                        content = "Votre candidature pour le poste '${it.title}' a été validée. Votre dossier étudiant complet (Relevé, CV, Certificats) a été transmis au service de recrutement (${it.contactEmail}).",
                        dateString = "À l'instant",
                        category = "Général"
                    )
                )
            }
            onComplete()
        }
    }

    fun setLocalCourseReminders(enabled: Boolean) {
        localCourseRemindersEnabled.value = enabled
        viewModelScope.launch {
            repository.insertAnnonce(
                Annonce(
                    title = if (enabled) "Rappels de cours activés 🔔" else "Rappels désactivés 🔕",
                    content = if (enabled) "L'application vous enverra un rappel sonore local 15 minutes avant le début de chaque cours de votre horaire." else "Les rappels sonores de cours ont été désactivés.",
                    dateString = "À l'instant",
                    category = "Général"
                )
            )
        }
    }

    fun sendTutorQuestion(question: String) {
        if (question.trim().isEmpty()) return
        val currentMsgs = tutorMessagesState.value.toMutableList()
        val userMsg = ChatMessage(
            sender = "STUDENT",
            content = question,
            dateMillis = System.currentTimeMillis()
        )
        currentMsgs.add(userMsg)
        tutorMessagesState.value = currentMsgs
        
        tutorIsLoading.value = true
        
        viewModelScope.launch {
            val responseText = callGeminiApi(question)
            tutorIsLoading.value = false
            
            val updatedMsgs = tutorMessagesState.value.toMutableList()
            val aiMsg = ChatMessage(
                sender = "ADMIN",
                content = responseText,
                dateMillis = System.currentTimeMillis()
            )
            updatedMsgs.add(aiMsg)
            tutorMessagesState.value = updatedMsgs
        }
    }

    private suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // High-fidelity fallback simulator for offline prototype layout
            kotlinx.coroutines.delay(1800)
            return@withContext getTutorFallbackResponse(prompt)
        }
        
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "You are an intelligent Academic AI Tutor for students at UDBL. Format answer with bullet points, brief formulas if applicable, in clean French. Prompt: $prompt")
                            })
                        })
                    })
                })
            }
            
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.getJSONArray("candidates")
                val text = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                text
            } else {
                "Désolé, une erreur technique est survenue au niveau des serveurs Gemini (Code: $responseCode). Fallback: " + getTutorFallbackResponse(prompt)
            }
        } catch (e: Exception) {
            "Désolé, impossible d'établir la connexion aux serveurs de l'IA (erreur local: ${e.message}). Mode tuteur local activé: \n\n" + getTutorFallbackResponse(prompt)
        }
    }

    private fun getTutorFallbackResponse(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("compilation") || query.contains("traducteur") || query.contains("lexical") || query.contains("automate") -> {
                "**Tuteur Compilation & Traducteurs**\n\nDans un compilateur, l'analyseur lexical regroupe les caractères du code source en unités sémantiques appelées **Jetons (Tokens)**.\n\n*   **Automates Finis Déterministes (AFD)**: Utilisés pour implémenter l'analyseur lexical en programmant des transitions d'états de manière efficace.\n*   **Expression Régulière**: Écrite pour définir la structure logique des jetons (identifiants, nombres, mots-clés).\n\nAvez-vous besoin d'un exemple d'implémentation d'automate fini en Kotlin pour le TP1 ?"
            }
            query.contains("intelligence") || query.contains("ia") || query.contains("réseau de neurone") || query.contains("heuristic") -> {
                "**Tuteur Intelligence Artificielle**\n\nPour vos cours à l'UDBL, retenez les concepts piliers:\n\n1.  **Algorithme A\\***: Algorithme de recherche de chemin optimal utilisant l'évaluation récursive f(n) = g(n) + h(n) où h est l'heuristique.\n2.  **Apprentissage Supervisé**: Apprendre à partir d'exemples étiquetés (ex: SVM, Réseaux neuronaux multi-couches avec rétropropagation d'erreur).\n\nSouhaitez-vous explorer le calcul d'une fonction de perte d'entropie croisée ?"
            }
            query.contains("génie logiciel") || query.contains("agile") || query.contains("design pattern") || query.contains("patron") -> {
                "**Tuteur Génie Logiciel II**\n\nL'UDBL enseigne les principes d'architecture d'entreprise solides:\n\n*   **Principes SOLID**: Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion.\n*   **Patron de Conception (Design Pattern)**:\n    *   *Création*: Singleton, Factory, Builder\n    *   *Structure*: Adapter, Composite, Proxy\n    *   *Comportement*: Observer, Strategy, State\n\nQuel pattern spécifique voulez-vous modéliser ?"
            }
            query.contains("système distribué") || query.contains("rpc") || query.contains("consensus") || query.contains("corba") -> {
                "**Tuteur Systèmes Distribués**\n\nLes systèmes distribués gèrent la cohérence des bases de données partagées ou des serveurs distants:\n\n*   **Appel de Procédure à Distance (RPC)**: Permet d'appeler une fonction distante comme si elle était locale, en s'appuyant sur des *stubs* client et squelette serveur.\n*   **Théorème CAP**: Indique qu'un système partagé ne peut garantir simultanément que deux des trois aspects: *Cohérence (Consistency)*, *Disponibilité (Availability)*, et *Tolérance aux Cloisonnements (Partition tolerance)*.\n\nSouhaitez-vous un exemple d'implémentation de Stub RPC sous Kotlin ou Java ?"
            }
            else -> {
                "**Tuteur Académique UDBL**\n\nMerci pour votre question ! Je note vos interrogations académiques. Afin d'aligner vos réponses avec les syllabus officiels de l'Université de Lubumbashi:\n\n*   Les contrôles de TP comptent pour **30%** de la note finale.\n*   Les examens de fin de semestre représentent **70%**.\n*   Ce sujet correspond au programme d'informatique théorique et programmation.\n\nPourriez-vous préciser votre question ou nommer la section spécifique du syllabus ?"
            }
        }
    }
}
