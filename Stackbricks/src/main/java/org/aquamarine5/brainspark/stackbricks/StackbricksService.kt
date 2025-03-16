package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

open class StackbricksService(
    private val context: Context,
    private val messageProvider: StackbricksMessageProvider,
    private val packageProvider: StackbricksPackageProvider
) {
    companion object {
        const val TAG = "StackbricksService"
    }

    open fun getCurrentVersion(): Long {
        return PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )
    }

    open fun getCurrentVersionName(): String? {
        return context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).versionName
    }

    open suspend fun isNeedUpdate(): StackbricksVersionData? {
        val currentVersion = getCurrentVersion()
        val updateMessage = messageProvider.getLatestVersionData()
        Log.i(TAG,"currentVersion: $currentVersion, serverVersion: ${updateMessage.versionCode}")
        return if (currentVersion < updateMessage.versionCode) updateMessage else null
    }

    open suspend fun downloadPackage(versionData: StackbricksVersionData): StackbricksPackageFile {
        return packageProvider.downloadPackage(context, versionData)
    }

    open suspend fun getLatestPackageInfo(): StackbricksVersionData {
        return messageProvider.getLatestVersionData()
    }

    fun installPackage(packageFile: StackbricksPackageFile) {
        packageFile.installPackage(context)
    }

    open suspend fun downloadAndInstallPackage(versionData: StackbricksVersionData) {
        val packageFile = downloadPackage(versionData)
        installPackage(packageFile)
    }

    open suspend fun updateIfAvailable() {
        isNeedUpdate()?.let {
            downloadAndInstallPackage(it)
        }
    }
}