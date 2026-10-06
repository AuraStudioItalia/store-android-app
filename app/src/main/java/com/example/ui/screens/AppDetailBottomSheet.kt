package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AuraAppItem
import com.example.data.model.DownloadStatus
import com.example.ui.components.DownloadProgressBar
import com.example.ui.components.ExternalTag
import com.example.ui.components.OfficialTag
import com.example.ui.components.StatusBadge
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
fun AppDetailBottomSheet(
    app: AuraAppItem,
    downloadStatus: DownloadStatus?,
    onDismiss: () -> Unit,
    onInstallClick: () -> Unit,
    onLaunchClick: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.iconFullUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = app.folderName,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = app.appData.apk.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (app.isOfficial) {
                            OfficialTag()
                        } else {
                            ExternalTag()
                        }

                        if (app.hasUpdate) {
                            StatusBadge(text = "AGGIORNAMENTO", color = MaterialTheme.colorScheme.tertiary)
                        } else if (app.isInstalled) {
                            StatusBadge(text = "INSTALLATA v${app.installedVersionName ?: ""}", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spec Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SpecItem(title = "Versione", value = "v${app.appData.version}", icon = Icons.Default.Info)
                    SpecItem(title = "Dimensione", value = "${app.appData.apk.sizeMb ?: 0.0} MB", icon = Icons.Default.Storage)
                    SpecItem(title = "Target SDK", value = app.appData.apk.targetSdk ?: "36", icon = Icons.Default.Android)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Download Status / Install Button
            if (downloadStatus != null && downloadStatus !is DownloadStatus.Idle) {
                DownloadProgressBar(status = downloadStatus)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (app.isInstalled) {
                    OutlinedButton(
                        onClick = onLaunchClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_btn_open"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apri App", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onInstallClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_btn_reinstall"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (app.hasUpdate) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            contentColor = if (app.hasUpdate) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (app.hasUpdate) Icons.Default.SystemUpdate else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (app.hasUpdate) "Aggiorna" else "Reinstalla", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onInstallClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("detail_btn_install"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scarica e Installa APK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Section
            if (!app.appData.description.isNullOrBlank()) {
                Text(
                    text = "Descrizione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = app.appData.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Security & Signature Card
            if (!app.appData.apk.signatureSha256.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Firma Digitale SHA-256", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.appData.apk.signatureSha256,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Changelog Section
            val changelog = app.appData.changelog
            if (!changelog.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cronologia Modifiche (Changelog)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                changelog.forEach { logItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "v${logItem.version ?: app.appData.version}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if (!logItem.date.isNullOrBlank()) {
                                    Text(
                                        text = logItem.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!logItem.changes.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                logItem.changes.forEach { change ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = change,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SpecItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
