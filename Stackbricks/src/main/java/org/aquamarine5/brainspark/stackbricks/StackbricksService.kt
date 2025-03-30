package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

open class StackbricksService(
    private val context: Context,
    private val messageProvider: StackbricksMessageProvider,
    private val packageProvider: StackbricksPackageProvider,
    val state: StackbricksState
) {

    private var _manifest: StackbricksManifest? = null

    private var _package: StackbricksPackageFile? = null

    private suspend fun getManifest(): StackbricksManifest {
        return if (_manifest == null) {
            messageProvider.getManifest().apply {
                _manifest = this
            }
        } else {
            _manifest!!
        }
    }

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
        val updateMessage = getManifest().latestStable
        return if (currentVersion < updateMessage.versionCode) updateMessage else null
    }

    open suspend fun isBetaVersionAvailable(): StackbricksVersionData? {
        val latestTest = getManifest().latestTest
        return if (getCurrentVersion() < latestTest.versionCode) latestTest else null
    }

    open suspend fun downloadPackage(
        isStable: Boolean = true,
        withProgress: Boolean = true
    ): StackbricksPackageFile {
        return packageProvider.downloadPackage(
            context,
            if (isStable) getManifest().latestStable else getManifest().latestTest,
            state.downloadingProgress
        ).apply {
            _package = this
        }
    }

    open suspend fun isNewerVersion(): Boolean {
        return isNeedUpdate() != null
    }

    open suspend fun getLatestPackageInfo(): StackbricksVersionData {
        return messageProvider.getLatestVersionData()
    }

    fun installPackage(packageFile: StackbricksPackageFile) {
        packageFile.installPackage(context)
    }

    open suspend fun downloadAndInstallPackage(versionData: StackbricksVersionData) {
        val packageFile = downloadPackage()
        installPackage(packageFile)
    }

    open suspend fun updateIfAvailable() {
        isNeedUpdate()?.let {
            downloadAndInstallPackage(it)
        }
    }
}