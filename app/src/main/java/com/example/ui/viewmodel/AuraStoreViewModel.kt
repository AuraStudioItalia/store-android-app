package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.installer.ApkInstaller
import com.example.data.model.AuraAppItem
import com.example.data.model.AuthState
import com.example.data.model.DownloadStatus
import com.example.data.model.RepositoryInfo
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class AuraStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuraRepository(application)
    private val authRepository = AuthRepository()

    private val prefs = application.getSharedPreferences("aura_store_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("app_theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode).apply()
    }

    private val officialRepo = RepositoryInfo(
        id = "aura_official",
        name = "Aura Studio Italia",
        url = "https://github.com/AuraStudioItalia/aura-store.git",
        isOfficial = true
    )

    private val _repositories = MutableStateFlow<List<RepositoryInfo>>(loadSavedRepositories())
    val repositories: StateFlow<List<RepositoryInfo>> = _repositories.asStateFlow()

    private val _showAddRepoDialog = MutableStateFlow(false)
    val showAddRepoDialog: StateFlow<Boolean> = _showAddRepoDialog.asStateFlow()

    fun openAddRepoDialog() {
        _showAddRepoDialog.value = true
    }

    fun dismissAddRepoDialog() {
        _showAddRepoDialog.value = false
    }

    private fun loadSavedRepositories(): List<RepositoryInfo> {
        val savedString = prefs.getString("external_repositories_list", null)
        val list = mutableListOf(officialRepo)
        if (!savedString.isNullOrBlank()) {
            savedString.split(";;").forEach { item ->
                val parts = item.split("||")
                if (parts.size >= 3) {
                    list.add(
                        RepositoryInfo(
                            id = parts[0],
                            name = parts[1],
                            url = parts[2],
                            isOfficial = false
                        )
                    )
                }
            }
        }
        return list
    }

    private fun saveRepositories(repos: List<RepositoryInfo>) {
        val externalRepos = repos.filter { !it.isOfficial }
        val serialized = externalRepos.joinToString(";;") { "${it.id}||${it.name}||${it.url}" }
        prefs.edit().putString("external_repositories_list", serialized).apply()
    }

    fun addExternalRepository(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) return
        val newRepo = RepositoryInfo(
            id = "ext_${System.currentTimeMillis()}",
            name = name.trim(),
            url = url.trim(),
            isOfficial = false
        )
        val updated = _repositories.value + newRepo
        _repositories.value = updated
        saveRepositories(updated)
        _showAddRepoDialog.value = false

        val currentAuth = authState.value
        if (currentAuth is AuthState.LoggedIn) {
            uploadRepoToCloud(currentAuth.uid, newRepo)
        }

        loadApps()
    }

    fun removeExternalRepository(repoId: String) {
        if (repoId == "aura_official") return
        val updated = _repositories.value.filter { it.id != repoId }
        _repositories.value = updated
        saveRepositories(updated)

        val currentAuth = authState.value
        if (currentAuth is AuthState.LoggedIn) {
            deleteRepoFromCloud(currentAuth.uid, repoId)
        }

        loadApps()
    }

    private val _showSecurityNoticeDialog = MutableStateFlow(
        !prefs.getBoolean("first_time_launch_warning_shown", false)
    )
    val showSecurityNoticeDialog: StateFlow<Boolean> = _showSecurityNoticeDialog.asStateFlow()

    private val _allApps = MutableStateFlow<List<AuraAppItem>>(emptyList())
    val allApps: StateFlow<List<AuraAppItem>> = _allApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tutte")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _downloadStatusMap = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatusMap: StateFlow<Map<String, DownloadStatus>> = _downloadStatusMap.asStateFlow()

    private val _selectedApp = MutableStateFlow<AuraAppItem?>(null)
    val selectedApp: StateFlow<AuraAppItem?> = _selectedApp.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _showCreateAccountDialog = MutableStateFlow(false)
    val showCreateAccountDialog: StateFlow<Boolean> = _showCreateAccountDialog.asStateFlow()

    private val _showInstallPermissionDialog = MutableStateFlow(false)
    val showInstallPermissionDialog: StateFlow<Boolean> = _showInstallPermissionDialog.asStateFlow()

    val filteredApps: StateFlow<List<AuraAppItem>> = combine(
        _allApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, category ->
        apps.filter { app ->
            val isNotAuraStore = !app.isAuraStore()
            val matchesQuery = query.isBlank() ||
                    app.appData.apk.packageName.contains(query, ignoreCase = true) ||
                    app.folderName.contains(query, ignoreCase = true) ||
                    (app.appData.description?.contains(query, ignoreCase = true) == true)

            val matchesCategory = category == "Tutte" ||
                    (category == "Aggiornamenti" && app.hasUpdate) ||
                    (category == "Installate" && app.isInstalled) ||
                    app.category.equals(category, ignoreCase = true)

            isNotAuraStore && matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val updateApps: StateFlow<List<AuraAppItem>> = _allApps.combine(_allApps) { apps, _ ->
        apps.filter { it.hasUpdate && !it.isAuraStore() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installedApps: StateFlow<List<AuraAppItem>> = _allApps.combine(_allApps) { apps, _ ->
        apps.filter { it.isInstalled && !it.isAuraStore() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableApps: StateFlow<List<AuraAppItem>> = _allApps.combine(_allApps) { apps, _ ->
        apps.filter { !it.isInstalled && !it.isAuraStore() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun createFallbackAuraStoreItem(): AuraAppItem {
        val context = getApplication<Application>().applicationContext
        val pkgName = context.packageName
        val installedVer = ApkInstaller.getInstalledVersionName(context, pkgName) ?: "1.0.0"
        val installedCode = ApkInstaller.getInstalledVersionCode(context, pkgName)
        val isInstalled = ApkInstaller.isInstalled(context, pkgName)

        val appData = com.example.data.model.AuraAppData(
            name = "Aura Store",
            iconUrl = "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/AuraStore/icon.png",
            description = "Lo store ufficiale per le applicazioni e i servizi dell'ecosistema Aura Studio Italia.",
            version = "1.0.0",
            apk = com.example.data.model.ApkInfo(
                fileName = "AuraStore.apk",
                sizeMb = 12.5,
                packageName = pkgName
            )
        )

        return AuraAppItem(
            folderName = "AuraStore",
            appData = appData,
            iconFullUrl = "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/AuraStore/icon.png",
            apkFullUrl = "https://raw.githubusercontent.com/AuraStudioItalia/aura-store/main/repo/AuraStore/AuraStore.apk",
            isInstalled = isInstalled,
            installedVersionName = installedVer,
            installedVersionCode = installedCode,
            hasUpdate = false,
            isFeatured = true,
            category = "Utility",
            isOfficial = true,
            repoName = "Aura Studio Italia"
        )
    }

    val auraStoreAppItem: StateFlow<AuraAppItem?> = _allApps.combine(_allApps) { apps, _ ->
        apps.firstOrNull { it.isAuraStore() } ?: createFallbackAuraStoreItem()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createFallbackAuraStoreItem())

    init {
        loadApps()
        observeAuthStateAndSyncCloudRepos()
    }

    private fun observeAuthStateAndSyncCloudRepos() {
        viewModelScope.launch {
            authState.collect { state ->
                if (state is AuthState.LoggedIn) {
                    syncRepositoriesWithCloud(state.uid)
                }
            }
        }
    }

    private fun syncRepositoriesWithCloud(uid: String) {
        if (uid.isBlank()) return

        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("store")
                .child(uid)
                .child("repos")
                .get()
                .addOnSuccessListener { snapshot ->
                    val cloudRepos = mutableListOf<RepositoryInfo>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            val url = child.child("url").getValue(String::class.java) ?: ""
                            if (name.isNotBlank() && url.isNotBlank()) {
                                cloudRepos.add(RepositoryInfo(id = id, name = name, url = url, isOfficial = false))
                            }
                        }
                    }
                    mergeAndUploadRepos(uid, cloudRepos)
                }
                .addOnFailureListener {
                    syncRepositoriesWithFirestore(uid)
                }
        } catch (e: Exception) {
            syncRepositoriesWithFirestore(uid)
        }
    }

    private fun syncRepositoriesWithFirestore(uid: String) {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("store")
                .document(uid)
                .collection("repos")
                .get()
                .addOnSuccessListener { query ->
                    val cloudRepos = mutableListOf<RepositoryInfo>()
                    for (doc in query.documents) {
                        val id = doc.getString("id") ?: doc.id
                        val name = doc.getString("name") ?: ""
                        val url = doc.getString("url") ?: ""
                        if (name.isNotBlank() && url.isNotBlank()) {
                            cloudRepos.add(RepositoryInfo(id = id, name = name, url = url, isOfficial = false))
                        }
                    }
                    mergeAndUploadRepos(uid, cloudRepos)
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mergeAndUploadRepos(uid: String, cloudRepos: List<RepositoryInfo>) {
        val currentLocal = _repositories.value.filter { !it.isOfficial }
        val mergedMap = mutableMapOf<String, RepositoryInfo>()

        for (r in currentLocal) {
            mergedMap[r.id] = r
        }
        for (r in cloudRepos) {
            if (!mergedMap.containsKey(r.id)) {
                mergedMap[r.id] = r
            }
        }

        val finalExternalList = mergedMap.values.toList()
        val fullList = listOf(officialRepo) + finalExternalList

        if (_repositories.value != fullList) {
            _repositories.value = fullList
            saveRepositories(fullList)
            loadApps()
        }

        for (r in finalExternalList) {
            uploadRepoToCloud(uid, r)
        }
    }

    private fun uploadRepoToCloud(uid: String, repo: RepositoryInfo) {
        if (uid.isBlank() || repo.isOfficial) return
        val map = mapOf(
            "id" to repo.id,
            "name" to repo.name,
            "url" to repo.url
        )
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("store")
                .child(uid)
                .child("repos")
                .child(repo.id)
                .setValue(map)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("store")
                .document(uid)
                .collection("repos")
                .document(repo.id)
                .set(map)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteRepoFromCloud(uid: String, repoId: String) {
        if (uid.isBlank() || repoId == "aura_official") return
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("store")
                .child(uid)
                .child("repos")
                .child(repoId)
                .removeValue()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("store")
                .document(uid)
                .collection("repos")
                .document(repoId)
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.fetchApps(_repositories.value)
            result.onSuccess { apps ->
                _allApps.value = apps
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Errore nel caricamento del repository Aura."
            }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun selectApp(app: AuraAppItem) {
        _selectedApp.value = app
    }

    fun clearSelectedApp() {
        _selectedApp.value = null
    }

    fun downloadAndInstall(app: AuraAppItem) {
        val pkgName = app.appData.apk.packageName
        viewModelScope.launch {
            repository.downloadApk(app).collect { status ->
                val currentMap = _downloadStatusMap.value.toMutableMap()
                currentMap[pkgName] = status
                _downloadStatusMap.value = currentMap

                if (status is DownloadStatus.Completed) {
                    val apkFile = File(status.filePath)
                    val context = getApplication<Application>().applicationContext
                    if (!ApkInstaller.canInstallApk(context)) {
                        _showInstallPermissionDialog.value = true
                    }
                    val installed = ApkInstaller.installApk(context, apkFile)
                    if (!installed && !ApkInstaller.canInstallApk(context)) {
                        _showInstallPermissionDialog.value = true
                    }
                }
            }
        }
    }

    fun launchInstalledApp(packageName: String): Boolean {
        val context = getApplication<Application>().applicationContext
        return ApkInstaller.launchApp(context, packageName)
    }

    fun checkInstalledStatus() {
        val context = getApplication<Application>().applicationContext
        val updated = ApkInstaller.checkAndEnrichApps(context, _allApps.value)
        _allApps.value = updated
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        authRepository.signInWithEmail(email, pass, onResult)
    }

    fun proceedIncognito() {
        authRepository.setIncognitoMode()
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun openCreateAccountDialog() {
        _showCreateAccountDialog.value = true
    }

    fun dismissCreateAccountDialog() {
        _showCreateAccountDialog.value = false
    }

    fun dismissSecurityNoticeDialog() {
        prefs.edit().putBoolean("first_time_launch_warning_shown", true).apply()
        _showSecurityNoticeDialog.value = false
    }

    fun openInstallPermissionSettings() {
        val context = getApplication<Application>().applicationContext
        ApkInstaller.openInstallPermissionSettings(context)
        _showInstallPermissionDialog.value = false
    }

    fun dismissInstallPermissionDialog() {
        _showInstallPermissionDialog.value = false
    }
}
