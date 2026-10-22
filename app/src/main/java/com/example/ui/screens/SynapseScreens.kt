package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CampusZone
import com.example.data.model.FESC_COMMON_SUBJECTS
import com.example.data.model.SosAlert
import com.example.data.model.StudentProfile
import com.example.data.model.StudentStatusMode
import com.example.data.model.StudyTable
import com.example.ui.components.InteractiveCampusRadarCard
import com.example.ui.theme.SynapseAmber
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralContainer
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseElevatedSurface
import com.example.ui.theme.SynapseEmerald
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.SynapseViolet
import com.example.ui.theme.SynapseVioletLight

@Composable
fun CampusRadarScreen(
    profile: StudentProfile?,
    studyTables: List<StudyTable>,
    sosAlerts: List<SosAlert>,
    selectedZone: CampusZone?,
    selectedSubject: String?,
    onSelectZone: (CampusZone?) -> Unit,
    onSelectSubject: (String?) -> Unit,
    onCheckInZone: (CampusZone) -> Unit,
    onOpenCreateTable: (CampusZone) -> Unit,
    onOpenLaunchSos: (CampusZone) -> Unit,
    onToggleJoinTable: (StudyTable) -> Unit,
    onOpenTableThread: (StudyTable) -> Unit,
    onOfferSosHelp: (SosAlert) -> Unit,
    onOpenSosThread: (SosAlert) -> Unit,
    onToggleResolveSos: (SosAlert) -> Unit,
    onOpenProfileModal: () -> Unit,
    onSeedDemo: () -> Unit
) {
    val currentUserZone = CampusZone.fromId(profile?.currentZone ?: CampusZone.LIBRARY.id)
    val currentStatusMode = StudentStatusMode.fromId(profile?.statusMode ?: StudentStatusMode.STUDYING.id)

    val filteredTables = remember(studyTables, selectedZone, selectedSubject) {
        studyTables.filter { table ->
            (selectedZone == null || table.campusZone == selectedZone.id) &&
                (selectedSubject == null || table.subject.equals(selectedSubject, ignoreCase = true))
        }
    }

    val filteredSos = remember(sosAlerts, selectedZone, selectedSubject) {
        sosAlerts.filter { alert ->
            !alert.isResolved &&
                (selectedZone == null || alert.campusZone == selectedZone.id) &&
                (selectedSubject == null || alert.subject.equals(selectedSubject, ignoreCase = true))
        }
    }

    val allSubjects = remember(studyTables, sosAlerts) {
        (studyTables.map { it.subject } + sosAlerts.map { it.subject } + FESC_COMMON_SUBJECTS.take(4))
            .filter { it.isNotBlank() }
            .distinct()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("campus_radar_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Student Live Status Banner
        item {
            StudentLiveStatusBanner(
                profile = profile,
                currentZone = currentUserZone,
                statusMode = currentStatusMode,
                activeTablesCount = studyTables.count { it.isActive },
                activeSosCount = sosAlerts.count { !it.isResolved },
                onOpenProfileModal = onOpenProfileModal
            )
        }

        // 2. Subject Filter Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedSubject == null,
                    onClick = { onSelectSubject(null) },
                    label = { Text("Todas las materias") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SynapseViolet.copy(alpha = 0.22f),
                        selectedLabelColor = SynapseVioletLight
                    )
                )
                allSubjects.forEach { subject ->
                    val isSelected = selectedSubject == subject
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectSubject(subject) },
                        label = { Text(subject) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SynapseViolet.copy(alpha = 0.24f),
                            selectedLabelColor = SynapseVioletLight
                        )
                    )
                }
            }
        }

        // 3. Interactive Campus Map Card
        item {
            InteractiveCampusRadarCard(
                selectedZone = selectedZone,
                currentUserZone = currentUserZone,
                studyTables = studyTables,
                sosAlerts = sosAlerts,
                onZoneSelected = onSelectZone,
                onCheckInZone = onCheckInZone,
                onOpenTableInZone = onOpenCreateTable,
                onLaunchSosInZone = onOpenLaunchSos
            )
        }

        // 4. Urgent SOS Alerts in Focus
        if (filteredSos.isNotEmpty()) {
            item {
                SectionHeaderRow(
                    title = if (selectedZone != null) {
                        "Alertas SOS en ${selectedZone.shortName} (${filteredSos.size})"
                    } else {
                        "Alertas SOS Urgentes en el Campus (${filteredSos.size})"
                    },
                    subtitle = "Compañeros que necesitan explicación antes de su parcial",
                    accentColor = SynapseCoralSos
                )
            }
            items(filteredSos, key = { "radar_sos_${it.id}" }) { alert ->
                SosAlertCard(
                    alert = alert,
                    onOfferHelp = { onOfferSosHelp(alert) },
                    onOpenThread = { onOpenSosThread(alert) },
                    onToggleResolve = { onToggleResolveSos(alert) }
                )
            }
        }

        // 5. Open Study Tables in Focus
        item {
            SectionHeaderRow(
                title = if (selectedZone != null) {
                    "Mesas Abiertas en ${selectedZone.shortName} (${filteredTables.size})"
                } else {
                    "Mesas de Estudio con Cupos en Vivo (${filteredTables.size})"
                },
                subtitle = "Únete a estudiar la misma materia o reserva tu silla",
                accentColor = SynapseCyan
            )
        }

        if (filteredTables.isEmpty()) {
            item {
                EmptyCampusStateCard(
                    title = "No hay mesas para este filtro ahora mismo",
                    subtitle = "Sé el primero en abrir una mesa de estudio en esta zona o recarga las sesiones activas del campus.",
                    primaryActionLabel = "Abrir Mesa de Estudio",
                    onPrimaryAction = { onOpenCreateTable(selectedZone ?: currentUserZone) },
                    secondaryActionLabel = "Sincronizar Semana de Parciales",
                    onSecondaryAction = onSeedDemo
                )
            }
        } else {
            items(filteredTables, key = { "radar_table_${it.id}" }) { table ->
                StudyTableCard(
                    table = table,
                    onToggleJoin = { onToggleJoinTable(table) },
                    onOpenThread = { onOpenTableThread(table) }
                )
            }
        }
    }
}

@Composable
fun StudyTablesScreen(
    studyTables: List<StudyTable>,
    selectedZone: CampusZone?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectZone: (CampusZone?) -> Unit,
    onOpenCreateTable: () -> Unit,
    onToggleJoinTable: (StudyTable) -> Unit,
    onOpenTableThread: (StudyTable) -> Unit,
    onSeedDemo: () -> Unit
) {
    val filteredTables = remember(studyTables, selectedZone, searchQuery) {
        studyTables.filter { table ->
            val matchesZone = selectedZone == null || table.campusZone == selectedZone.id
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                table.subject.contains(q, ignoreCase = true) ||
                table.topic.contains(q, ignoreCase = true) ||
                table.hostName.contains(q, ignoreCase = true) ||
                table.spotReference.contains(q, ignoreCase = true)
            matchesZone && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_tables_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
                border = BorderStroke(1.dp, SynapseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mesas de Estudio en Vivo",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Encuentra quién está repasando tu materia y ocupa un asiento disponible.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SynapseTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onOpenCreateTable,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SynapseCyan,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("create_study_table_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nueva Mesa", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_tables_input"),
                        placeholder = { Text("Buscar por materia, tema, bloque o compañero...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedZone == null,
                            onClick = { onSelectZone(null) },
                            label = { Text("Todo el Campus (${studyTables.size})") }
                        )
                        CampusZone.entries.forEach { zone ->
                            val count = studyTables.count { it.campusZone == zone.id }
                            FilterChip(
                                selected = selectedZone == zone,
                                onClick = { onSelectZone(if (selectedZone == zone) null else zone) },
                                label = { Text("${zone.shortName} ($count)") }
                            )
                        }
                    }
                }
            }
        }

        if (filteredTables.isEmpty()) {
            item {
                EmptyCampusStateCard(
                    title = "Sin mesas activas para tu búsqueda",
                    subtitle = "Abre tu propia mesa en Biblioteca, Cubículos o Plazoleta para que otros compañeros se sumen.",
                    primaryActionLabel = "Abrir Mesa de Estudio",
                    onPrimaryAction = onOpenCreateTable,
                    secondaryActionLabel = "Recargar Mesas FESC",
                    onSecondaryAction = onSeedDemo
                )
            }
        } else {
            items(filteredTables, key = { "table_item_${it.id}" }) { table ->
                StudyTableCard(
                    table = table,
                    onToggleJoin = { onToggleJoinTable(table) },
                    onOpenThread = { onOpenTableThread(table) }
                )
            }
        }
    }
}

@Composable
fun SosRadarScreen(
    sosAlerts: List<SosAlert>,
    onOpenLaunchSos: () -> Unit,
    onOfferHelp: (SosAlert) -> Unit,
    onOpenSosThread: (SosAlert) -> Unit,
    onToggleResolve: (SosAlert) -> Unit,
    onSeedDemo: () -> Unit
) {
    var showResolved by remember { mutableStateOf(false) }

    val displayedAlerts = remember(sosAlerts, showResolved) {
        sosAlerts.filter { if (showResolved) it.isResolved else !it.isResolved }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sos_alerts_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
                border = BorderStroke(1.dp, SynapseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = SynapseCoralSos,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Radar SOS de Parciales",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¿Trancado en un ejercicio horas antes del examen? Emite una alerta SOS con tu mesa exacta o gana reputación ayudando a un compañero.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SynapseTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !showResolved,
                                onClick = { showResolved = false },
                                label = {
                                    Text("Urgentes (${sosAlerts.count { !it.isResolved }})")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SynapseCoralSos.copy(alpha = 0.25f),
                                    selectedLabelColor = SynapseCoralSos
                                )
                            )
                            FilterChip(
                                selected = showResolved,
                                onClick = { showResolved = true },
                                label = {
                                    Text("Resueltas (${sosAlerts.count { it.isResolved }})")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SynapseEmerald.copy(alpha = 0.25f),
                                    selectedLabelColor = SynapseEmerald
                                )
                            )
                        }

                        Button(
                            onClick = onOpenLaunchSos,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SynapseCoralSos,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("launch_sos_primary_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lanzar SOS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (displayedAlerts.isEmpty()) {
            item {
                EmptyCampusStateCard(
                    title = if (showResolved) "Aún no hay alertas SOS marcadas como resueltas" else "¡Todo despejado! No hay alertas SOS pendientes",
                    subtitle = "Si tienes una duda urgente antes de tu examen parcial, lanza una alerta SOS indicando en qué mesa estás.",
                    primaryActionLabel = "Lanzar Alerta SOS Ahora",
                    onPrimaryAction = onOpenLaunchSos,
                    secondaryActionLabel = "Cargar Alertas de Ejemplo",
                    onSecondaryAction = onSeedDemo
                )
            }
        } else {
            items(displayedAlerts, key = { "sos_item_${it.id}" }) { alert ->
                SosAlertCard(
                    alert = alert,
                    onOfferHelp = { onOfferHelp(alert) },
                    onOpenThread = { onOpenSosThread(alert) },
                    onToggleResolve = { onToggleResolve(alert) }
                )
            }
        }
    }
}

@Composable
private fun StudentLiveStatusBanner(
    profile: StudentProfile?,
    currentZone: CampusZone,
    statusMode: StudentStatusMode,
    activeTablesCount: Int,
    activeSosCount: Int,
    onOpenProfileModal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProfileModal() }
            .testTag("student_status_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
        border = BorderStroke(1.dp, SynapseBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SynapseCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = SynapseCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile?.displayName?.ifBlank { "Estudiante FESC" } ?: "Estudiante FESC",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profile?.program ?: "Ingeniería de Software"} • ${profile?.semester ?: 5}° Semestre",
                            style = MaterialTheme.typography.bodySmall,
                            color = SynapseTextSecondary
                        )
                    }
                }

                Surface(
                    color = SynapseViolet.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SynapseViolet.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusMode.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = SynapseVioletLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Tu Zona",
                    value = currentZone.shortName,
                    accent = SynapseEmerald
                )
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Mesas Activas",
                    value = "$activeTablesCount grupos",
                    accent = SynapseCyan
                )
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Alertas SOS",
                    value = "$activeSosCount urgentes",
                    accent = SynapseCoralSos
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = SynapseElevatedSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SynapseTextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyTableCard(
    table: StudyTable,
    onToggleJoin: () -> Unit,
    onOpenThread: () -> Unit
) {
    val zone = CampusZone.fromId(table.campusZone)
    val seatFraction = (table.occupiedSeats.toFloat() / table.maxSeats.coerceAtLeast(1L).toFloat())
        .coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_table_card_${table.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (table.isJoinedByCurrentUser) SynapseCyan else SynapseBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SynapseCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = zone.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                color = SynapseCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = SynapseViolet.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = table.studyVibe,
                                style = MaterialTheme.typography.labelSmall,
                                color = SynapseVioletLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = table.subject,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Live Seats Badge
                Surface(
                    color = if (table.isFull && !table.isJoinedByCurrentUser) {
                        SynapseAmber.copy(alpha = 0.18f)
                    } else {
                        SynapseEmerald.copy(alpha = 0.18f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (table.isFull && !table.isJoinedByCurrentUser) SynapseAmber else SynapseEmerald
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            tint = if (table.isFull && !table.isJoinedByCurrentUser) SynapseAmber else SynapseEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${table.occupiedSeats}/${table.maxSeats} cupos",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (table.isFull && !table.isJoinedByCurrentUser) SynapseAmber else SynapseEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = table.topic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SynapseCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = table.spotReference,
                    style = MaterialTheme.typography.bodySmall,
                    color = SynapseCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seat Progress Indicator
            LinearProgressIndicator(
                progress = { seatFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (table.isJoinedByCurrentUser) SynapseCyan else SynapseEmerald,
                trackColor = SynapseElevatedSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Participants chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                table.participantNames.forEach { name ->
                    Surface(
                        color = SynapseElevatedSurface,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SynapseTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenThread,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("table_chat_button_${table.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SynapseBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat de Mesa")
                }

                Button(
                    onClick = onToggleJoin,
                    enabled = !table.isFull || table.isJoinedByCurrentUser,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("join_table_button_${table.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (table.isJoinedByCurrentUser) SynapseEmerald else SynapseCyan,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (table.isJoinedByCurrentUser) Icons.Default.CheckCircle else Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            table.isJoinedByCurrentUser -> "En la Mesa (Salir)"
                            table.isFull -> "Mesa Llena"
                            else -> "Unirme (${table.availableSeats} libres)"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SosAlertCard(
    alert: SosAlert,
    onOfferHelp: () -> Unit,
    onOpenThread: () -> Unit,
    onToggleResolve: () -> Unit
) {
    val zone = CampusZone.fromId(alert.campusZone)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sos_alert_card_${alert.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SynapseCardSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (alert.isResolved) SynapseBorder else SynapseCyan
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (alert.isResolved) {
                            SynapseEmerald.copy(alpha = 0.2f)
                        } else {
                            SynapseCoralSos.copy(alpha = 0.22f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (alert.isResolved) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (alert.isResolved) SynapseEmerald else SynapseCoralSos,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (alert.isResolved) "RESUELTA" else alert.examUrgency.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (alert.isResolved) SynapseEmerald else SynapseCoralSos,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = SynapseElevatedSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = zone.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = SynapseCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = alert.subject,
                    style = MaterialTheme.typography.labelMedium,
                    color = SynapseVioletLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.questionTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.questionDetail,
                style = MaterialTheme.typography.bodyMedium,
                color = SynapseTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SynapseCoralSos,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${alert.spotReference} • Publicado por ${alert.authorName} (${alert.authorProgram})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (alert.helperNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SynapseEmerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = SynapseEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compañeros apoyando: ${alert.helperNames.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SynapseEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!alert.isResolved) {
                    Button(
                        onClick = onOfferHelp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SynapseCoralSos,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("help_sos_button_${alert.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (alert.isCurrentUserHelping) "¡Ya vas en camino!" else "¡Voy a explicarte!",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenThread,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SynapseCyan),
                    modifier = Modifier.testTag("sos_thread_button_${alert.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = SynapseCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Explicar / Chat (${alert.responsesCount})",
                        color = SynapseCyan
                    )
                }

                OutlinedButton(
                    onClick = onToggleResolve,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SynapseEmerald),
                    modifier = Modifier.testTag("resolve_sos_button_${alert.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SynapseEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alert.isResolved) "Reabrir" else "Marcar Resuelta",
                        color = SynapseEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SynapseTextSecondary,
            modifier = Modifier.padding(start = 18.dp)
        )
    }
}

@Composable
private fun EmptyCampusStateCard(
    title: String,
    subtitle: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    secondaryActionLabel: String,
    onSecondaryAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
        border = BorderStroke(1.dp, SynapseBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SynapseTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(secondaryActionLabel)
                }
                Button(
                    onClick = onPrimaryAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SynapseCyan,
                        contentColor = Color.White
                    )
                ) {
                    Text(primaryActionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
