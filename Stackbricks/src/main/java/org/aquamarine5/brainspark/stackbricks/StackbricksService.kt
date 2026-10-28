/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Immutable
open class StackbricksService(
    private val context: Context,
    private val possibleMessageProvider: List<StackbricksMessageProvider>,
    private val possiblePackageProvider: List<StackbricksPackageProvider>,
    val state: StackbricksState,
    val stackbricksPolicy: StackbricksPolicy? = null,
    private val checkCurrentVersionIsTest: (String, Long) -> Boolean = DEFAULT_TEST_CHANNEL_CHECK
) {
    @Deprecated(
        "Use the constructor taking possibleMessageProvider and possiblePackageProvider instead",
        ReplaceWith(
            "StackbricksService(context, listOf(messageProvider), listOf(packageProvider), state, stackbricksPolicy, checkCurrentVersionIsTest)",
            "org.aquamarine5.brainspark.stackbricks.StackbricksService"
        )
    )
    constructor(
        context: Context,
        messageProvider: StackbricksMessageProvider,
        packageProvider: StackbricksPackageProvider,
        state: StackbricksState,
        stackbricksPolicy: StackbricksPolicy? = null,
        checkCurrentVersionIsTest: (String, Long) -> Boolean = DEFAULT_TEST_CHANNEL_CHECK
    ) : this(
        context,
        listOf(messageProvider),
        listOf(packageProvider),
        state,
        stackbricksPolicy,
        checkCurrentVersionIsTest
    )

    companion object {
        internal val DEFAULT_TEST_CHANNEL_CHECK: (String, Long) -> Boolean = { versionName, _ ->
            versionName.contains("beta", true) ||
                    versionName.contains("alpha", true) ||
                    versionName.contains("rc", true)
        }
    }

    private var _manifest: StackbricksManifest? = null

    val internalVersionData by state.tmpVersion

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

    open suspend fun deleteTemp() {
        context.cacheDir.listFiles { _, name ->
            name.startsWith("stackbricks_temp_apks") && name.endsWith(".apk")
        }?.forEach { it.delete() }
    }

    open suspend fun getManifest(dismissCache: Boolean = false): StackbricksManifest {
        if (_manifest != null && !dismissCache) return _manifest!!
        val exceptions = mutableListOf<Throwable>()
        possibleMessageProvider.forEach { provider ->
            runCatching { provider.getManifest() }
                .onSuccess { manifest ->
                    _manifest = manifest
                    return manifest
                }
                .onFailure {
                    it.printStackTrace()
                    exceptions.add(it)
                }
        }
        throw NoAvailableManifestException(exceptions)
    }

    suspend fun getCurrentChangelog(): String {
        internalVersionData?.takeIf { getCurrentVersion() < it.versionCode }?.let {
            return it.changelog
        }
        if (checkCurrentIsTestChannel())
            return getManifest().latestTest.changelog
        return getManifest().latestStable.changelog
    }

    open suspend fun getPackage(): StackbricksPackageFile {
        return if (state.tmpPackage == null) {
            downloadPackage().apply {
                state.tmpPackage = this
            }
        } else state.tmpPackage!!
    }

    open fun getCurrentVersion(): Long {
        if (stackbricksPolicy?.versionCode != null)
            return stackbricksPolicy.versionCode
        return PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )
    }

    open fun getCurrentVersionName(): String? {
        if (stackbricksPolicy?.versionName != null)
            return stackbricksPolicy.versionName
        return context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).versionName
    }

    open suspend fun isNeedUpdate(dismissCache: Boolean = false): StackbricksVersionData? {
        val currentVersion = getCurrentVersion()
        val updateMessage = getManifest(dismissCache).latestStable
        state.tmpVersion.value = updateMessage
        if (dismissCache) delay(300.milliseconds)
        return if (currentVersion < updateMessage.versionCode) updateMessage else null
    }

    open suspend fun isBetaVersionAvailable(dismissCache: Boolean = false): StackbricksVersionData? {
        val latestTest = getManifest(dismissCache).latestTest
        isNeedUpdate()?.let {
            return it
        }
        state.tmpVersion.value = latestTest
        if (dismissCache) delay(300.milliseconds)
        return if (getCurrentVersion() < latestTest.versionCode) latestTest else null
    }

    open suspend fun downloadPackage(
        withProgress: Boolean = true
    ): StackbricksPackageFile {
        val versionData = state.tmpVersion.value
            ?: throw NullPointerException(
                "Version data is null, please call isNeedUpdate() or isBetaVersionAvailable() first"
            )
        val exceptions = mutableListOf<Throwable>()
        versionData.possibleDownloadPath.forEach { downloadPath ->
            val provider = possiblePackageProvider
                .firstOrNull { it.authority == downloadPath.authority }
            if (provider == null) {
                exceptions.add(
                    IllegalStateException(
                        "No package provider registered for authority ${downloadPath.authority}"
                    )
                )
                return@forEach
            }
            runCatching {
                provider.downloadPackage(
                    context,
                    versionData,
                    downloadPath,
                    if (withProgress) state.downloadingProgress else null
                )
            }.onSuccess { packageFile ->
                state.tmpPackage = packageFile
                return packageFile
            }.onFailure {
                it.printStackTrace()
                exceptions.add(it)
            }
        }
        throw NoAvailablePackageException(exceptions)
    }

    open suspend fun isNewerVersion(dismissCache: Boolean = false): Boolean {
        return isNeedUpdate(dismissCache) != null
    }

    open suspend fun getLatestPackageInfo(): StackbricksVersionData =
        getManifest().latestStable

    open suspend fun getBetaPackageInfo(): StackbricksVersionData =
        getManifest().latestTest

    suspend fun installPackage(): Boolean {
        getPackage().run {
            installPackage(context)
            return this.isStable
        }
    }
}