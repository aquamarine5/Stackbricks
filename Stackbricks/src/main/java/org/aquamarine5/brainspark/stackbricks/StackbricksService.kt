package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

open class StackbricksService(
    private val context: Context,
    private val messageProvider: StackbricksMessageProvider,
    private val packageProvider: StackbricksPackageProvider
) {
    companion object {
        const val TAG = "StackbricksService"
    }

    open suspend fun isNeedUpdate(): StackbricksVersionData? {
        val currentVersion = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )
        val updateMessage = messageProvider.getLatestVersionData()
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