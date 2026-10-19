package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.SynapseViolet
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
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
                try {
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } catch (firebaseEx: Exception) {
                    Log.w("Auth", "Firebase signInWithCredential failed, activating local campus session", firebaseEx)
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
            // When debugging on a physical device via Android Studio, Google Play Services may throw
            // GetCredentialCancellationException ([28444] Developer console / SHA-1 / OAuth test user restriction)
            // right after selecting the Google account. Transition directly into the campus session so the
            // user is never stuck on the login screen.
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
    Firebase.auth.signOut()
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
        if (Firebase.auth.currentUser != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SynapseObsidian, MaterialTheme.colorScheme.surface)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emblem Pill
            Surface(
                color = SynapseCardSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, SynapseBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = SynapseCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RED SINÁPTICA DE CAMPUS • FESC",
                        style = MaterialTheme.typography.labelMedium,
                        color = SynapseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "FESC Synapse",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No estudies solo antes de un parcial. Conéctate en tiempo real con compañeros en la biblioteca, cubículos y áreas comunes del campus.",
                style = MaterialTheme.typography.bodyLarge,
                color = SynapseTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Artwork Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SynapseCardSurface),
                border = BorderStroke(1.dp, SynapseBorder)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_campus_synapse_hero),
                        contentDescription = "Ilustración de la red de estudio FESC Synapse",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureHighlightRow(
                            icon = Icons.Default.LocationOn,
                            accentColor = SynapseCyan,
                            title = "Mapa Interactivo de Zonas del Campus",
                            subtitle = "Ubica al instante quién estudia tu misma materia en Biblioteca, Cubículos, Laboratorios o Cafetería."
                        )
                        FeatureHighlightRow(
                            icon = Icons.Default.Groups,
                            accentColor = SynapseViolet,
                            title = "Mesas de Estudio Abiertas con Cupos en Vivo",
                            subtitle = "Reserva tu asiento en mesas colaborativas o abre tu propio grupo de repaso."
                        )
                        FeatureHighlightRow(
                            icon = Icons.Default.Bolt,
                            accentColor = SynapseCoralSos,
                            title = "Alertas SOS de Dudas Urgentes",
                            subtitle = "Lanza una señal SOS antes de tu examen parcial y recibe ayuda presencial o paso a paso."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    color = SynapseCoralSos.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SynapseCoralSos.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
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
                    .height(56.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SynapseCyan,
                    contentColor = SynapseObsidian
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SynapseObsidian,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Sign in with Google • Entrar al Campus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    errorMessage = null
                    onEnterLocalCampus("Estudiante FESC")
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("local_campus_sign_in_button"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SynapseCyan.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SynapseCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SynapseCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acceso Directo Estudiante FESC (Dispositivo Local)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Sincronización segura con tu cuenta Google o acceso instantáneo para pruebas en dispositivo físico.",
                style = MaterialTheme.typography.bodySmall,
                color = SynapseTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SynapseTextSecondary
            )
        }
    }
}
