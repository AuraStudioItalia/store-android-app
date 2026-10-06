package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuraAppItem
import com.example.data.model.DownloadStatus
import com.example.ui.components.AuraAppCard
import com.example.ui.theme.AuraAmberWarning
import com.example.ui.theme.AuraCyanSecondary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraGreenSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    updateApps: List<AuraAppItem>,
    installedApps: List<AuraAppItem>,
    availableApps: List<AuraAppItem>,
    downloadStatusMap: Map<String, DownloadStatus>,
    isLoading: Boolean,
    onAppClick: (AuraAppItem) -> Unit,
    onInstallClick: (AuraAppItem) -> Unit,
    onLaunchClick: (AuraAppItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upToDateApps = installedApps.filter { !it.hasUpdate }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
        ) {
            // Header Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (updateApps.isNotEmpty()) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (updateApps.isNotEmpty()) Icons.Default.SystemUpdate else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (updateApps.isNotEmpty()) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (updateApps.isNotEmpty()) "${updateApps.size} Aggiornamenti Disponibili" else "Tutte le app sono aggiornate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (updateApps.isNotEmpty()) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${installedApps.size} app Aura installate sul dispositivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (updateApps.isNotEmpty()) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (updateApps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    updateApps.forEach { app -> onInstallClick(app) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_update_all"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aggiorna Tutte (${updateApps.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SECTION 1: Updates Available
            if (updateApps.isNotEmpty()) {
                item {
                    SectionHeader(title = "Aggiornamenti Disponibili (${updateApps.size})", color = AuraAmberWarning)
                }

                items(updateApps, key = { "upd_${it.folderName}" }) { app ->
                    AuraAppCard(
                        app = app,
                        downloadStatus = downloadStatusMap[app.appData.apk.packageName],
                        onCardClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) },
                        onLaunchClick = { onLaunchClick(app) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // SECTION 2: Up To Date Installed Apps
            if (upToDateApps.isNotEmpty()) {
                item {
                    SectionHeader(title = "App Installate Aggiornate (${upToDateApps.size})", color = AuraGreenSuccess)
                }

                items(upToDateApps, key = { "inst_${it.folderName}" }) { app ->
                    AuraAppCard(
                        app = app,
                        downloadStatus = downloadStatusMap[app.appData.apk.packageName],
                        onCardClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) },
                        onLaunchClick = { onLaunchClick(app) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // SECTION 3: Available to Install from Repo
            if (availableApps.isNotEmpty()) {
                item {
                    SectionHeader(title = "Disponibili da Repository (${availableApps.size})", color = AuraCyanSecondary)
                }

                items(availableApps, key = { "avail_${it.folderName}" }) { app ->
                    AuraAppCard(
                        app = app,
                        downloadStatus = downloadStatusMap[app.appData.apk.packageName],
                        onCardClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) },
                        onLaunchClick = { onLaunchClick(app) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            if (installedApps.isEmpty() && availableApps.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessuna applicazione trovata nel repository.",
                            color = AuraTextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = RoundedCornerShape(2.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
