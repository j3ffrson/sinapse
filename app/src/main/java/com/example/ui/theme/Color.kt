package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Global reactive theme state: false = Light Mode (high-contrast white surfaces), true = Dark Mode (high-contrast charcoal with bright text)
var isSynapseDarkMode by mutableStateOf(false)

// Dynamic Backgrounds & Surfaces (FESC Institutional Identity)
val SynapseObsidian: Color
    get() = if (isSynapseDarkMode) Color(0xFF16171A) else Color(0xFFF2F4F7)

val SynapseSurface: Color
    get() = if (isSynapseDarkMode) Color(0xFF1F2126) else Color(0xFFFFFFFF)

val SynapseCardSurface: Color
    get() = if (isSynapseDarkMode) Color(0xFF262930) else Color(0xFFFFFFFF)

val SynapseElevatedSurface: Color
    get() = if (isSynapseDarkMode) Color(0xFF323640) else Color(0xFFF5F6F9)

val SynapseBorder: Color
    get() = if (isSynapseDarkMode) Color(0xFF4C5260) else Color(0xFFDADCE2)

// Primary Brand Accent — FESC Red Primary & Burgundy
val SynapseCyan: Color
    get() = Color(0xFFE30513)

val SynapseCyanDim: Color
    get() = Color(0xFF9C0F06)

val SynapseCyanContainer: Color
    get() = if (isSynapseDarkMode) Color(0xFF4A1215) else Color(0xFFFDECEC)

// Secondary Institutional Wine
val SynapseViolet: Color
    get() = Color(0xFF9C0F06)

val SynapseVioletLight: Color
    get() = if (isSynapseDarkMode) Color(0xFFFFB4AB) else Color(0xFF9C0F06)

val SynapseVioletContainer: Color
    get() = if (isSynapseDarkMode) Color(0xFF3D1917) else Color(0xFFFAE6E5)

// Urgent SOS / Digital Alert Red
val SynapseCoralSos: Color
    get() = Color(0xFFD32F2F)

val SynapseCoralContainer: Color
    get() = if (isSynapseDarkMode) Color(0xFF3F1717) else Color(0xFFFCE8E8)

// Semantic States (Resolved / Mentor & Warning)
val SynapseEmerald: Color
    get() = if (isSynapseDarkMode) Color(0xFF4CAF50) else Color(0xFF2E7D32)

val SynapseEmeraldContainer: Color
    get() = if (isSynapseDarkMode) Color(0xFF1B331E) else Color(0xFFE8F5E9)

val SynapseAmber: Color
    get() = if (isSynapseDarkMode) Color(0xFFFF9800) else Color(0xFFE65100)

val SynapseAmberContainer: Color
    get() = if (isSynapseDarkMode) Color(0xFF38240D) else Color(0xFFFFF3E0)

// High-Contrast Typography Hierarchy for comfortable reading
val SynapseTextPrimary: Color
    get() = if (isSynapseDarkMode) Color(0xFFFFFFFF) else Color(0xFF1C1C1C)

val SynapseTextSecondary: Color
    get() = if (isSynapseDarkMode) Color(0xFFDADDE2) else Color(0xFF45474A)

val SynapseTextMuted: Color
    get() = if (isSynapseDarkMode) Color(0xFFA6ABB4) else Color(0xFF6E7075)
