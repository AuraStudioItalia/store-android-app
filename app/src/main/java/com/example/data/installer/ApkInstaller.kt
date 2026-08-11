package com.example.data.installer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.data.model.AuraAppItem
import java.io.File

object ApkInstaller {

    fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledVersionName(context: Context, packageName: String): String? {
        return try {
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            null
        }
    }

    fun getInstalledVersionCode(context: Context, packageName: String): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun canInstallApk(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (!canInstallApk(context)) {
            openInstallPermissionSettings(context)
            return false
        }

        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun checkAndEnrichApps(context: Context, apps: List<AuraAppItem>): List<AuraAppItem> {
        return apps.map { app ->
            val pkg = app.appData.apk.packageName
            val installed = isInstalled(context, pkg)
            val instVerName = getInstalledVersionName(context, pkg)
            val instVerCode = getInstalledVersionCode(context, pkg)
            
            val repoVer = app.appData.version
            var hasUpd = false
            if (installed && instVerName != null) {
                hasUpd = isVersionNewer(repoVersion = repoVer, installedVersion = instVerName)
            }

            app.copy(
                isInstalled = installed,
                installedVersionName = instVerName,
                installedVersionCode = instVerCode,
                hasUpdate = hasUpd
            )
        }
    }

    private fun isVersionNewer(repoVersion: String, installedVersion: String): Boolean {
        if (repoVersion.trim() == installedVersion.trim()) return false
        val repoParts = repoVersion.split(".").mapNotNull { it.toIntOrNull() }
        val instParts = installedVersion.split(".").mapNotNull { it.toIntOrNull() }
        
        if (repoParts.isNotEmpty() && instParts.isNotEmpty()) {
            val maxLength = maxOf(repoParts.size, instParts.size)
            for (i in 0 until maxLength) {
                val repoVal = repoParts.getOrElse(i) { 0 }
                val instVal = instParts.getOrElse(i) { 0 }
                if (repoVal > instVal) return true
                if (repoVal < instVal) return false
            }
            return false
        }
        
        return repoVersion.compareTo(installedVersion, ignoreCase = true) > 0
    }
}
