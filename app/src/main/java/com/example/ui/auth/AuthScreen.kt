package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseElevatedSurface
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseTextMuted
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.isSynapseDarkMode
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val FALLBACK_WEB_CLIENT_ID =
    "1080111455648-leuh5pn4od6ogmgq0l5ggk3e44orrefr.apps.googleusercontent.com"

fun safeFirebaseAuth(context: Context? = null): FirebaseAuth? = runCatching {
    if (context != null && FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
    }
    FirebaseAuth.getInstance()
}.getOrNull()

private fun resolveWebClientId(context: Context): String {
    val resId = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    return if (resId != 0) {
        runCatching { context.getString(resId) }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: FALLBACK_WEB_CLIENT_ID
    } else {
        FALLBACK_WEB_CLIENT_ID
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthFallbackToLocalCampus: (String) -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope
) {
    val clientId = resolveWebClientId(context)
    if (clientId.isBlank()) {
        onAuthFallbackToLocalCampus("Estudiante FESC")
        return
    }

    val activityContext = context.findActivity() ?: context
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val primaryRequest = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = try {
                credentialManager.getCredential(activityContext, primaryRequest)
            } catch (primaryError: Exception) {
                Log.w("Auth", "Primary GetSignInWithGoogleOption failed, trying GetGoogleIdOption", primaryError)
                val fallbackGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(fallbackGoogleIdOption)
                    .build()
                credentialManager.getCredential(activityContext, fallbackRequest)
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                val displayName = googleIdTokenCredential.displayName
                    ?: googleIdTokenCredential.id.substringBefore("@").ifBlank { "Estudiante FESC" }
                val auth = safeFirebaseAuth(context)
                if (auth != null) {
                    try {
                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        auth.signInWithCredential(authCredential).await()
                        onAuthSuccess()
                    } catch (firebaseEx: Exception) {
                        Log.w("Auth", "Firebase signInWithCredential failed, activating local campus session", firebaseEx)
                        onAuthFallbackToLocalCampus(displayName)
                    }
                } else {
                    onAuthFallbackToLocalCampus(displayName)
                }
            } else {
                onAuthFallbackToLocalCampus("Estudiante FESC")
            }
        } catch (e: Exception) {
            Log.w(
                "Auth",
                "Google CredentialManager did not complete OAuth token exchange (${e.javaClass.simpleName}: ${e.message}). Entering local campus session.",
                e
            )
            onAuthFallbackToLocalCampus("Estudiante FESC")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    safeFirebaseAuth(context)?.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onEnterLocalCampus: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (safeFirebaseAuth(context)?.currentUser != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SynapseObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Light / Dark Mode Toggle Pill in Top-Right
        Surface(
            onClick = { isSynapseDarkMode = !isSynapseDarkMode },
            color = SynapseCardSurface,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, SynapseBorder),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("auth_theme_toggle_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSynapseDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Cambiar tema claro u oscuro",
                    tint = SynapseCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSynapseDarkMode) "Modo Claro" else "Modo Oscuro",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // High-Contrast Minimalist Card Container
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
            border = BorderStroke(1.dp, SynapseBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Minimalist Institutional Emblem
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SynapseCyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = SynapseCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FESC • COMUNIDAD DE ESTUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = SynapseCyan,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "FESC Synapse",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Conéctate en tiempo real con compañeros que estudian tus mismas materias en el campus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SynapseTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Lightweight Minimalist Feature Pills
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MinimalFeaturePill(
                        icon = Icons.Default.LocationOn,
                        label = "Mapa del Campus"
                    )
                    MinimalFeaturePill(
                        icon = Icons.Default.Groups,
                        label = "Mesas en Vivo"
                    )
                    MinimalFeaturePill(
                        icon = Icons.Default.Bolt,
                        label = "Alertas SOS"
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        color = SynapseElevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SynapseCoralSos.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthFallbackToLocalCampus = { displayName ->
                                isLoading = false
                                onEnterLocalCampus(displayName)
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = coroutineScope
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SynapseCyan,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Continuar con Google",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Usa tu cuenta institucional o personal de Google",
                    style = MaterialTheme.typography.bodySmall,
                    color = SynapseTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MinimalFeaturePill(
    icon: ImageVector,
    label: String
) {
    Surface(
        color = SynapseElevatedSurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, SynapseBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SynapseCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
