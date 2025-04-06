package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.pm.PackageInfoCompat

open class StackbricksService(
    private val context: Context,
    private val messageProvider: StackbricksMessageProvider,
    private val packageProvider: StackbricksPackageProvider,
    val state: StackbricksState,
    private val checkCurrentVersionIsTest: (String, Long) -> Boolean = { versionName, _ ->
        versionName.contains("beta", true) ||
                versionName.contains("alpha", true) ||
                versionName.contains("rc", true)
    }
) {
    private var _status by state.status

    private var _progress by state.downloadingProgress

    private var _manifest: StackbricksManifest? = null

    private var _package: StackbricksPackageFile? = null

    private var _version: MutableState<StackbricksVersionData?> = mutableStateOf(null)

    val internalVersionData by _version

    @Stable
    open fun checkCurrentIsTestChannel(): Boolean {
        val versionName = getCurrentVersionName()
        val versionCode = getCurrentVersion()
        return if (versionName != null) {
            checkCurrentVersionIsTest(versionName, versionCode)
        } else {
            Log.w(
                "StackbricksService",
                "Version name is null, cannot check if current version is test channel"
            )
            false
        }
    }

    open suspend fun getManifest(): StackbricksManifest {
        return if (_manifest == null) {
            messageProvider.getManifest().apply {
                _manifest = this
            }
        } else {
            _manifest!!
        }
    }

    open suspend fun getPackage(): StackbricksPackageFile {
        return if (_package == null) {
            downloadPackage().apply {
                _package = this
            }
        } else _package!!
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
        _version.value = updateMessage
        return if (currentVersion < updateMessage.versionCode) updateMessage else null
    }

    open suspend fun isBetaVersionAvailable(): StackbricksVersionData? {
        val latestTest = getManifest().latestTest
        _version.value = latestTest
        return if (getCurrentVersion() < latestTest.versionCode) latestTest else null
    }

    open suspend fun downloadPackage(
        isStable: Boolean = true,
        withProgress: Boolean = true
    ): StackbricksPackageFile {
        if (_version.value == null)
            throw NullPointerException("Version data is null, please call isNeedUpdate() or isBetaVersionAvailable() first")
        return packageProvider.downloadPackage(
            context,
            _version.value!!,
            state.downloadingProgress
        ).apply {
            _package = this
        }
    }

    open suspend fun isNewerVersion(): Boolean {
        return isNeedUpdate() != null
    }

    open suspend fun getLatestPackageInfo(): StackbricksVersionData =
        getManifest().latestStable

    open suspend fun getBetaPackageInfo(): StackbricksVersionData =
        getManifest().latestTest

    suspend fun installPackage() {
        getPackage().installPackage(context)
    }
}