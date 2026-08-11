package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.ui.theme.AuraCyanSecondary
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletPrimary

@Composable
fun DownloadProgressBar(
    status: DownloadStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        is DownloadStatus.Downloading -> {
            val progress = if (status.progressPercent >= 0) status.progressPercent / 100f else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "DownloadProgress")

            Column(modifier = modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (status.progressPercent >= 0) "Download in corso... ${status.progressPercent}%" else "Download in corso...",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraCyanSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (status.totalBytes > 0) {
                        val downloadedMb = status.bytesDownloaded / (1024f * 1024f)
                        val totalMb = status.totalBytes / (1024f * 1024f)
                        Text(
                            text = String.format("%.1f / %.1f MB", downloadedMb, totalMb),
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (status.progressPercent >= 0) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AuraCyanSecondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AuraCyanSecondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
        is DownloadStatus.Completed -> {
            Text(
                text = "✓ Download completato. Avvio installazione...",
                style = MaterialTheme.typography.bodySmall,
                color = AuraVioletPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        is DownloadStatus.Error -> {
            Text(
                text = "❌ ${status.message}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
        DownloadStatus.Idle -> {
            // Nothing to render
        }
    }
}
