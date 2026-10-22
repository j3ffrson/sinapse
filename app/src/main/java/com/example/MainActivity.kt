package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.CampusZone
import com.example.data.model.CollaborationMessage
import com.example.data.model.SosAlert
import com.example.data.model.StudentProfile
import com.example.data.model.StudyTable
import com.example.data.model.UiState
import com.example.data.repository.SynapseRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.safeFirebaseAuth
import com.example.ui.auth.signOut
import com.example.ui.components.CollaborationThreadDialog
import com.example.ui.components.CreateStudyTableDialog
import com.example.ui.components.LaunchSosAlertDialog
import com.example.ui.components.StudentProfileDialog
import com.example.ui.screens.CampusRadarScreen
import com.example.ui.screens.SosRadarScreen
import com.example.ui.screens.StudyTablesScreen
import com.example.ui.theme.FESCSynapseTheme
import com.example.ui.theme.SynapseBorder
import com.example.ui.theme.SynapseCardSurface
import com.example.ui.theme.SynapseCoralSos
import com.example.ui.theme.SynapseCyan
import com.example.ui.theme.SynapseElevatedSurface
import com.example.ui.theme.SynapseObsidian
import com.example.ui.theme.SynapseSurface
import com.example.ui.theme.SynapseTextSecondary
import com.example.ui.theme.isSynapseDarkMode
import com.example.ui.viewmodel.SynapseViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        }
        enableEdgeToEdge()
        setContent {
            FESCSynapseTheme {
                AppNavigation()
            }
        }
    }
}

internal fun FirebaseAuth?.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val authInstance = this@authStateFlow
    if (authInstance == null) {
        trySend(null)
        awaitClose {}
        return@callbackFlow
    }
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    authInstance.addAuthStateListener(listener)
    awaitClose { authInstance.removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth? = null) {
    val context = LocalContext.current
    val resolvedAuth = remember(auth, context) { auth ?: safeFirebaseAuth(context) }
    val authFlowUser by resolvedAuth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = resolvedAuth?.currentUser)
    var directFirebaseUser by remember { mutableStateOf(resolvedAuth?.currentUser) }
    var localCampusDisplayName by rememberSaveable { mutableStateOf<String?>(null) }

    val activeFirebaseUser = authFlowUser ?: directFirebaseUser

    if (activeFirebaseUser == null && localCampusDisplayName == null) {
        AuthScreen(
            onAuthSuccess = {
                directFirebaseUser = resolvedAuth?.currentUser
                if (directFirebaseUser == null) {
                    localCampusDisplayName = "Estudiante FESC"
                }
            },
            onEnterLocalCampus = { displayName ->
                localCampusDisplayName = displayName.ifBlank { "Estudiante FESC" }
            }
        )
    } else {
        val isLocalMode = activeFirebaseUser == null
        val sessionKey = activeFirebaseUser?.uid ?: "local_campus_session"
        val displayName = activeFirebaseUser?.displayName
            ?: localCampusDisplayName
            ?: "Estudiante FESC"

        val viewModel: SynapseViewModel = viewModel(
            key = sessionKey,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val db = runCatching {
                        if (FirebaseApp.getApps(app).isEmpty()) {
                            FirebaseApp.initializeApp(app)
                        }
                        val resId = app.resources.getIdentifier(
                            "firestore_database_id",
                            "string",
                            app.packageName
                        )
                        val databaseId = if (resId != 0) {
                            app.getString(resId)
                        } else {
                            "ai-studio-android-fescsyna-e52cef2b-05e0-49d4-9d9b-3a5040aafcc1"
                        }
                        if (FirebaseApp.getApps(app).isNotEmpty()) {
                            FirebaseFirestore.getInstance(databaseId)
                        } else {
                            null
                        }
                    }.getOrNull()

                    SynapseViewModel(
                        repository = SynapseRepository(
                            db = db,
                            isLocalSession = isLocalMode || db == null,
                            localUserId = sessionKey
                        ),
                        currentUserId = sessionKey,
                        initialDisplayName = displayName
                    )
                }
            }
        )
        MainSynapseScaffold(
            viewModel = viewModel,
            onExitSession = {
                directFirebaseUser = null
                localCampusDisplayName = null
            }
        )
    }
}

enum class SynapseTab(val route: String, val label: String) {
    RADAR("radar", "Mapa Campus"),
    TABLES("tables", "Mesas en Vivo"),
    SOS("sos", "Alertas SOS")
}

private data class ActiveThreadContext(
    val parentId: String,
    val parentType: String,
    val title: String,
    val subtitle: String,
    val spotReference: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSynapseScaffold(
    viewModel: SynapseViewModel,
    onExitSession: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val tablesState by viewModel.studyTablesState.collectAsStateWithLifecycle()
    val sosState by viewModel.sosAlertsState.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val selectedZone by viewModel.selectedZoneFilter.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(SynapseTab.RADAR) }
    var createTableZoneModal by remember { mutableStateOf<CampusZone?>(null) }
    var launchSosZoneModal by remember { mutableStateOf<CampusZone?>(null) }
    var activeThread by remember { mutableStateOf<ActiveThreadContext?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

    if (currentTab != SynapseTab.RADAR) {
        BackHandler {
            currentTab = SynapseTab.RADAR
        }
    }

    val profile: StudentProfile? = (profileState as? UiState.Success)?.data
    val studyTables: List<StudyTable> = (tablesState as? UiState.Success)?.data ?: emptyList()
    val sosAlerts: List<SosAlert> = (sosState as? UiState.Success)?.data ?: emptyList()
    val allMessages: List<CollaborationMessage> = (messagesState as? UiState.Success)?.data ?: emptyList()

    val urgentSosCount = sosAlerts.count { !it.isResolved }
    val activeTablesCount = studyTables.count { it.isActive }
    val userDefaultZone = CampusZone.fromId(profile?.currentZone ?: CampusZone.LIBRARY.id)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SynapseObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SynapseSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SynapseCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = SynapseCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FESC Synapse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Campus en Tiempo Real • ${userDefaultZone.shortName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SynapseCyan
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = { launchSosZoneModal = selectedZone ?: userDefaultZone },
                        color = SynapseCoralSos.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SynapseCoralSos),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("topbar_sos_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Lanzar SOS",
                                tint = SynapseCoralSos,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOS",
                                style = MaterialTheme.typography.labelMedium,
                                color = SynapseCoralSos,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSynapseDarkMode = !isSynapseDarkMode },
                        modifier = Modifier.testTag("topbar_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSynapseDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isSynapseDarkMode) "Cambiar a modo claro" else "Cambiar a modo oscuro",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.testTag("open_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil Estudiantil",
                            tint = SynapseCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SynapseSurface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == SynapseTab.RADAR,
                    onClick = { currentTab = SynapseTab.RADAR },
                    icon = {
                        Icon(Icons.Default.Map, contentDescription = SynapseTab.RADAR.label)
                    },
                    label = { Text(SynapseTab.RADAR.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = SynapseCyan,
                        unselectedIconColor = SynapseTextSecondary,
                        unselectedTextColor = SynapseTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_radar")
                )

                NavigationBarItem(
                    selected = currentTab == SynapseTab.TABLES,
                    onClick = { currentTab = SynapseTab.TABLES },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeTablesCount > 0) {
                                    Badge(
                                        containerColor = SynapseCyan,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ) {
                                        Text("$activeTablesCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = SynapseTab.TABLES.label)
                        }
                    },
                    label = { Text(SynapseTab.TABLES.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = SynapseCyan,
                        unselectedIconColor = SynapseTextSecondary,
                        unselectedTextColor = SynapseTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_tables")
                )

                NavigationBarItem(
                    selected = currentTab == SynapseTab.SOS,
                    onClick = { currentTab = SynapseTab.SOS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (urgentSosCount > 0) {
                                    Badge(
                                        containerColor = SynapseCoralSos,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ) {
                                        Text("$urgentSosCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = SynapseTab.SOS.label)
                        }
                    },
                    label = { Text(SynapseTab.SOS.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = SynapseCoralSos,
                        unselectedIconColor = SynapseTextSecondary,
                        unselectedTextColor = SynapseTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_sos")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = bannerMessage != null) {
                bannerMessage?.let { msg ->
                    Surface(
                        color = SynapseElevatedSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SynapseCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearBannerMessage() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar aviso",
                                    tint = SynapseTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (tablesState is UiState.Loading && sosState is UiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SynapseCyan)
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = "Conectando con el radar sináptico del campus FESC...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SynapseTextSecondary
                        )
                    }
                }
            } else {
                when (currentTab) {
                    SynapseTab.RADAR -> {
                        CampusRadarScreen(
                            profile = profile,
                            studyTables = studyTables,
                            sosAlerts = sosAlerts,
                            selectedZone = selectedZone,
                            selectedSubject = selectedSubject,
                            onSelectZone = viewModel::selectZoneFilter,
                            onSelectSubject = viewModel::selectSubjectFilter,
                            onCheckInZone = { zone -> viewModel.checkInToZone(zone) },
                            onOpenCreateTable = { zone -> createTableZoneModal = zone },
                            onOpenLaunchSos = { zone -> launchSosZoneModal = zone },
                            onToggleJoinTable = viewModel::toggleJoinTable,
                            onOpenTableThread = { table ->
                                activeThread = ActiveThreadContext(
                                    parentId = table.id,
                                    parentType = "TABLE",
                                    title = table.subject,
                                    subtitle = table.topic,
                                    spotReference = table.spotReference
                                )
                            },
                            onOfferSosHelp = { alert -> viewModel.respondInPersonToSos(alert) },
                            onOpenSosThread = { alert ->
                                activeThread = ActiveThreadContext(
                                    parentId = alert.id,
                                    parentType = "SOS",
                                    title = alert.questionTitle,
                                    subtitle = alert.subject,
                                    spotReference = alert.spotReference
                                )
                            },
                            onToggleResolveSos = viewModel::toggleResolveSos,
                            onOpenProfileModal = { showProfileDialog = true },
                            onSeedDemo = viewModel::seedDefaultCampusScenario
                        )
                    }

                    SynapseTab.TABLES -> {
                        StudyTablesScreen(
                            studyTables = studyTables,
                            selectedZone = selectedZone,
                            searchQuery = searchQuery,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectZone = viewModel::selectZoneFilter,
                            onOpenCreateTable = { createTableZoneModal = selectedZone ?: userDefaultZone },
                            onToggleJoinTable = viewModel::toggleJoinTable,
                            onOpenTableThread = { table ->
                                activeThread = ActiveThreadContext(
                                    parentId = table.id,
                                    parentType = "TABLE",
                                    title = table.subject,
                                    subtitle = table.topic,
                                    spotReference = table.spotReference
                                )
                            },
                            onSeedDemo = viewModel::seedDefaultCampusScenario
                        )
                    }

                    SynapseTab.SOS -> {
                        SosRadarScreen(
                            sosAlerts = sosAlerts,
                            onOpenLaunchSos = { launchSosZoneModal = selectedZone ?: userDefaultZone },
                            onOfferHelp = { alert -> viewModel.respondInPersonToSos(alert) },
                            onOpenSosThread = { alert ->
                                activeThread = ActiveThreadContext(
                                    parentId = alert.id,
                                    parentType = "SOS",
                                    title = alert.questionTitle,
                                    subtitle = alert.subject,
                                    spotReference = alert.spotReference
                                )
                            },
                            onToggleResolve = viewModel::toggleResolveSos,
                            onSeedDemo = viewModel::seedDefaultCampusScenario
                        )
                    }
                }
            }
        }
    }

    createTableZoneModal?.let { initialZone ->
        CreateStudyTableDialog(
            initialZone = initialZone,
            onDismiss = { createTableZoneModal = null },
            onCreateTable = { subject, topic, zone, spot, vibe, maxSeats ->
                viewModel.createNewStudyTable(
                    subject = subject,
                    topic = topic,
                    campusZone = zone,
                    spotReference = spot,
                    studyVibe = vibe,
                    maxSeats = maxSeats
                )
            }
        )
    }

    launchSosZoneModal?.let { initialZone ->
        LaunchSosAlertDialog(
            initialZone = initialZone,
            onDismiss = { launchSosZoneModal = null },
            onLaunchSos = { subject, title, detail, urgency, zone, spot ->
                viewModel.launchSosAlert(
                    subject = subject,
                    questionTitle = title,
                    questionDetail = detail,
                    examUrgency = urgency,
                    campusZone = zone,
                    spotReference = spot
                )
            }
        )
    }

    activeThread?.let { thread ->
        val threadMessages = remember(allMessages, thread.parentId) {
            allMessages.filter { it.parentId == thread.parentId }
        }
        CollaborationThreadDialog(
            title = thread.title,
            subtitle = thread.subtitle,
            spotReference = thread.spotReference,
            parentType = thread.parentType,
            messages = threadMessages,
            onDismiss = { activeThread = null },
            onSendMessage = { content, isGoingInPerson ->
                viewModel.sendThreadMessage(
                    parentId = thread.parentId,
                    parentType = thread.parentType,
                    content = content,
                    isGoingInPerson = isGoingInPerson
                )
            }
        )
    }

    if (showProfileDialog) {
        StudentProfileDialog(
            profile = profile,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = { name, program, semester, zone, status, subjects ->
                viewModel.updateProfileDetails(
                    displayName = name,
                    program = program,
                    semester = semester,
                    currentZone = zone,
                    statusMode = status,
                    enrolledSubjects = subjects
                )
            },
            onReseedCampusDemo = viewModel::seedDefaultCampusScenario,
            onSignOut = {
                showProfileDialog = false
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        onExitSession()
                    },
                    scope = scope
                )
            }
        )
    }
}
