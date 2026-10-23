package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Global reactive theme state: false = Light Mode (high-contrast white surfaces), true = Dark Mode (high-contrast charcoal with bright text)
var isSynapseDarkMode by mutableStateOf(false)

// Global reactive accessibility state: Okabe-Ito universal colorblind-safe palette (Protanopia, Deuteranopia, Tritanopia)
var isSynapseColorblindMode by mutableStateOf(false)

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

// Primary Brand Accent — FESC Red Primary (Standard) or Okabe-Ito Cobalt Blue (Colorblind Mode)
val SynapseCyan: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF3898EC)
        isSynapseColorblindMode -> Color(0xFF0072B2)
        else -> Color(0xFFE30513)
    }

val SynapseCyanDim: Color
    get() = when {
        isSynapseColorblindMode -> Color(0xFF005587)
        else -> Color(0xFF9C0F06)
    }

val SynapseCyanContainer: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF102D47)
        isSynapseColorblindMode -> Color(0xFFE6F3FA)
        isSynapseDarkMode -> Color(0xFF4A1215)
        else -> Color(0xFFFDECEC)
    }

// Secondary Institutional Wine (Standard) or Okabe-Ito Indigo Purple (Colorblind Mode)
val SynapseViolet: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF7B68EE)
        isSynapseColorblindMode -> Color(0xFF332288)
        else -> Color(0xFF9C0F06)
    }

val SynapseVioletLight: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFFC5B8FF)
        isSynapseColorblindMode -> Color(0xFF332288)
        isSynapseDarkMode -> Color(0xFFFFB4AB)
        else -> Color(0xFF9C0F06)
    }

val SynapseVioletContainer: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF231E42)
        isSynapseColorblindMode -> Color(0xFFECEAF6)
        isSynapseDarkMode -> Color(0xFF3D1917)
        else -> Color(0xFFFAE6E5)
    }

// Urgent SOS / Digital Alert — FESC Red (Standard) or Okabe-Ito Vermilion Orange (Colorblind Mode, distinct from Blue/Teal)
val SynapseCoralSos: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFFFF8533)
        isSynapseColorblindMode -> Color(0xFFD55E00)
        else -> Color(0xFFD32F2F)
    }

val SynapseCoralContainer: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF42230D)
        isSynapseColorblindMode -> Color(0xFFFFF0E5)
        isSynapseDarkMode -> Color(0xFF3F1717)
        else -> Color(0xFFFCE8E8)
    }

// Semantic States (Resolved / Available Seats & Warning)
// In Colorblind mode, uses Okabe-Ito Bluish Teal (#00866B / #26C6DA) to prevent Red-Green confusion
val SynapseEmerald: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF26C6DA)
        isSynapseColorblindMode -> Color(0xFF00796B)
        isSynapseDarkMode -> Color(0xFF4CAF50)
        else -> Color(0xFF2E7D32)
    }

val SynapseEmeraldContainer: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF0E353A)
        isSynapseColorblindMode -> Color(0xFFE0F2F1)
        isSynapseDarkMode -> Color(0xFF1B331E)
        else -> Color(0xFFE8F5E9)
    }

val SynapseAmber: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFFFFCA28)
        isSynapseColorblindMode -> Color(0xFF9E6B00)
        isSynapseDarkMode -> Color(0xFFFF9800)
        else -> Color(0xFFE65100)
    }

val SynapseAmberContainer: Color
    get() = when {
        isSynapseColorblindMode && isSynapseDarkMode -> Color(0xFF3B2E08)
        isSynapseColorblindMode -> Color(0xFFFFF8E1)
        isSynapseDarkMode -> Color(0xFF38240D)
        else -> Color(0xFFFFF3E0)
    }

// High-Contrast Typography Hierarchy for comfortable reading
val SynapseTextPrimary: Color
    get() = if (isSynapseDarkMode) Color(0xFFFFFFFF) else Color(0xFF1C1C1C)

val SynapseTextSecondary: Color
    get() = if (isSynapseDarkMode) Color(0xFFDADDE2) else Color(0xFF45474A)

val SynapseTextMuted: Color
    get() = if (isSynapseDarkMode) Color(0xFFA6ABB4) else Color(0xFF6E7075)
