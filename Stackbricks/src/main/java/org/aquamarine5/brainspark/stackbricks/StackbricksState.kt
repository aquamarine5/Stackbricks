/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuVersionData
import java.io.File
import java.time.Instant

@Stable
data class StackbricksState(
    val status: MutableState<StackbricksStatus> = mutableStateOf(StackbricksStatus.STATUS_START),
    val downloadingProgress: MutableState<Float?> = mutableStateOf(null),
    var tmpPackage: StackbricksPackageFile? = null,
    val tmpVersion: MutableState<StackbricksVersionData?> = mutableStateOf(null)
) {
    companion object {
        val Saver: Saver<StackbricksState, *> = listSaver(
            save = {
                @Suppress("Deprecation")
                listOf(
                    it.status.value,
                    it.downloadingProgress.value,
                    it.tmpPackage != null,
                    it.tmpPackage?.file?.absolutePath,
                    it.tmpPackage?.isStable,
                    it.tmpVersion.value != null,
                    it.tmpVersion.value?.versionCode,
                    it.tmpVersion.value?.versionName,
                    it.tmpVersion.value?.downloadFilename,
                    it.tmpVersion.value?.isStable,
                    it.tmpVersion.value?.changelog,
                    it.tmpVersion.value?.forceInstall,
                    it.tmpVersion.value?.releaseDate?.epochSecond,
                    it.tmpVersion.value?.packageName,
                    it.tmpVersion.value?.forceInstallLessVersion
                )
            },
            restore = {
                val hasRestoredPackage = it[2] as Boolean
                val restoredStatus = it[0] as StackbricksStatus
                val restoredProgress = it[1] as Float?
                val (status, progress) = when {
                    restoredStatus == StackbricksStatus.STATUS_DOWNLOADING && hasRestoredPackage ->
                        StackbricksStatus.STATUS_CLICK_INSTALL to restoredProgress
                    restoredStatus == StackbricksStatus.STATUS_DOWNLOADING ||
                            restoredStatus == StackbricksStatus.STATUS_CHECKING ->
                        StackbricksStatus.STATUS_START to null
                    else -> restoredStatus to restoredProgress
                }
                StackbricksState(
                    mutableStateOf(status),
                    mutableStateOf(progress),
                    if (hasRestoredPackage) StackbricksPackageFile(
                        File(it[3] as String),
                        it[4] as Boolean
                    ) else null,
                    if (it[5] as Boolean) mutableStateOf(
                        QiniuVersionData(
                            it[6] as Int,
                            it[7] as String,
                            it[8] as String,
                            Instant.ofEpochSecond(it[12] as Long),
                            it[13] as String,
                            it[10] as String,
                            it[11] as Boolean,
                            it[9] as Boolean,
                            it[14] as Long
                        )
                    ) else mutableStateOf(null)
                )
            }
        )
    }
}