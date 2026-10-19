package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CampusZone(
    val id: String,
    val title: String,
    val shortName: String,
    val buildingRef: String,
    val description: String,
    val mapX: Float,
    val mapY: Float
) {
    LIBRARY(
        id = "LIBRARY",
        title = "Biblioteca Central FESC",
        shortName = "Biblioteca",
        buildingRef = "Bloque B • Piso 2",
        description = "Mesas grupales amplias, pizarrones acrílicos y silencio moderado.",
        mapX = 0.50f,
        mapY = 0.26f
    ),
    CUBICLES(
        id = "CUBICLES",
        title = "Cubículos de Estudio",
        shortName = "Cubículos",
        buildingRef = "Bloque B • Piso 3",
        description = "Espacios cerrados con pantalla para simulacros de parcial y tutorías.",
        mapX = 0.22f,
        mapY = 0.42f
    ),
    LABS(
        id = "LABS",
        title = "Laboratorios & Cómputo",
        shortName = "Laboratorios",
        buildingRef = "Bloque A • Piso 3",
        description = "Equipos de desarrollo, redes, diseño gráfico y práctica de código.",
        mapX = 0.78f,
        mapY = 0.40f
    ),
    PLAZA(
        id = "PLAZA",
        title = "Áreas Comunes & Plazoleta",
        shortName = "Plazoleta",
        buildingRef = "Zona Central • Piso 1",
        description = "Mesas al aire libre ideales para debate abierto y repaso rápido.",
        mapX = 0.32f,
        mapY = 0.74f
    ),
    CAFETERIA(
        id = "CAFETERIA",
        title = "Cafetería Universitaria",
        shortName = "Cafetería",
        buildingRef = "Bloque C • Piso 1",
        description = "Estudio colaborativo con café, enchufes cercanos y ambiente dinámico.",
        mapX = 0.74f,
        mapY = 0.74f
    );

    companion object {
        fun fromId(id: String): CampusZone =
            entries.find { it.id == id } ?: LIBRARY
    }
}

enum class StudentStatusMode(
    val id: String,
    val label: String,
    val badgeText: String
) {
    STUDYING(
        id = "STUDYING",
        label = "En mesa abierta • Acepto compañeros",
        badgeText = "En Mesa Abierta"
    ),
    MENTOR(
        id = "MENTOR",
        label = "Modo Mentor • Disponible para explicar",
        badgeText = "Mentor Disponible"
    ),
    LOOKING_GROUP(
        id = "LOOKING_GROUP",
        label = "En el campus • Buscando grupo",
        badgeText = "Buscando Grupo"
    ),
    SOS_ACTIVE(
        id = "SOS_ACTIVE",
        label = "Alerta SOS • Necesito ayuda urgente",
        badgeText = "En Alerta SOS"
    );

    companion object {
        fun fromId(id: String): StudentStatusMode =
            entries.find { it.id == id } ?: STUDYING
    }
}

val FESC_PROGRAMS = listOf(
    "Ingeniería de Software",
    "Diseño Gráfico",
    "Administración Financiera",
    "Negocios Internacionales",
    "Administración Turística y Hotelera",
    "Logística Empresarial"
)

val FESC_COMMON_SUBJECTS = listOf(
    "Cálculo Diferencial e Integral",
    "Estructura de Datos y Algoritmos",
    "Programación Orientada a Objetos",
    "Contabilidad General y Costos",
    "Estadística Inferencial",
    "Bases de Datos Relacionales",
    "Matemática Financiera",
    "Diseño de Interfaces UX/UI"
)

@IgnoreExtraProperties
data class StudentProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val program: String = "Ingeniería de Software",
    val semester: Long = 4L,
    val currentZone: String = CampusZone.LIBRARY.id,
    val statusMode: String = StudentStatusMode.STUDYING.id,
    val enrolledSubjects: List<String> = emptyList(),
    val helpedPeersCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyTable(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val topic: String = "",
    val campusZone: String = CampusZone.LIBRARY.id,
    val spotReference: String = "",
    val studyVibe: String = "Resolución de ejercicios",
    val hostName: String = "",
    val hostProgram: String = "",
    val maxSeats: Long = 5L,
    val occupiedSeats: Long = 1L,
    val participantNames: List<String> = emptyList(),
    val isJoinedByCurrentUser: Boolean = false,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val availableSeats: Int
        get() = (maxSeats - occupiedSeats).coerceAtLeast(0L).toInt()

    val isFull: Boolean
        get() = occupiedSeats >= maxSeats
}

@IgnoreExtraProperties
data class SosAlert(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val questionTitle: String = "",
    val questionDetail: String = "",
    val examUrgency: String = "Parcial en 2 horas",
    val campusZone: String = CampusZone.LIBRARY.id,
    val spotReference: String = "",
    val authorName: String = "",
    val authorProgram: String = "",
    val isResolved: Boolean = false,
    val helperNames: List<String> = emptyList(),
    val isCurrentUserHelping: Boolean = false,
    val responsesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CollaborationMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val parentId: String = "",
    val parentType: String = "TABLE",
    val senderName: String = "",
    val senderRole: String = "",
    val content: String = "",
    val isGoingInPerson: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
