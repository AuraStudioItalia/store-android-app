package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataJsonWrapper(
    @Json(name = "app") val app: AuraAppData
)

@JsonClass(generateAdapter = true)
data class AuraAppData(
    @Json(name = "name") val name: String? = null,
    @Json(name = "icon_url") val iconUrl: String? = "icon.png",
    @Json(name = "description") val description: String? = "",
    @Json(name = "version") val version: String = "1.0",
    @Json(name = "changelog") val changelog: List<ChangelogItem>? = emptyList(),
    @Json(name = "apk") val apk: ApkInfo
)

@JsonClass(generateAdapter = true)
data class ChangelogItem(
    @Json(name = "version") val version: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "changes") val changes: List<String>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApkInfo(
    @Json(name = "file_name") val fileName: String,
    @Json(name = "size_mb") val sizeMb: Double? = 0.0,
    @Json(name = "min_android_version") val minAndroidVersion: String? = "7.0",
    @Json(name = "target_sdk") val targetSdk: String? = "36",
    @Json(name = "signature_sha256") val signatureSha256: String? = "",
    @Json(name = "package_name") val packageName: String
)

data class GitHubContentItem(
    val name: String,
    val path: String,
    val type: String,
    @Json(name = "download_url") val downloadUrl: String? = null
)

data class RepositoryInfo(
    val id: String,
    val name: String,
    val url: String,
    val isOfficial: Boolean = false
)

data class AuraAppItem(
    val folderName: String,
    val appData: AuraAppData,
    val iconFullUrl: String,
    val apkFullUrl: String,
    val isInstalled: Boolean = false,
    val installedVersionName: String? = null,
    val installedVersionCode: Long = 0,
    val hasUpdate: Boolean = false,
    val isFeatured: Boolean = false,
    val category: String = "General",
    val isOfficial: Boolean = true,
    val repoName: String = "Aura Studio Italia"
) {
    val name: String
        get() = appData.name?.takeIf { it.isNotBlank() } ?: folderName

    fun isAuraStore(): Boolean {
        return folderName.equals("AuraStore", ignoreCase = true) ||
                appData.apk.packageName.equals("com.example.aurastore", ignoreCase = true) ||
                appData.apk.packageName.equals("com.example", ignoreCase = true) ||
                appData.apk.packageName.equals("com.aura.store", ignoreCase = true) ||
                appData.apk.packageName.contains("aurastore", ignoreCase = true) ||
                appData.apk.packageName.contains("aura.store", ignoreCase = true) ||
                name.contains("Aura Store", ignoreCase = true)
    }
}

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus()
    data class Completed(val filePath: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

sealed class AuthState {
    object LoggedOut : AuthState()
    object Incognito : AuthState()
    data class LoggedIn(
        val email: String,
        val uid: String,
        val nome: String? = null,
        val cognome: String? = null,
        val profileImage: String? = null
    ) : AuthState()
}
