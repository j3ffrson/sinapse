package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CampusZone
import com.example.data.model.CollaborationMessage
import com.example.data.model.SosAlert
import com.example.data.model.StudentProfile
import com.example.data.model.StudentStatusMode
import com.example.data.model.StudyTable
import com.example.data.model.UiState
import com.example.data.repository.SynapseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SynapseViewModel(
    private val repository: SynapseRepository,
    private val currentUserId: String,
    private val initialDisplayName: String
) : ViewModel() {

    private var hasTriggeredInitialSeed = false

    val profileState: StateFlow<UiState<StudentProfile?>> = repository
        .observeStudentProfile(currentUserId)
        .map<StudentProfile?, UiState<StudentProfile?>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing student profile", error)
            emit(UiState.Error(error.message ?: "Error al cargar el perfil estudiantil"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val studyTablesState: StateFlow<UiState<List<StudyTable>>> = repository
        .observeStudyTables(currentUserId)
        .map<List<StudyTable>, UiState<List<StudyTable>>> { tables ->
            if (tables.isEmpty() && !hasTriggeredInitialSeed) {
                hasTriggeredInitialSeed = true
                seedDefaultCampusScenario()
            }
            UiState.Success(tables)
        }
        .catch { error ->
            Log.w(TAG, "Error observing study tables", error)
            emit(UiState.Error(error.message ?: "Error al observar las mesas de estudio"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val sosAlertsState: StateFlow<UiState<List<SosAlert>>> = repository
        .observeSosAlerts(currentUserId)
        .map<List<SosAlert>, UiState<List<SosAlert>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing SOS alerts", error)
            emit(UiState.Error(error.message ?: "Error al observar alertas SOS"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val messagesState: StateFlow<UiState<List<CollaborationMessage>>> = repository
        .observeMessages(currentUserId)
        .map<List<CollaborationMessage>, UiState<List<CollaborationMessage>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing messages", error)
            emit(UiState.Error(error.message ?: "Error al observar mensajes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _selectedZoneFilter = MutableStateFlow<CampusZone?>(null)
    val selectedZoneFilter: StateFlow<CampusZone?> = _selectedZoneFilter.asStateFlow()

    private val _selectedSubjectFilter = MutableStateFlow<String?>(null)
    val selectedSubjectFilter: StateFlow<String?> = _selectedSubjectFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun selectZoneFilter(zone: CampusZone?) {
        _selectedZoneFilter.value = zone
    }

    fun selectSubjectFilter(subject: String?) {
        _selectedSubjectFilter.value = if (_selectedSubjectFilter.value == subject) null else subject
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearBannerMessage() {
        _bannerMessage.value = null
    }

    private fun currentProfileOrDefault(): StudentProfile {
        val current = (profileState.value as? UiState.Success)?.data
        return current ?: StudentProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = initialDisplayName.ifBlank { "Estudiante FESC" },
            program = "Ingeniería de Software",
            semester = 5L,
            currentZone = CampusZone.LIBRARY.id,
            statusMode = StudentStatusMode.STUDYING.id,
            enrolledSubjects = listOf(
                "Cálculo Diferencial e Integral",
                "Estructura de Datos y Algoritmos",
                "Contabilidad General y Costos"
            ),
            helpedPeersCount = 4L
        )
    }

    fun checkInToZone(zone: CampusZone, statusMode: StudentStatusMode? = null) {
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            val result = repository.saveOrUpdateProfile(
                displayName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Estudiante FESC" } },
                program = profile.program,
                semester = profile.semester.toInt(),
                currentZone = zone.id,
                statusMode = statusMode?.id ?: profile.statusMode,
                enrolledSubjects = profile.enrolledSubjects,
                helpedPeersCount = profile.helpedPeersCount.toInt()
            )
            result.onSuccess {
                _bannerMessage.value = "Check-in activo en ${zone.title}"
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo actualizar tu ubicación"
            }
        }
    }

    fun updateProfileDetails(
        displayName: String,
        program: String,
        semester: Int,
        currentZone: CampusZone,
        statusMode: StudentStatusMode,
        enrolledSubjects: List<String>
    ) {
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveOrUpdateProfile(
                displayName = displayName,
                program = program,
                semester = semester,
                currentZone = currentZone.id,
                statusMode = statusMode.id,
                enrolledSubjects = enrolledSubjects,
                helpedPeersCount = profile.helpedPeersCount.toInt()
            )
            _isBusy.value = false
            result.onSuccess {
                _bannerMessage.value = "Perfil sináptico actualizado en el campus"
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo guardar el perfil"
            }
        }
    }

    fun createNewStudyTable(
        subject: String,
        topic: String,
        campusZone: CampusZone,
        spotReference: String,
        studyVibe: String,
        maxSeats: Int
    ) {
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createStudyTable(
                subject = subject,
                topic = topic,
                campusZone = campusZone.id,
                spotReference = spotReference,
                studyVibe = studyVibe,
                hostName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Estudiante FESC" } },
                hostProgram = "${profile.program} • ${profile.semester}° Sem",
                maxSeats = maxSeats
            )
            _isBusy.value = false
            result.onSuccess {
                _bannerMessage.value = "Mesa abierta en ${campusZone.shortName} • Cupos en vivo activados"
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo abrir la mesa de estudio"
            }
        }
    }

    fun toggleJoinTable(table: StudyTable) {
        val profile = currentProfileOrDefault()
        val myName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Tú (FESC)" } }
        viewModelScope.launch {
            val result = repository.toggleJoinStudyTable(table, myName)
            result.onSuccess { joined ->
                _bannerMessage.value = if (joined) {
                    "Te uniste a la mesa de ${table.subject} en ${CampusZone.fromId(table.campusZone).shortName}"
                } else {
                    "Liberaste tu cupo en la mesa de ${table.subject}"
                }
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo actualizar el cupo"
            }
        }
    }

    fun removeStudyTable(tableId: String) {
        viewModelScope.launch {
            repository.deleteStudyTable(tableId).onSuccess {
                _bannerMessage.value = "Mesa de estudio finalizada"
            }
        }
    }

    fun launchSosAlert(
        subject: String,
        questionTitle: String,
        questionDetail: String,
        examUrgency: String,
        campusZone: CampusZone,
        spotReference: String
    ) {
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createSosAlert(
                subject = subject,
                questionTitle = questionTitle,
                questionDetail = questionDetail,
                examUrgency = examUrgency,
                campusZone = campusZone.id,
                spotReference = spotReference,
                authorName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Estudiante FESC" } },
                authorProgram = "${profile.program} • ${profile.semester}° Sem"
            )
            _isBusy.value = false
            result.onSuccess {
                _bannerMessage.value = "¡Alerta SOS emitida! Los compañeros en ${campusZone.shortName} ya pueden verla."
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo emitir la alerta SOS"
            }
        }
    }

    fun respondInPersonToSos(alert: SosAlert, customNote: String? = null) {
        val profile = currentProfileOrDefault()
        val helperName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Mentor FESC" } }
        viewModelScope.launch {
            val result = repository.offerHelpOnSos(
                alert = alert,
                helperName = helperName,
                helperProgram = "${profile.program} • ${profile.semester}° Sem",
                customMessage = customNote
            )
            result.onSuccess {
                repository.saveOrUpdateProfile(
                    displayName = helperName,
                    program = profile.program,
                    semester = profile.semester.toInt(),
                    currentZone = alert.campusZone,
                    statusMode = StudentStatusMode.MENTOR.id,
                    enrolledSubjects = profile.enrolledSubjects,
                    helpedPeersCount = profile.helpedPeersCount.toInt() + 1
                )
                _bannerMessage.value = "¡Confirmado! Avisamos a ${alert.authorName} que vas en camino."
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "No se pudo registrar tu asistencia"
            }
        }
    }

    fun toggleResolveSos(alert: SosAlert) {
        viewModelScope.launch {
            repository.toggleSosResolved(alert).onSuccess { resolved ->
                _bannerMessage.value = if (resolved) {
                    "¡Duda SOS marcada como resuelta! Éxitos en el parcial."
                } else {
                    "Alerta SOS reabierta en el radar."
                }
            }
        }
    }

    fun sendThreadMessage(
        parentId: String,
        parentType: String,
        content: String,
        isGoingInPerson: Boolean = false
    ) {
        if (content.isBlank()) return
        val profile = currentProfileOrDefault()
        val senderName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Estudiante FESC" } }
        viewModelScope.launch {
            repository.postCollaborationMessage(
                parentId = parentId,
                parentType = parentType,
                senderName = senderName,
                senderRole = "${profile.program} • ${profile.semester}° Sem",
                content = content,
                isGoingInPerson = isGoingInPerson
            ).onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Error al enviar el mensaje"
            }
        }
    }

    fun seedDefaultCampusScenario() {
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.seedCampusNetworkScenario(
                studentName = profile.displayName.ifBlank { initialDisplayName.ifBlank { "Estudiante FESC" } },
                studentProgram = profile.program
            )
            _isBusy.value = false
            result.onSuccess {
                _bannerMessage.value = "Red FESC Synapse sincronizada con mesas y alertas activas en el campus"
            }.onFailure {
                Log.w(TAG, "Failed to seed campus scenario", it)
            }
        }
    }

    private companion object {
        const val TAG = "SynapseVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
