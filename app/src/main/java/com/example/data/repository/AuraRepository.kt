package com.example.data.repository

import android.content.Context
import com.example.data.installer.ApkInstaller
import com.example.data.model.AuraAppData
import com.example.data.model.AuraAppItem
import com.example.data.model.DataJsonWrapper
import com.example.data.model.DownloadStatus
import com.example.data.model.RepositoryInfo
import com.example.data.remote.GitHubApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AuraRepository(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val apiService: GitHubApiService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GitHubApiService::class.java)

    private val dataWrapperAdapter = moshi.adapter(DataJsonWrapper::class.java)

    suspend fun fetchApps(repositories: List<RepositoryInfo> = emptyList()): Result<List<AuraAppItem>> = withContext(Dispatchers.IO) {
        try {
            val appItems = mutableListOf<AuraAppItem>()
            val officialRepo = repositories.firstOrNull { it.isOfficial } ?: RepositoryInfo(
                id = "aura_official",
                name = "Aura Studio Italia",
                url = "https://github.com/AuraStudioItalia/aura-store.git",
                isOfficial = true
            )

            val foldersToScan = mutableSetOf<String>()

            try {
                val contents = apiService.getRepoContents()
                contents.filter { it.type == "dir" }.forEach { dir ->
                    foldersToScan.add(dir.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback list of folders in case GitHub API rate limits
            if (foldersToScan.isEmpty()) {
                foldersToScan.addAll(listOf("Nyra", "AuraDocs", "AuraStore"))
            }

            for (folder in foldersToScan) {
                val rawDataJsonUrl = "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/$folder/data.json"
                try {
                    val jsonRequest = Request.Builder().url(rawDataJsonUrl).build()
                    val response = okHttpClient.newCall(jsonRequest).execute()

                    if (response.isSuccessful) {
                        val jsonStr = response.body?.string()
                        if (!jsonStr.isNullOrBlank()) {
                            val wrapper = dataWrapperAdapter.fromJson(jsonStr)
                            val appData = wrapper?.app
                            if (appData != null) {
                                val iconName = appData.iconUrl.takeIf { !it.isNullOrBlank() } ?: "icon.png"
                                val iconFullUrl = if (iconName.startsWith("http")) {
                                    iconName
                                } else {
                                    "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/$folder/$iconName"
                                }

                                val apkFullUrl = "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/$folder/${appData.apk.fileName}"

                                val item = AuraAppItem(
                                    folderName = folder,
                                    appData = appData,
                                    iconFullUrl = iconFullUrl,
                                    apkFullUrl = apkFullUrl,
                                    isFeatured = folder.contains("Nyra", ignoreCase = true) || folder.contains("Docs", ignoreCase = true),
                                    category = when {
                                        folder.contains("Nyra", ignoreCase = true) -> "Produttività"
                                        folder.contains("Docs", ignoreCase = true) -> "Documentazione"
                                        else -> "Utility"
                                    },
                                    isOfficial = true,
                                    repoName = officialRepo.name
                                )
                                appItems.add(item)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fetch external repositories if configured
            val externalRepos = repositories.filter { !it.isOfficial }
            for (extRepo in externalRepos) {
                try {
                    val extApps = fetchExternalRepositoryApps(extRepo)
                    appItems.addAll(extApps)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val enriched = ApkInstaller.checkAndEnrichApps(context, appItems)
            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchExternalRepositoryApps(repo: RepositoryInfo): List<AuraAppItem> {
        val results = mutableListOf<AuraAppItem>()
        val cleanUrl = repo.url.trim().removeSuffix("/").removeSuffix(".git")
        if (cleanUrl.isBlank()) return results

        // Determine base raw URLs for repository access
        var gitHubOwnerRepo: Pair<String, String>? = null
        val rawBaseUrls = mutableListOf<String>()

        if (cleanUrl.contains("raw.githubusercontent.com")) {
            val after = cleanUrl.substringAfter("raw.githubusercontent.com/")
            val segs = after.split("/").filter { it.isNotBlank() }
            if (segs.size >= 3) {
                val owner = segs[0]
                val repoName = segs[1]
                val branch = segs[2]
                gitHubOwnerRepo = Pair(owner, repoName)
                rawBaseUrls.add("https://raw.githubusercontent.com/$owner/$repoName/$branch")
            } else if (segs.size == 2) {
                val owner = segs[0]
                val repoName = segs[1]
                gitHubOwnerRepo = Pair(owner, repoName)
                rawBaseUrls.add("https://raw.githubusercontent.com/$owner/$repoName/main")
                rawBaseUrls.add("https://raw.githubusercontent.com/$owner/$repoName/master")
            }
        } else if (cleanUrl.contains("github.com")) {
            val after = cleanUrl.substringAfter("github.com/")
            val segs = after.split("/").filter { it.isNotBlank() }
            if (segs.size >= 2) {
                val owner = segs[0]
                val repoName = segs[1]
                gitHubOwnerRepo = Pair(owner, repoName)
                rawBaseUrls.add("https://raw.githubusercontent.com/$owner/$repoName/main")
                rawBaseUrls.add("https://raw.githubusercontent.com/$owner/$repoName/master")
            }
        } else if (cleanUrl.contains("gitlab.com")) {
            val after = cleanUrl.substringAfter("gitlab.com/")
            val segs = after.split("/").filter { it.isNotBlank() }
            if (segs.size >= 2) {
                val group = segs[0]
                val proj = segs[1]
                rawBaseUrls.add("https://gitlab.com/$group/$proj/-/raw/main")
                rawBaseUrls.add("https://gitlab.com/$group/$proj/-/raw/master")
            }
        } else {
            rawBaseUrls.add(cleanUrl)
        }

        // Folders that contain apps following the official Aura Store directory model:
        // repo/<folder>/data.json
        val discoveredFolders = mutableSetOf<String>()

        // 1. If GitHub repo, discover subdirectories inside 'repo' via GitHub contents API
        gitHubOwnerRepo?.let { (owner, repoName) ->
            try {
                val apiUrl = "https://api.github.com/repos/$owner/$repoName/contents/repo"
                val req = Request.Builder()
                    .url(apiUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()
                val resp = okHttpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val bodyStr = resp.body?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val jsonArr = org.json.JSONArray(bodyStr)
                        for (i in 0 until jsonArr.length()) {
                            val item = jsonArr.optJSONObject(i)
                            if (item != null && item.optString("type") == "dir") {
                                val name = item.optString("name")
                                if (name.isNotBlank()) discoveredFolders.add(name)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore API limit / absent contents
            }
        }

        // 2. Discover folders from repo index manifests: repo/index.json, repo.json, etc.
        var workingBaseRawUrl = rawBaseUrls.firstOrNull() ?: cleanUrl
        for (rawBase in rawBaseUrls) {
            val manifestCandidates = listOf(
                "$rawBase/repo/index.json",
                "$rawBase/index.json",
                "$rawBase/repo.json",
                "$rawBase/repo/apps.json",
                "$rawBase/apps.json"
            )
            for (mUrl in manifestCandidates) {
                try {
                    val req = Request.Builder().url(mUrl).build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: continue
                        if (body.isNotBlank()) {
                            workingBaseRawUrl = rawBase
                            val trimmed = body.trim()
                            if (trimmed.startsWith("{")) {
                                val obj = org.json.JSONObject(trimmed)
                                val arr = obj.optJSONArray("apps") ?: obj.optJSONArray("directories") ?: obj.optJSONArray("folders")
                                if (arr != null) {
                                    for (i in 0 until arr.length()) {
                                        val fn = arr.optString(i)
                                        if (fn.isNotBlank()) discoveredFolders.add(fn)
                                    }
                                }
                            } else if (trimmed.startsWith("[")) {
                                val arr = org.json.JSONArray(trimmed)
                                for (i in 0 until arr.length()) {
                                    val item = arr.opt(i)
                                    if (item is String && item.isNotBlank()) {
                                        discoveredFolders.add(item)
                                    }
                                }
                            }
                            if (discoveredFolders.isNotEmpty()) break
                        }
                    }
                } catch (e: Exception) {
                    // Try next candidate
                }
            }
            if (discoveredFolders.isNotEmpty()) break
        }

        // 3. For each folder found under repo/<folder>, parse data.json
        for (folder in discoveredFolders) {
            val candidateJsonUrls = listOf(
                "$workingBaseRawUrl/repo/$folder/data.json",
                "$workingBaseRawUrl/$folder/data.json"
            )

            for (jsonUrl in candidateJsonUrls) {
                try {
                    val req = Request.Builder().url(jsonUrl).build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val jsonStr = resp.body?.string()
                        if (!jsonStr.isNullOrBlank()) {
                            val wrapper = dataWrapperAdapter.fromJson(jsonStr)
                            val appData = wrapper?.app
                            if (appData != null) {
                                val parentDir = jsonUrl.substringBeforeLast("/")
                                val iconName = appData.iconUrl.takeIf { !it.isNullOrBlank() } ?: "icon.png"
                                val iconFullUrl = if (iconName.startsWith("http")) {
                                    iconName
                                } else {
                                    "$parentDir/$iconName"
                                }

                                val apkFileName = appData.apk.fileName
                                val apkFullUrl = if (apkFileName.startsWith("http")) {
                                    apkFileName
                                } else {
                                    "$parentDir/$apkFileName"
                                }

                                results.add(
                                    AuraAppItem(
                                        folderName = "${repo.id}_$folder",
                                        appData = appData,
                                        iconFullUrl = iconFullUrl,
                                        apkFullUrl = apkFullUrl,
                                        isFeatured = false,
                                        category = "Esterno",
                                        isOfficial = false,
                                        repoName = repo.name
                                    )
                                )
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next candidate
                }
            }
        }

        // 4. Fallback: direct data.json or index.json
        if (results.isEmpty()) {
            for (rawBase in rawBaseUrls) {
                val directDataUrls = listOf(
                    "$rawBase/repo/data.json",
                    "$rawBase/data.json",
                    "$rawBase/index.json",
                    "$rawBase/apps.json"
                )
                for (directUrl in directDataUrls) {
                    try {
                        val req = Request.Builder().url(directUrl).build()
                        val resp = okHttpClient.newCall(req).execute()
                        if (resp.isSuccessful) {
                            val jsonStr = resp.body?.string()
                            if (!jsonStr.isNullOrBlank()) {
                                val parsed = parseRepoJson(jsonStr, directUrl, directUrl.substringBeforeLast("/"), repo.name, repo.id)
                                if (parsed.isNotEmpty()) {
                                    results.addAll(parsed)
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore and try next
                    }
                }
                if (results.isNotEmpty()) break
            }
        }

        return results
    }

    private fun parseRepoJson(jsonStr: String, targetUrl: String, baseUrl: String, repoName: String, repoId: String = "ext"): List<AuraAppItem> {
        val items = mutableListOf<AuraAppItem>()
        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("{")) {
                val jsonObj = org.json.JSONObject(trimmed)

                // F-Droid index-v1.json or index.json format
                if (jsonObj.has("apps") || jsonObj.has("packages")) {
                    val appsArray = jsonObj.optJSONArray("apps")
                    val pkgsObj = jsonObj.optJSONObject("packages")

                    if (appsArray != null) {
                        for (i in 0 until appsArray.length()) {
                            val appObj = appsArray.optJSONObject(i) ?: continue
                            val pkgName = appObj.optString("packageName").ifBlank { appObj.optString("id") }
                            if (pkgName.isBlank()) continue

                            val appName = appObj.optString("name").ifBlank { appObj.optString("title").ifBlank { pkgName } }
                            val summary = appObj.optString("summary").ifBlank { appObj.optString("description") }

                            var iconName = appObj.optString("icon")
                            val iconFullUrl = when {
                                iconName.startsWith("http") -> iconName
                                iconName.isNotBlank() -> "$baseUrl/icons/$iconName"
                                else -> "$baseUrl/icon.png"
                            }

                            var version = "1.0"
                            var apkFileName = "$pkgName.apk"
                            var sizeMb = 0.0

                            // Try to extract version/apk details from packages object
                            if (pkgsObj != null && pkgsObj.has(pkgName)) {
                                val pkgVers = pkgsObj.optJSONArray(pkgName)
                                if (pkgVers != null && pkgVers.length() > 0) {
                                    val latestVer = pkgVers.optJSONObject(0)
                                    if (latestVer != null) {
                                        version = latestVer.optString("versionName", "1.0")
                                        apkFileName = latestVer.optString("apkName", "$pkgName.apk")
                                        val sizeBytes = latestVer.optLong("size", 0L)
                                        if (sizeBytes > 0) {
                                            sizeMb = String.format("%.1f", sizeBytes / (1024.0 * 1024.0)).replace(",", ".").toDoubleOrNull() ?: 0.0
                                        }
                                    }
                                }
                            }

                            val apkFullUrl = if (apkFileName.startsWith("http")) apkFileName else "$baseUrl/$apkFileName"

                            val appData = AuraAppData(
                                name = appName,
                                iconUrl = iconFullUrl,
                                description = summary,
                                version = version,
                                apk = com.example.data.model.ApkInfo(
                                    packageName = pkgName,
                                    fileName = apkFileName,
                                    sizeMb = sizeMb
                                )
                            )

                            items.add(
                                AuraAppItem(
                                    folderName = "${repoId}_$pkgName",
                                    appData = appData,
                                    iconFullUrl = iconFullUrl,
                                    apkFullUrl = apkFullUrl,
                                    isFeatured = false,
                                    category = "Esterno",
                                    isOfficial = false,
                                    repoName = repoName
                                )
                            )
                        }
                    }
                } else if (jsonObj.has("app")) {
                    // Single Aura DataJsonWrapper format
                    val wrapper = dataWrapperAdapter.fromJson(jsonStr)
                    val appData = wrapper?.app
                    if (appData != null) {
                        val iconName = appData.iconUrl.takeIf { !it.isNullOrBlank() } ?: "icon.png"
                        val iconFullUrl = if (iconName.startsWith("http")) iconName else "$baseUrl/$iconName"
                        val apkFullUrl = if (appData.apk.fileName.startsWith("http")) appData.apk.fileName else "$baseUrl/${appData.apk.fileName}"

                        val pkgFolder = appData.apk.packageName.ifBlank { "external_app" }
                        items.add(
                            AuraAppItem(
                                folderName = "${repoId}_$pkgFolder",
                                appData = appData,
                                iconFullUrl = iconFullUrl,
                                apkFullUrl = apkFullUrl,
                                isFeatured = false,
                                category = "Esterno",
                                isOfficial = false,
                                repoName = repoName
                            )
                        )
                    }
                }
            } else if (trimmed.startsWith("[")) {
                // Array of apps format
                val jsonArr = org.json.JSONArray(trimmed)
                for (i in 0 until jsonArr.length()) {
                    val appObj = jsonArr.optJSONObject(i) ?: continue
                    val pkgName = appObj.optString("packageName").ifBlank { appObj.optString("id", "ext_pkg_$i") }
                    val appName = appObj.optString("name").ifBlank { pkgName }
                    val summary = appObj.optString("description").ifBlank { appObj.optString("summary") }
                    val version = appObj.optString("version", "1.0")
                    val iconUrl = appObj.optString("iconUrl").ifBlank { "$baseUrl/icon.png" }
                    val apkUrl = appObj.optString("apkUrl").ifBlank { "$baseUrl/$pkgName.apk" }

                    val appData = AuraAppData(
                        name = appName,
                        iconUrl = iconUrl,
                        description = summary,
                        version = version,
                        apk = com.example.data.model.ApkInfo(
                            packageName = pkgName,
                            fileName = "$pkgName.apk"
                        )
                    )

                    items.add(
                        AuraAppItem(
                            folderName = "${repoId}_$pkgName",
                            appData = appData,
                            iconFullUrl = iconUrl,
                            apkFullUrl = apkUrl,
                            isFeatured = false,
                            category = "Esterno",
                            isOfficial = false,
                            repoName = repoName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    fun downloadApk(appItem: AuraAppItem): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Downloading(0, 0, 0))

        val downloadDir = context.getExternalFilesDir("apks") ?: context.cacheDir
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val destinationFile = File(downloadDir, appItem.appData.apk.fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        try {
            val request = Request.Builder().url(appItem.apkFullUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                emit(DownloadStatus.Error("Impossibile scaricare APK. HTTP ${response.code}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(DownloadStatus.Error("Download fallito: Risposta vuota"))
                return@flow
            }

            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            var lastReportedPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    val percent = ((totalBytesRead * 100) / contentLength).toInt()
                    if (percent != lastReportedPercent) {
                        lastReportedPercent = percent
                        emit(DownloadStatus.Downloading(percent, totalBytesRead, contentLength))
                    }
                } else {
                    emit(DownloadStatus.Downloading(-1, totalBytesRead, 0))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            emit(DownloadStatus.Completed(destinationFile.absolutePath))
        } catch (e: Exception) {
            emit(DownloadStatus.Error("Errore durante il download: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}
