package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CampusZone
import com.example.data.model.CollaborationMessage
import com.example.data.model.SosAlert
import com.example.data.model.StudentProfile
import com.example.data.model.StudentStatusMode
import com.example.data.model.StudyTable
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SynapseRepository(
    private val db: FirebaseFirestore?,
    private val isLocalSession: Boolean = false,
    private val localUserId: String = "local_student_fesc"
) {
    constructor(context: Context) : this(
        db = kotlin.runCatching {
            val resId = context.resources.getIdentifier(
                "firestore_database_id",
                "string",
                context.packageName
            )
            val dbId = if (resId != 0) context.getString(resId) else "(default)"
            FirebaseFirestore.getInstance(dbId)
        }.getOrNull(),
        isLocalSession = false
    )

    private val useLocalStore: Boolean
        get() = isLocalSession || db == null

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val localProfileFlow = MutableStateFlow<StudentProfile?>(null)
    private val localTablesFlow = MutableStateFlow<List<StudyTable>>(emptyList())
    private val localSosFlow = MutableStateFlow<List<SosAlert>>(emptyList())
    private val localMessagesFlow = MutableStateFlow<List<CollaborationMessage>>(emptyList())

    private fun requireUserId(): String {
        if (useLocalStore) return localUserId
        return auth?.currentUser?.uid
            ?: throw IllegalStateException("Debes iniciar sesión con Google antes de usar FESC Synapse.")
    }

    fun observeStudentProfile(userId: String): Flow<StudentProfile?> {
        val firestore = db
        if (useLocalStore || firestore == null) return localProfileFlow.asStateFlow()
        return callbackFlow {
            val path = "users/$userId"
            val registration = firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.GET, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    val profile = if (snapshot != null && snapshot.exists()) {
                        snapshot.toObject(
                            StudentProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                    trySend(profile)
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeStudyTables(userId: String): Flow<List<StudyTable>> {
        val firestore = db
        if (useLocalStore || firestore == null) return localTablesFlow.asStateFlow()
        return callbackFlow {
            val path = "study_tables"
            val registration = firestore.collection(path)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    val tables = snapshot?.toObjects(
                        StudyTable::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                    trySend(tables)
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeSosAlerts(userId: String): Flow<List<SosAlert>> {
        val firestore = db
        if (useLocalStore || firestore == null) return localSosFlow.asStateFlow()
        return callbackFlow {
            val path = "sos_alerts"
            val registration = firestore.collection(path)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    val alerts = snapshot?.toObjects(
                        SosAlert::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )?.sortedWith(
                        compareBy<SosAlert> { it.isResolved }
                            .thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                    ) ?: emptyList()
                    trySend(alerts)
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeMessages(userId: String): Flow<List<CollaborationMessage>> {
        val firestore = db
        if (useLocalStore || firestore == null) return localMessagesFlow.asStateFlow()
        return callbackFlow {
            val path = "collaboration_messages"
            val registration = firestore.collection(path)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    val messages = snapshot?.toObjects(
                        CollaborationMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )?.sortedBy { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                    trySend(messages)
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun getStudyTableById(tableId: String): Result<StudyTable?> = runCatching {
        val firestore = db
        if (useLocalStore || firestore == null) {
            return@runCatching localTablesFlow.value.find { it.id == tableId }
        }
        val path = "study_tables/$tableId"
        try {
            val doc = firestore.collection("study_tables").document(tableId).get().await()
            doc.toObject(StudyTable::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun saveOrUpdateProfile(
        displayName: String,
        program: String,
        semester: Int,
        currentZone: String,
        statusMode: String,
        enrolledSubjects: List<String>,
        helpedPeersCount: Int
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val safeName = displayName.trim().ifBlank { "Estudiante FESC" }.take(80)
        val safeProgram = program.trim().ifBlank { "Ingeniería de Software" }.take(80)
        val safeSemester = semester.coerceIn(1, 12)
        val safeZone = currentZone.trim().ifBlank { CampusZone.LIBRARY.id }.take(60)
        val safeStatus = statusMode.trim().ifBlank { StudentStatusMode.STUDYING.id }.take(60)
        val safeSubjects = enrolledSubjects.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(10)
        val safeHelped = helpedPeersCount.coerceIn(0, 10000)

        val firestore = db
        if (useLocalStore || firestore == null) {
            val now = Timestamp.now()
            localProfileFlow.value = StudentProfile(
                id = uid,
                userId = uid,
                displayName = safeName,
                program = safeProgram,
                semester = safeSemester.toLong(),
                currentZone = safeZone,
                statusMode = safeStatus,
                enrolledSubjects = safeSubjects,
                helpedPeersCount = safeHelped.toLong(),
                createdAt = localProfileFlow.value?.createdAt ?: now,
                updatedAt = now
            )
            return@runCatching
        }

        val docRef = firestore.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "program" to safeProgram,
                    "semester" to safeSemester,
                    "currentZone" to safeZone,
                    "statusMode" to safeStatus,
                    "enrolledSubjects" to safeSubjects,
                    "helpedPeersCount" to safeHelped,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "program" to safeProgram,
                    "semester" to safeSemester,
                    "currentZone" to safeZone,
                    "statusMode" to safeStatus,
                    "enrolledSubjects" to safeSubjects,
                    "helpedPeersCount" to safeHelped,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun createStudyTable(
        subject: String,
        topic: String,
        campusZone: String,
        spotReference: String,
        studyVibe: String,
        hostName: String,
        hostProgram: String,
        maxSeats: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "table_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val cleanHost = hostName.trim().ifBlank { "Estudiante FESC" }.take(80)
        val cleanProgram = hostProgram.trim().ifBlank { "Ingeniería de Software" }.take(80)
        val cleanMaxSeats = maxSeats.coerceIn(2, 12)

        val firestore = db
        if (useLocalStore || firestore == null) {
            val now = Timestamp.now()
            val newTable = StudyTable(
                id = docId,
                userId = uid,
                subject = subject.trim().take(100),
                topic = topic.trim().take(200),
                campusZone = campusZone.trim().take(50),
                spotReference = spotReference.trim().take(120),
                studyVibe = studyVibe.trim().take(60),
                hostName = cleanHost,
                hostProgram = cleanProgram,
                maxSeats = cleanMaxSeats.toLong(),
                occupiedSeats = 1L,
                participantNames = listOf(cleanHost),
                isJoinedByCurrentUser = true,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            localTablesFlow.value = listOf(newTable) + localTablesFlow.value
            return@runCatching docId
        }

        val docRef = firestore.collection("study_tables").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "subject" to subject.trim().take(100),
            "topic" to topic.trim().take(200),
            "campusZone" to campusZone.trim().take(50),
            "spotReference" to spotReference.trim().take(120),
            "studyVibe" to studyVibe.trim().take(60),
            "hostName" to cleanHost,
            "hostProgram" to cleanProgram,
            "maxSeats" to cleanMaxSeats,
            "occupiedSeats" to 1,
            "participantNames" to listOf(cleanHost),
            "isJoinedByCurrentUser" to true,
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleJoinStudyTable(
        table: StudyTable,
        studentName: String
    ): Result<Boolean> = runCatching {
        requireUserId()
        require(table.id.isNotBlank()) { "El ID de la mesa no puede estar vacío." }
        val cleanStudent = studentName.trim().ifBlank { "Tú (FESC)" }.take(80)

        val joining = !table.isJoinedByCurrentUser
        val updatedParticipants = if (joining) {
            (table.participantNames + cleanStudent).distinct().take(table.maxSeats.toInt())
        } else {
            val filtered = table.participantNames.filterNot {
                it.equals(cleanStudent, ignoreCase = true) || it.startsWith("Tú")
            }
            if (filtered.isEmpty()) listOf(table.hostName.ifBlank { "Anfitrión FESC" }) else filtered
        }
        val newOccupied = updatedParticipants.size.coerceIn(1, table.maxSeats.toInt())

        val firestore = db
        if (useLocalStore || firestore == null) {
            localTablesFlow.value = localTablesFlow.value.map { existing ->
                if (existing.id == table.id) {
                    existing.copy(
                        occupiedSeats = newOccupied.toLong(),
                        participantNames = updatedParticipants,
                        isJoinedByCurrentUser = joining,
                        updatedAt = Timestamp.now()
                    )
                } else {
                    existing
                }
            }
            return@runCatching joining
        }

        val docRef = firestore.collection("study_tables").document(table.id)
        val updateMap = mapOf(
            "occupiedSeats" to newOccupied,
            "participantNames" to updatedParticipants,
            "isJoinedByCurrentUser" to joining,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updateMap).await()
            joining
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteStudyTable(tableId: String): Result<Unit> = runCatching {
        requireUserId()
        val firestore = db
        if (useLocalStore || firestore == null) {
            localTablesFlow.value = localTablesFlow.value.filterNot { it.id == tableId }
            return@runCatching
        }
        val docRef = firestore.collection("study_tables").document(tableId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createSosAlert(
        subject: String,
        questionTitle: String,
        questionDetail: String,
        examUrgency: String,
        campusZone: String,
        spotReference: String,
        authorName: String,
        authorProgram: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "sos_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val cleanAuthor = authorName.trim().ifBlank { "Estudiante FESC" }.take(80)
        val cleanProgram = authorProgram.trim().ifBlank { "Ingeniería de Software" }.take(80)

        val firestore = db
        if (useLocalStore || firestore == null) {
            val now = Timestamp.now()
            val newAlert = SosAlert(
                id = docId,
                userId = uid,
                subject = subject.trim().take(100),
                questionTitle = questionTitle.trim().take(140),
                questionDetail = questionDetail.trim().take(500),
                examUrgency = examUrgency.trim().take(60),
                campusZone = campusZone.trim().take(50),
                spotReference = spotReference.trim().take(120),
                authorName = cleanAuthor,
                authorProgram = cleanProgram,
                isResolved = false,
                helperNames = emptyList(),
                isCurrentUserHelping = false,
                responsesCount = 0L,
                createdAt = now,
                updatedAt = now
            )
            localSosFlow.value = listOf(newAlert) + localSosFlow.value
            return@runCatching docId
        }

        val docRef = firestore.collection("sos_alerts").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "subject" to subject.trim().take(100),
            "questionTitle" to questionTitle.trim().take(140),
            "questionDetail" to questionDetail.trim().take(500),
            "examUrgency" to examUrgency.trim().take(60),
            "campusZone" to campusZone.trim().take(50),
            "spotReference" to spotReference.trim().take(120),
            "authorName" to cleanAuthor,
            "authorProgram" to cleanProgram,
            "isResolved" to false,
            "helperNames" to emptyList<String>(),
            "isCurrentUserHelping" to false,
            "responsesCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun offerHelpOnSos(
        alert: SosAlert,
        helperName: String,
        helperProgram: String,
        customMessage: String? = null
    ): Result<Unit> = runCatching {
        requireUserId()
        require(alert.id.isNotBlank()) { "El ID de la alerta SOS no puede estar vacío." }
        val cleanHelper = helperName.trim().ifBlank { "Mentor FESC" }.take(80)
        val updatedHelpers = (alert.helperNames + cleanHelper).distinct().take(10)
        val newResponsesCount = (alert.responsesCount.toInt() + 1).coerceIn(0, 1000)
        val msgText = customMessage?.trim()?.takeIf { it.isNotEmpty() }
            ?: "¡Voy en camino a ${alert.spotReference} (${CampusZone.fromId(alert.campusZone).shortName}) para explicarte paso a paso!"

        val firestore = db
        if (useLocalStore || firestore == null) {
            localSosFlow.value = localSosFlow.value.map { existing ->
                if (existing.id == alert.id) {
                    existing.copy(
                        helperNames = updatedHelpers,
                        isCurrentUserHelping = true,
                        responsesCount = newResponsesCount.toLong(),
                        updatedAt = Timestamp.now()
                    )
                } else {
                    existing
                }
            }
            postCollaborationMessage(
                parentId = alert.id,
                parentType = "SOS",
                senderName = cleanHelper,
                senderRole = helperProgram.ifBlank { "Mentor Par FESC" },
                content = msgText,
                isGoingInPerson = true
            ).getOrThrow()
            return@runCatching
        }

        val docRef = firestore.collection("sos_alerts").document(alert.id)
        try {
            docRef.update(
                mapOf(
                    "helperNames" to updatedHelpers,
                    "isCurrentUserHelping" to true,
                    "responsesCount" to newResponsesCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            postCollaborationMessage(
                parentId = alert.id,
                parentType = "SOS",
                senderName = cleanHelper,
                senderRole = helperProgram.ifBlank { "Mentor Par FESC" },
                content = msgText,
                isGoingInPerson = true
            ).getOrThrow()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleSosResolved(alert: SosAlert): Result<Boolean> = runCatching {
        requireUserId()
        val newResolved = !alert.isResolved
        val firestore = db
        if (useLocalStore || firestore == null) {
            localSosFlow.value = localSosFlow.value.map { existing ->
                if (existing.id == alert.id) {
                    existing.copy(isResolved = newResolved, updatedAt = Timestamp.now())
                } else {
                    existing
                }
            }
            return@runCatching newResolved
        }

        val docRef = firestore.collection("sos_alerts").document(alert.id)
        try {
            docRef.update(
                mapOf(
                    "isResolved" to newResolved,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            newResolved
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun postCollaborationMessage(
        parentId: String,
        parentType: String,
        senderName: String,
        senderRole: String,
        content: String,
        isGoingInPerson: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val cleanType = if (parentType == "SOS") "SOS" else "TABLE"
        val cleanSender = senderName.trim().ifBlank { "Estudiante FESC" }.take(80)
        val cleanRole = senderRole.trim().ifBlank { "Par Académico" }.take(60)
        val cleanContent = content.trim().take(500)

        val firestore = db
        if (useLocalStore || firestore == null) {
            val now = Timestamp.now()
            val newMessage = CollaborationMessage(
                id = msgId,
                userId = uid,
                parentId = parentId.trim().take(128),
                parentType = cleanType,
                senderName = cleanSender,
                senderRole = cleanRole,
                content = cleanContent,
                isGoingInPerson = isGoingInPerson,
                createdAt = now,
                updatedAt = now
            )
            localMessagesFlow.value = localMessagesFlow.value + newMessage
            return@runCatching msgId
        }

        val docRef = firestore.collection("collaboration_messages").document(msgId)
        val payload = mapOf(
            "userId" to uid,
            "parentId" to parentId.trim().take(128),
            "parentType" to cleanType,
            "senderName" to cleanSender,
            "senderRole" to cleanRole,
            "content" to cleanContent,
            "isGoingInPerson" to isGoingInPerson,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            msgId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun seedCampusNetworkScenario(
        studentName: String,
        studentProgram: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        saveOrUpdateProfile(
            displayName = studentName,
            program = studentProgram,
            semester = 5,
            currentZone = CampusZone.LIBRARY.id,
            statusMode = StudentStatusMode.STUDYING.id,
            enrolledSubjects = listOf(
                "Cálculo Diferencial e Integral",
                "Estructura de Datos y Algoritmos",
                "Contabilidad General y Costos",
                "Estadística Inferencial"
            ),
            helpedPeersCount = 4
        ).getOrThrow()

        data class SeedTable(
            val id: String,
            val subject: String,
            val topic: String,
            val campusZone: String,
            val spotReference: String,
            val studyVibe: String,
            val hostName: String,
            val hostProgram: String,
            val maxSeats: Int,
            val occupiedSeats: Int,
            val participantNames: List<String>,
            val isJoinedByCurrentUser: Boolean
        )

        val sampleTables = listOf(
            SeedTable(
                id = "table_fesc_calc_lib",
                subject = "Cálculo Diferencial e Integral",
                topic = "Integrales por sustitución trigonométrica y fracciones parciales",
                campusZone = CampusZone.LIBRARY.id,
                spotReference = "Biblioteca Piso 2 • Mesa 4 junto al ventanal",
                studyVibe = "Resolución de taller",
                hostName = "Camila Vargas",
                hostProgram = "Ingeniería de Software • 4° Sem",
                maxSeats = 5,
                occupiedSeats = 3,
                participantNames = listOf("Camila Vargas", "Andrés Peñaranda", "Juliana Mora"),
                isJoinedByCurrentUser = false
            ),
            SeedTable(
                id = "table_fesc_algo_labs",
                subject = "Estructura de Datos y Algoritmos",
                topic = "Árboles AVL, recorridos BFS/DFS y complejidad Big-O para el parcial",
                campusZone = CampusZone.LABS.id,
                spotReference = "Laboratorio 302 Bloque A • Mesón Central",
                studyVibe = "Práctica en código",
                hostName = "Mateo Contreras",
                hostProgram = "Ingeniería de Software • 5° Sem",
                maxSeats = 6,
                occupiedSeats = 4,
                participantNames = listOf("Mateo Contreras", "Laura Jaimes", "Diego Leal", "Kevin Rincón"),
                isJoinedByCurrentUser = false
            ),
            SeedTable(
                id = "table_fesc_fin_cub",
                subject = "Contabilidad General y Costos",
                topic = "Balance general, estado de resultados y cierre contable",
                campusZone = CampusZone.CUBICLES.id,
                spotReference = "Cubículo de Estudio #3 (Con tablero acrílico)",
                studyVibe = "Simulacro de parcial",
                hostName = "Valentina Quintero",
                hostProgram = "Administración Financiera • 4° Sem",
                maxSeats = 4,
                occupiedSeats = 2,
                participantNames = listOf("Valentina Quintero", "Santiago Gómez"),
                isJoinedByCurrentUser = false
            ),
            SeedTable(
                id = "table_fesc_ux_plaza",
                subject = "Diseño de Interfaces UX/UI",
                topic = "Heurísticas de Nielsen, sistemas de grillas y prototipado móvil",
                campusZone = CampusZone.PLAZA.id,
                spotReference = "Plazoleta Principal • Mesa sombreada #2",
                studyVibe = "Debate y revisión",
                hostName = "Daniela Serrano",
                hostProgram = "Diseño Gráfico • 6° Sem",
                maxSeats = 5,
                occupiedSeats = 2,
                participantNames = listOf("Daniela Serrano", "Felipe Duarte"),
                isJoinedByCurrentUser = false
            ),
            SeedTable(
                id = "table_fesc_stat_cafe",
                subject = "Estadística Inferencial",
                topic = "Distribución normal, intervalos de confianza y pruebas de hipótesis",
                campusZone = CampusZone.CAFETERIA.id,
                spotReference = "Cafetería Central • Mesa larga frente a barra",
                studyVibe = "Ejercicios paso a paso",
                hostName = "Sebastián Lindarte",
                hostProgram = "Negocios Internacionales • 5° Sem",
                maxSeats = 6,
                occupiedSeats = 3,
                participantNames = listOf("Sebastián Lindarte", "Paula Laguado", "Nicolás Vera"),
                isJoinedByCurrentUser = false
            )
        )

        data class SeedSos(
            val id: String,
            val subject: String,
            val questionTitle: String,
            val questionDetail: String,
            val examUrgency: String,
            val campusZone: String,
            val spotReference: String,
            val authorName: String,
            val authorProgram: String,
            val helperNames: List<String>,
            val responsesCount: Int
        )

        val sampleSos = listOf(
            SeedSos(
                id = "sos_fesc_calc_1",
                subject = "Cálculo Diferencial e Integral",
                questionTitle = "Auxilio con optimización de derivadas antes del parcial de las 6 PM",
                questionDetail = "Estoy en el ejercicio 7 de la guía del profesor: hallar las dimensiones del cilindro inscrito en un cono para maximizar volumen. No logro plantear la ecuación secundaria.",
                examUrgency = "Parcial en 1 hora",
                campusZone = CampusZone.LIBRARY.id,
                spotReference = "Biblioteca Piso 2 • Mesa individual #9 (buzo negro)",
                authorName = "Alejandro Chacón",
                authorProgram = "Ingeniería de Software • 3° Sem",
                helperNames = listOf("Camila Vargas"),
                responsesCount = 1
            ),
            SeedSos(
                id = "sos_fesc_poo_2",
                subject = "Programación Orientada a Objetos",
                questionTitle = "Error con polimorfismo e interfaces en el proyecto del parcial",
                questionDetail = "Mi clase abstracta no me deja sobrescribir el método calcularLiquidacion() cuando implemento la interfaz en el módulo de nómina. ¿Alguien en salas de cómputo me explica 5 minutos?",
                examUrgency = "Parcial en 2 horas",
                campusZone = CampusZone.LABS.id,
                spotReference = "Sala de Cómputo 301 • Equipo #14",
                authorName = "Mariana Bautista",
                authorProgram = "Ingeniería de Software • 4° Sem",
                helperNames = emptyList(),
                responsesCount = 0
            ),
            SeedSos(
                id = "sos_fesc_fin_3",
                subject = "Matemática Financiera",
                questionTitle = "¿Cómo convertir tasa efectiva anual a nominal trimestre anticipado?",
                questionDetail = "Tengo la fórmula de equivalencia de tasas pero al amortizar la tabla con cuota fija me da una diferencia en el tercer periodo. Estoy con calculadora financiera en cubículos.",
                examUrgency = "Parcial hoy 7:00 PM",
                campusZone = CampusZone.CUBICLES.id,
                spotReference = "Cubículo #1 • Entrada derecha",
                authorName = "Carlos Eduardo Niño",
                authorProgram = "Administración Financiera • 5° Sem",
                helperNames = listOf("Valentina Quintero"),
                responsesCount = 1
            )
        )

        val firestore = db
        if (useLocalStore || firestore == null) {
            val now = Timestamp.now()
            localTablesFlow.value = sampleTables.map { t ->
                StudyTable(
                    id = "${uid}_${t.id}",
                    userId = uid,
                    subject = t.subject,
                    topic = t.topic,
                    campusZone = t.campusZone,
                    spotReference = t.spotReference,
                    studyVibe = t.studyVibe,
                    hostName = t.hostName,
                    hostProgram = t.hostProgram,
                    maxSeats = t.maxSeats.toLong(),
                    occupiedSeats = t.occupiedSeats.toLong(),
                    participantNames = t.participantNames,
                    isJoinedByCurrentUser = t.isJoinedByCurrentUser,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            }
            localSosFlow.value = sampleSos.map { s ->
                SosAlert(
                    id = "${uid}_${s.id}",
                    userId = uid,
                    subject = s.subject,
                    questionTitle = s.questionTitle,
                    questionDetail = s.questionDetail,
                    examUrgency = s.examUrgency,
                    campusZone = s.campusZone,
                    spotReference = s.spotReference,
                    authorName = s.authorName,
                    authorProgram = s.authorProgram,
                    isResolved = false,
                    helperNames = s.helperNames,
                    isCurrentUserHelping = false,
                    responsesCount = s.responsesCount.toLong(),
                    createdAt = now,
                    updatedAt = now
                )
            }
            localMessagesFlow.value = listOf(
                CollaborationMessage(
                    id = "${uid}_msg_init_1",
                    userId = uid,
                    parentId = "${uid}_sos_fesc_calc_1",
                    parentType = "SOS",
                    senderName = "Camila Vargas",
                    senderRole = "Mentora • Ing. Software",
                    content = "¡Usa semejanza de triángulos! H_cono / R_cono = (H_cono - h_cilindro) / r_cilindro. De ahí despejas h en función de r. Estamos a dos mesas de ti si quieres verlo en papel.",
                    isGoingInPerson = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
            return@runCatching
        }

        for (t in sampleTables) {
            val docRef = firestore.collection("study_tables").document("${uid}_${t.id}")
            val payload = mapOf(
                "userId" to uid,
                "subject" to t.subject,
                "topic" to t.topic,
                "campusZone" to t.campusZone,
                "spotReference" to t.spotReference,
                "studyVibe" to t.studyVibe,
                "hostName" to t.hostName,
                "hostProgram" to t.hostProgram,
                "maxSeats" to t.maxSeats,
                "occupiedSeats" to t.occupiedSeats,
                "participantNames" to t.participantNames,
                "isJoinedByCurrentUser" to t.isJoinedByCurrentUser,
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        }

        for (s in sampleSos) {
            val docId = "${uid}_${s.id}"
            val docRef = firestore.collection("sos_alerts").document(docId)
            val payload = mapOf(
                "userId" to uid,
                "subject" to s.subject,
                "questionTitle" to s.questionTitle,
                "questionDetail" to s.questionDetail,
                "examUrgency" to s.examUrgency,
                "campusZone" to s.campusZone,
                "spotReference" to s.spotReference,
                "authorName" to s.authorName,
                "authorProgram" to s.authorProgram,
                "isResolved" to false,
                "helperNames" to s.helperNames,
                "isCurrentUserHelping" to false,
                "responsesCount" to s.responsesCount,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        }

        val initialMsgRef = firestore.collection("collaboration_messages").document("${uid}_msg_init_1")
        initialMsgRef.set(
            mapOf(
                "userId" to uid,
                "parentId" to "${uid}_sos_fesc_calc_1",
                "parentType" to "SOS",
                "senderName" to "Camila Vargas",
                "senderRole" to "Mentora • Ing. Software",
                "content" to "¡Usa semejanza de triángulos! H_cono / R_cono = (H_cono - h_cilindro) / r_cilindro. De ahí despejas h en función de r. Estamos a dos mesas de ti si quieres verlo en papel.",
                "isGoingInPerson" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
