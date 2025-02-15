package org.aquamarine5.brainspark.stackbricks.v2

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

open class StackbricksService(
    private val context: Context,
    val messageProvider: StackbricksMessageProvider,
    val packageProvider: StackbricksPackageProvider
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


    open suspend fun downloadAndInstallPackage(versionData: StackbricksVersionData) {
        val packageFile = packageProvider.downloadPackage(context, versionData)
        packageFile.installPackage(context)
    }

    open suspend fun updateIfAvailable() {
        isNeedUpdate()?.let {
            downloadAndInstallPackage(it)
        }

    }
}