package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.CampusZone
import com.example.data.model.SosAlert
import com.example.data.model.StudyTable
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseElevatedSurface
import com.example.ui.theme.SynapseEmerald
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.SynapseViolet
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveCampusRadarCard(
    selectedZone: CampusZone?,
    currentUserZone: CampusZone,
    studyTables: List<StudyTable>,
    sosAlerts: List<SosAlert>,
    onZoneSelected: (CampusZone?) -> Unit,
    onCheckInZone: (CampusZone) -> Unit,
    onOpenTableInZone: (CampusZone) -> Unit,
    onLaunchSosInZone: (CampusZone) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campus_radar_card"),
        shape = RoundedCornerShape(24.dp),
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
                        text = "MAPA SINÁPTICO DEL CAMPUS FESC",
                        style = MaterialTheme.typography.labelMedium,
                        color = SynapseCyan
                    )
                    Text(
                        text = "Toca un bloque para explorar mesas y alertas SOS",
                        style = MaterialTheme.typography.bodySmall,
                        color = SynapseTextSecondary
                    )
                }
                if (selectedZone != null) {
                    FilterChip(
                        selected = true,
                        onClick = { onZoneSelected(null) },
                        label = { Text("Ver Todo el Campus") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SynapseCyan.copy(alpha = 0.18f),
                            selectedLabelColor = SynapseCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Campus Canvas + Positioned Zone Nodes
            val mapGradientColors = if (com.example.ui.theme.isSynapseDarkMode) {
                listOf(Color(0xFF2B2E36), Color(0xFF1B1D22))
            } else {
                listOf(Color(0xFFFFFFFF), Color(0xFFEBEEF3))
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(colors = mapGradientColors)
                    )
                    .border(1.dp, SynapseBorder, RoundedCornerShape(20.dp))
            ) {
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Architectural blueprint grid lines
                    val gridStepX = w / 8f
                    val gridStepY = h / 6f
                    for (i in 1..7) {
                        drawLine(
                            color = SynapseCyan.copy(alpha = 0.07f),
                            start = Offset(gridStepX * i, 0f),
                            end = Offset(gridStepX * i, h),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    for (j in 1..5) {
                        drawLine(
                            color = SynapseCyan.copy(alpha = 0.07f),
                            start = Offset(0f, gridStepY * j),
                            end = Offset(w, gridStepY * j),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Radar concentric rings around central Library
                    val centerOffset = Offset(w * 0.50f, h * 0.45f)
                    drawCircle(
                        color = SynapseViolet.copy(alpha = 0.08f),
                        radius = w * 0.22f,
                        center = centerOffset,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = SynapseCyan.copy(alpha = 0.08f),
                        radius = w * 0.38f,
                        center = centerOffset,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Neural synapse pathways between campus zones
                    val connections = listOf(
                        CampusZone.LIBRARY to CampusZone.CUBICLES,
                        CampusZone.LIBRARY to CampusZone.LABS,
                        CampusZone.LIBRARY to CampusZone.PLAZA,
                        CampusZone.LIBRARY to CampusZone.CAFETERIA,
                        CampusZone.CUBICLES to CampusZone.PLAZA,
                        CampusZone.LABS to CampusZone.CAFETERIA,
                        CampusZone.PLAZA to CampusZone.CAFETERIA
                    )
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    for ((fromZone, toZone) in connections) {
                        val isHighlighted = selectedZone == null ||
                            selectedZone == fromZone ||
                            selectedZone == toZone
                        val start = Offset(w * fromZone.mapX, h * fromZone.mapY)
                        val end = Offset(w * toZone.mapX, h * toZone.mapY)
                        drawLine(
                            color = if (isHighlighted) {
                                SynapseCyan.copy(alpha = 0.42f)
                            } else {
                                SynapseBorder.copy(alpha = 0.3f)
                            },
                            start = start,
                            end = end,
                            strokeWidth = if (isHighlighted) 2.2.dp.toPx() else 1.2.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    // Animated pulse rings for each zone
                    for (zone in CampusZone.entries) {
                        val zoneCenter = Offset(w * zone.mapX, h * zone.mapY)
                        val activeSosCount = sosAlerts.count { it.campusZone == zone.id && !it.isResolved }
                        val activeTableCount = studyTables.count { it.campusZone == zone.id && it.isActive }

                        if (activeSosCount > 0) {
                            drawCircle(
                                color = SynapseCoralSos.copy(alpha = pulseAlpha),
                                radius = (22.dp.toPx() + (34.dp.toPx() * pulseRadiusFraction)),
                                center = zoneCenter,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        } else if (activeTableCount > 0) {
                            drawCircle(
                                color = SynapseCyan.copy(alpha = pulseAlpha * 0.7f),
                                radius = (20.dp.toPx() + (26.dp.toPx() * pulseRadiusFraction)),
                                center = zoneCenter,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                    }
                }

                // Tappable Zone Node Widgets
                CampusZone.entries.forEach { zone ->
                    val zoneTables = studyTables.count { it.campusZone == zone.id && it.isActive }
                    val zoneSos = sosAlerts.count { it.campusZone == zone.id && !it.isResolved }
                    val isSelected = selectedZone == zone
                    val isUserHere = currentUserZone == zone

                    val nodeWidthDp = 112.dp
                    val nodeHeightDp = 58.dp

                    Box(
                        modifier = Modifier
                            .offset {
                                val x = (widthPx * zone.mapX - (nodeWidthDp.toPx() / 2f))
                                    .coerceIn(6f, (widthPx - nodeWidthDp.toPx() - 6f).coerceAtLeast(6f))
                                val y = (heightPx * zone.mapY - (nodeHeightDp.toPx() / 2f))
                                    .coerceIn(6f, (heightPx - nodeHeightDp.toPx() - 6f).coerceAtLeast(6f))
                                IntOffset(x.roundToInt(), y.roundToInt())
                            }
                            .width(nodeWidthDp)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isSelected -> SynapseElevatedSurface
                                    zoneSos > 0 -> if (com.example.ui.theme.isSynapseDarkMode) {
                                        Color(0xFF3A1D1D)
                                    } else {
                                        Color(0xFFFFF5F5)
                                    }
                                    else -> SynapseCardSurface
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = when {
                                    isSelected -> SynapseCyan
                                    zoneSos > 0 -> SynapseCoralSos
                                    isUserHere -> SynapseEmerald
                                    else -> SynapseBorder
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onZoneSelected(if (isSelected) null else zone)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("campus_zone_${zone.id.lowercase()}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isUserHere) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(SynapseEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = zone.shortName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SynapseCyan else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SynapseCyan.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$zoneTables mesas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SynapseCyan,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                if (zoneSos > 0) {
                                    Surface(
                                        color = SynapseCoralSos.copy(alpha = 0.24f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "$zoneSos SOS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SynapseCoralSos,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend + Quick Zone Selector Pills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CampusZone.entries.forEach { zone ->
                    val isSelected = selectedZone == zone
                    FilterChip(
                        selected = isSelected,
                        onClick = { onZoneSelected(if (isSelected) null else zone) },
                        label = { Text(zone.shortName) },
                        leadingIcon = if (currentUserZone == zone) {
                            {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Tu ubicación actual",
                                    tint = SynapseEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SynapseCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SynapseCyan
                        )
                    )
                }
            }

            // Contextual Zone Action Strip
            val activeZone = selectedZone ?: currentUserZone
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = SynapseElevatedSurface.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SynapseBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SynapseCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeZone.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${activeZone.buildingRef} • ${activeZone.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SynapseTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentUserZone != activeZone) {
                            OutlinedButton(
                                onClick = { onCheckInZone(activeZone) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SynapseEmerald),
                                modifier = Modifier.testTag("checkin_zone_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SynapseEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check-in Aquí", color = SynapseEmerald)
                            }
                        } else {
                            Surface(
                                color = SynapseEmerald.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SynapseEmerald.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SynapseEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Estás en esta zona",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SynapseEmerald
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onOpenTableInZone(activeZone) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SynapseCyan,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("open_table_in_zone_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Mesa")
                        }

                        Button(
                            onClick = { onLaunchSosInZone(activeZone) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SynapseCoralSos,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("launch_sos_in_zone_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lanzar SOS")
                        }
                    }
                }
            }
        }
    }
}
