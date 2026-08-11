package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AuthState
import com.example.ui.screens.AppDetailBottomSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.AuraAmberWarning
import com.example.ui.theme.AuraCyanSecondary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraVioletPrimary
import com.example.ui.viewmodel.AuraStoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraStoreApp(
    viewModel: AuraStoreViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val updateApps by viewModel.updateApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val downloadStatusMap by viewModel.downloadStatusMap.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val auraStoreApp by viewModel.auraStoreAppItem.collectAsState()
    val repositories by viewModel.repositories.collectAsState()
    val showAddRepoDialog by viewModel.showAddRepoDialog.collectAsState()
    val showCreateAccountDialog by viewModel.showCreateAccountDialog.collectAsState()
    val showInstallPermissionDialog by viewModel.showInstallPermissionDialog.collectAsState()
    val showSecurityNoticeDialog by viewModel.showSecurityNoticeDialog.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Re-check installed apps on lifecycle ON_RESUME (e.g. returning from APK installer)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkInstalledStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (authState is AuthState.LoggedOut) {
        AuthScreen(
            onSignIn = { email, pass, callback ->
                viewModel.signInWithEmail(email, pass, callback)
            },
            onIncognitoClick = {
                viewModel.proceedIncognito()
            },
            onCreateAccountClick = {
                viewModel.openCreateAccountDialog()
            },
            showCreateAccountDialog = showCreateAccountDialog,
            onDismissCreateAccountDialog = {
                viewModel.dismissCreateAccountDialog()
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 3.dp
                ) {
                    // TAB 0: Store
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Store else Icons.Outlined.Store,
                                contentDescription = "Store"
                            )
                        },
                        label = { Text("Store", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_store")
                    )

                    // TAB 1: Aggiornamenti
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (updateApps.isNotEmpty()) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        ) {
                                            Text("${updateApps.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.SystemUpdate else Icons.Outlined.SystemUpdate,
                                    contentDescription = "Aggiornamenti"
                                )
                            }
                        },
                        label = { Text("Aggiornamenti", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_updates")
                    )

                    // TAB 2: Profilo
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profilo"
                            )
                        },
                        label = { Text("Profilo", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { if (targetState > initialState) it / 4 else -it / 4 } togetherWith
                        fadeOut(animationSpec = tween(180))
                    },
                    label = "tab_navigation_animation"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(
                            apps = filteredApps,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            selectedCategory = selectedCategory,
                            onCategorySelect = { viewModel.onCategorySelected(it) },
                            downloadStatusMap = downloadStatusMap,
                            onAppClick = { viewModel.selectApp(it) },
                            onInstallClick = { viewModel.downloadAndInstall(it) },
                            onLaunchClick = { viewModel.launchInstalledApp(it.appData.apk.packageName) },
                            onRefresh = { viewModel.loadApps() }
                        )
                        1 -> UpdatesScreen(
                            updateApps = updateApps,
                            installedApps = installedApps,
                            availableApps = availableApps,
                            downloadStatusMap = downloadStatusMap,
                            isLoading = isLoading,
                            onAppClick = { viewModel.selectApp(it) },
                            onInstallClick = { viewModel.downloadAndInstall(it) },
                            onLaunchClick = { viewModel.launchInstalledApp(it.appData.apk.packageName) },
                            onRefresh = { viewModel.loadApps() }
                        )
                        2 -> ProfileScreen(
                            authState = authState,
                            themeMode = themeMode,
                            onThemeModeSelected = { viewModel.setThemeMode(it) },
                            repositories = repositories,
                            onAddRepoClick = { viewModel.openAddRepoDialog() },
                            onDeleteRepoClick = { viewModel.removeExternalRepository(it) },
                            auraStoreApp = auraStoreApp,
                            downloadStatusMap = downloadStatusMap,
                            onAppClick = { viewModel.selectApp(it) },
                            onInstallClick = { viewModel.downloadAndInstall(it) },
                            onLaunchClick = { viewModel.launchInstalledApp(it) },
                            onSignOut = { viewModel.signOut() },
                            onCreateAccountClick = { viewModel.openCreateAccountDialog() },
                            onManageInstallPermissions = { viewModel.openInstallPermissionSettings() }
                        )
                    }
                }
            }
        }

        // App Detail Sheet
        if (selectedApp != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            AppDetailBottomSheet(
                app = selectedApp!!,
                downloadStatus = downloadStatusMap[selectedApp!!.appData.apk.packageName],
                onDismiss = { viewModel.clearSelectedApp() },
                onInstallClick = { viewModel.downloadAndInstall(selectedApp!!) },
                onLaunchClick = { viewModel.launchInstalledApp(selectedApp!!.appData.apk.packageName) },
                sheetState = sheetState
            )
        }

        // Add External Repository Dialog
        if (showAddRepoDialog) {
            var inputName by remember { mutableStateOf("") }
            var inputUrl by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { viewModel.dismissAddRepoDialog() },
                title = {
                    Text(
                        text = "Aggiungi Repository Esterno",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = "Sono supportati esclusivamente i repository creati appositamente per Aura Store che seguono la struttura e lo schema ufficiale (JSON / Git). I repository esterni non sono controllati da Aura e sono da usare a proprio rischio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nome Repository") },
                            placeholder = { Text("Es. Repo App X") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_repo_name")
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("URL Repository") },
                            placeholder = { Text("https://github.com/x/repo, https://example.com/repo") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_repo_url")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputName.isNotBlank() && inputUrl.isNotBlank()) {
                                viewModel.addExternalRepository(inputName, inputUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("btn_confirm_add_repo")
                    ) {
                        Text("Aggiungi", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissAddRepoDialog() }) {
                        Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp)
            )
        }

        // Create Account Dialog (if open from profile)
        if (showCreateAccountDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissCreateAccountDialog() },
                title = { Text("Creazione Account Aura", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Text(
                        text = "La creazione dell'account per Aura Store deve essere effettuata su apps.aurastudioitalia.it oppure scaricando l'app Aura Docs o Nyra.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissCreateAccountDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Ho Capito", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp)
            )
        }

        // First-Time Launch Official Version Security Dialog
        if (showSecurityNoticeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSecurityNoticeDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Versione Verificata",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Versione Ufficiale Aura Store",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Questa è la versione ufficiale di Aura Store distribuita attraverso i canali ufficiali di Aura Studio Italia.\n\nSe l'applicazione è stata scaricata da fonti terze non verificate, potrebbe essere stata modificata e non garantire la massima sicurezza.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissSecurityNoticeDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("btn_security_notice_confirm")
                    ) {
                        Text("Ho Capito • Continua", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp)
            )
        }

        // Install Permission Dialog
        if (showInstallPermissionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissInstallPermissionDialog() },
                title = { Text("Permesso di Installazione Richiesto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Text(
                        text = "Per installare l'APK scaricato, devi consentire ad Aura Store di installare app da origini sconosciute nelle Impostazioni di Android.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.openInstallPermissionSettings() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Apri Impostazioni", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissInstallPermissionDialog() }) {
                        Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}
