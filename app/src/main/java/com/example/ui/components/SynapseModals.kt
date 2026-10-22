package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CampusZone
import com.example.data.model.CollaborationMessage
import com.example.data.model.FESC_COMMON_SUBJECTS
import com.example.data.model.FESC_PROGRAMS
import com.example.data.model.StudentProfile
import com.example.data.model.StudentStatusMode
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseElevatedSurface
import com.example.ui.theme.SynapseEmerald
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.SynapseViolet
import com.example.ui.theme.SynapseVioletLight
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateStudyTableDialog(
    initialZone: CampusZone,
    onDismiss: () -> Unit,
    onCreateTable: (
        subject: String,
        topic: String,
        campusZone: CampusZone,
        spotReference: String,
        studyVibe: String,
        maxSeats: Int
    ) -> Unit
) {
    BackHandler { onDismiss() }

    var subject by remember { mutableStateOf(FESC_COMMON_SUBJECTS.first()) }
    var topic by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf(initialZone) }
    var spotReference by remember { mutableStateOf("Mesa 3 junto al ventanal") }
    var studyVibe by remember { mutableStateOf("Resolución de taller") }
    var maxSeats by remember { mutableIntStateOf(5) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val vibes = listOf("Resolución de taller", "Simulacro de parcial", "Práctica en código", "Debate de teoría")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_table_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
            border = BorderStroke(1.dp, SynapseBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = SynapseCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Mesa de Estudio",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Materia:", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FESC_COMMON_SUBJECTS.take(6).forEach { preset ->
                        FilterChip(
                            selected = subject == preset,
                            onClick = { subject = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia de estudio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_subject_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Tema específico o taller del parcial") },
                    placeholder = { Text("Ej: Repaso de integrales por partes y ejercicios 1 al 10") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_topic_input"),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Zona del Campus FESC:", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CampusZone.entries.forEach { zone ->
                        FilterChip(
                            selected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            label = { Text(zone.shortName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = spotReference,
                    onValueChange = { spotReference = it },
                    label = { Text("Referencia exacta para encontrarte") },
                    placeholder = { Text("Ej: Piso 2, Mesa 4 cerca a los enchufes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_spot_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Dinámica de la mesa:", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    vibes.forEach { v ->
                        FilterChip(
                            selected = studyVibe == v,
                            onClick = { studyVibe = v },
                            label = { Text(v, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cupos totales en la mesa: $maxSeats estudiantes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = maxSeats.toFloat(),
                    onValueChange = { maxSeats = it.roundToInt().coerceIn(2, 10) },
                    valueRange = 2f..10f,
                    steps = 7
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SynapseCoralSos,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (subject.isBlank() || topic.isBlank() || spotReference.isBlank()) {
                                validationError = "Completa la materia, el tema del parcial y tu ubicación exacta."
                            } else {
                                onCreateTable(
                                    subject,
                                    topic,
                                    selectedZone,
                                    spotReference,
                                    studyVibe,
                                    maxSeats
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SynapseCyan,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("confirm_create_table_button")
                    ) {
                        Text("Publicar Mesa en Vivo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaunchSosAlertDialog(
    initialZone: CampusZone,
    onDismiss: () -> Unit,
    onLaunchSos: (
        subject: String,
        questionTitle: String,
        questionDetail: String,
        examUrgency: String,
        campusZone: CampusZone,
        spotReference: String
    ) -> Unit
) {
    BackHandler { onDismiss() }

    var subject by remember { mutableStateOf(FESC_COMMON_SUBJECTS.first()) }
    var questionTitle by remember { mutableStateOf("") }
    var questionDetail by remember { mutableStateOf("") }
    var examUrgency by remember { mutableStateOf("Parcial en 1 hora") }
    var selectedZone by remember { mutableStateOf(initialZone) }
    var spotReference by remember { mutableStateOf("Biblioteca Piso 2 • Mesa 6") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val urgencyOptions = listOf("Parcial en 1 hora", "Parcial en 2 horas", "Parcial hoy 6:00 PM", "Parcial mañana")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("launch_sos_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
            border = BorderStroke(1.5.dp, SynapseCoralSos)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = SynapseCoralSos)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emitir Alerta SOS de Parcial",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Materia de la duda:", style = MaterialTheme.typography.labelMedium, color = SynapseCoralSos)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FESC_COMMON_SUBJECTS.take(5).forEach { preset ->
                        FilterChip(
                            selected = subject == preset,
                            onClick = { subject = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = questionTitle,
                    onValueChange = { questionTitle = it },
                    label = { Text("¿En qué tema o punto necesitas ayuda urgente?") },
                    placeholder = { Text("Ej: No me da el balance del flujo de caja en el taller") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = questionDetail,
                    onValueChange = { questionDetail = it },
                    label = { Text("Describe tu duda específica") },
                    placeholder = { Text("Explica qué intentaste hacer y en qué paso te bloqueaste...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_detail_input"),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Nivel de urgencia del parcial:", style = MaterialTheme.typography.labelMedium, color = SynapseCoralSos)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    urgencyOptions.forEach { option ->
                        FilterChip(
                            selected = examUrgency == option,
                            onClick = { examUrgency = option },
                            label = { Text(option, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SynapseCoralSos.copy(alpha = 0.24f),
                                selectedLabelColor = SynapseCoralSos
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("¿En qué zona del campus estás esperando?", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CampusZone.entries.forEach { zone ->
                        FilterChip(
                            selected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            label = { Text(zone.shortName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = spotReference,
                    onValueChange = { spotReference = it },
                    label = { Text("Mesa exacta o referencia para ubicarte") },
                    placeholder = { Text("Ej: Cubículo 2, tengo chaqueta gris y portátil") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_spot_input"),
                    singleLine = true
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SynapseCoralSos
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (questionTitle.isBlank() || questionDetail.isBlank() || spotReference.isBlank()) {
                                validationError = "Por favor escribe el título de tu duda, el detalle y en qué mesa te encuentras."
                            } else {
                                onLaunchSos(
                                    subject,
                                    questionTitle,
                                    questionDetail,
                                    examUrgency,
                                    selectedZone,
                                    spotReference
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SynapseCoralSos,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("confirm_launch_sos_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Emitir Señal SOS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CollaborationThreadDialog(
    title: String,
    subtitle: String,
    spotReference: String,
    parentType: String,
    messages: List<CollaborationMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (content: String, isGoingInPerson: Boolean) -> Unit
) {
    BackHandler { onDismiss() }

    var draftText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("collaboration_thread_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
            border = BorderStroke(1.dp, if (parentType == "SOS") SynapseCoralSos else SynapseCyan)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (parentType == "SOS") "HILO DE ASISTENCIA SOS" else "COORDINACIÓN DE MESA",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (parentType == "SOS") SynapseCoralSos else SynapseCyan
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$subtitle • $spotReference",
                            style = MaterialTheme.typography.bodySmall,
                            color = SynapseTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar chat")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = SynapseBorder
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Surface(
                                color = SynapseElevatedSurface,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Aún no hay mensajes en este hilo. Escribe una explicación rápida o avisa que vas en camino a la mesa.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SynapseTextSecondary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        items(messages, key = { it.id }) { msg ->
                            Surface(
                                color = if (msg.isGoingInPerson) {
                                    SynapseEmerald.copy(alpha = 0.14f)
                                } else {
                                    SynapseElevatedSurface
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (msg.isGoingInPerson) SynapseEmerald.copy(alpha = 0.5f) else SynapseBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (msg.isGoingInPerson) SynapseEmerald else SynapseCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = msg.senderRole,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SynapseTextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick In-Person Assist Button
                OutlinedButton(
                    onClick = {
                        onSendMessage(
                            "¡Voy camino a $spotReference para estudiarlo juntos en persona!",
                            true
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_in_person_message_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SynapseEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = SynapseEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Avisar: \"¡Voy camino a tu mesa en persona!\"", color = SynapseEmerald)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        placeholder = { Text("Escribe una explicación o mensaje...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("thread_message_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (draftText.isNotBlank()) {
                                onSendMessage(draftText.trim(), false)
                                draftText = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SynapseCyan,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("send_thread_message_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentProfileDialog(
    profile: StudentProfile?,
    onDismiss: () -> Unit,
    onSaveProfile: (
        displayName: String,
        program: String,
        semester: Int,
        currentZone: CampusZone,
        statusMode: StudentStatusMode,
        enrolledSubjects: List<String>
    ) -> Unit,
    onReseedCampusDemo: () -> Unit,
    onSignOut: () -> Unit
) {
    BackHandler { onDismiss() }

    var displayName by remember { mutableStateOf(profile?.displayName ?: "Estudiante FESC") }
    var selectedProgram by remember { mutableStateOf(profile?.program ?: FESC_PROGRAMS.first()) }
    var semester by remember { mutableIntStateOf((profile?.semester ?: 5L).toInt()) }
    var selectedZone by remember {
        mutableStateOf(CampusZone.fromId(profile?.currentZone ?: CampusZone.LIBRARY.id))
    }
    var selectedStatus by remember {
        mutableStateOf(StudentStatusMode.fromId(profile?.statusMode ?: StudentStatusMode.STUDYING.id))
    }
    var selectedSubjects by remember {
        mutableStateOf(
            profile?.enrolledSubjects?.takeIf { it.isNotEmpty() }
                ?: FESC_COMMON_SUBJECTS.take(3)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("student_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
            border = BorderStroke(1.dp, SynapseBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = SynapseCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Perfil Sináptico FESC",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Tu nombre en el campus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Programa Académico FESC:", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FESC_PROGRAMS.forEach { prog ->
                        FilterChip(
                            selected = selectedProgram == prog,
                            onClick = { selectedProgram = prog },
                            label = { Text(prog, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Semestre actual: $semester° Semestre",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = semester.toFloat(),
                    onValueChange = { semester = it.roundToInt().coerceIn(1, 10) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tu Estado en el Campus:", style = MaterialTheme.typography.labelMedium, color = SynapseVioletLight)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudentStatusMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedStatus == mode,
                            onClick = { selectedStatus = mode },
                            label = { Text(mode.badgeText, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SynapseViolet.copy(alpha = 0.24f),
                                selectedLabelColor = SynapseVioletLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Ubicación actual en el campus:", style = MaterialTheme.typography.labelMedium, color = SynapseEmerald)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CampusZone.entries.forEach { zone ->
                        FilterChip(
                            selected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            label = { Text(zone.shortName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Materias que estás preparando:", style = MaterialTheme.typography.labelMedium, color = SynapseCyan)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FESC_COMMON_SUBJECTS.forEach { subj ->
                        val isPicked = subj in selectedSubjects
                        FilterChip(
                            selected = isPicked,
                            onClick = {
                                selectedSubjects = if (isPicked && selectedSubjects.size > 1) {
                                    selectedSubjects - subj
                                } else {
                                    (selectedSubjects + subj).distinct().take(6)
                                }
                            },
                            label = { Text(subj, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = SynapseBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            onReseedCampusDemo()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recargar Campus Demo")
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SynapseCoralSos),
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = SynapseCoralSos,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salir", color = SynapseCoralSos)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onSaveProfile(
                            displayName,
                            selectedProgram,
                            semester,
                            selectedZone,
                            selectedStatus,
                            selectedSubjects
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SynapseCyan,
                        contentColor = Color.White
                    )
                ) {
                    Text("Guardar Perfil y Estado en Vivo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
