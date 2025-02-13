package org.aquamarine5.brainspark.stackbricks.v2

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

class StackbricksService(
    private val context: Context,
    val messageProvider: StackbricksMessageProvider,
    val packageProvider: StackbricksPackageProvider
) {
    companion object {
        const val TAG = "StackbricksService"
    }

    suspend fun isNeedUpdate(): StackbricksVersionData? {
        val currentVersion = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )

        val updateMessage = messageProvider.getLatestVersionData()
        return if (currentVersion < updateMessage.versionCode) updateMessage else null
    }


    suspend fun downloadAndInstallPackage(versionData: StackbricksVersionData) {
        val packageFile = packageProvider.downloadPackage(context, versionData)
        packageFile.installPackage(context)
    }

    suspend fun updateIfAvailable() {
        isNeedUpdate()?.let {
            downloadAndInstallPackage(it)
        }

    }
}