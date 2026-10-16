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
import com.alibaba.fastjson2.JSONObject
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
        private const val FIELD_SEPARATOR = '\u001F'
        private const val RECORD_SEPARATOR = '\u001E'

        private fun encodeDownloadPaths(paths: List<StackbricksDownloadPath>): String =
            paths.joinToString(RECORD_SEPARATOR.toString()) {
                listOf(
                    it.authority,
                    it.filename,
                    it.additionalInformation.toJSONString()
                ).joinToString(FIELD_SEPARATOR.toString())
            }

        private fun decodeDownloadPaths(encoded: String): List<StackbricksDownloadPath> {
            if (encoded.isEmpty()) return emptyList()
            return encoded.split(RECORD_SEPARATOR).mapNotNull { record ->
                val parts = record.split(FIELD_SEPARATOR)
                if (parts.size < 3) return@mapNotNull null
                runCatching {
                    StackbricksDownloadPath.fromJson(
                        JSONObject().apply {
                            put("authority", parts[0])
                            put("filename", parts[1])
                            put("additionalInformation", JSONObject.parseObject(parts[2]))
                        }
                    )
                }.getOrNull()
            }
        }

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
                    encodeDownloadPaths(it.tmpVersion.value?.possibleDownloadPath ?: emptyList()),
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
                val (status, progress) = when (restoredStatus) {
                    StackbricksStatus.STATUS_DOWNLOADING if hasRestoredPackage ->
                        StackbricksStatus.STATUS_CLICK_INSTALL to restoredProgress

                    StackbricksStatus.STATUS_DOWNLOADING, StackbricksStatus.STATUS_CHECKING ->
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
                            decodeDownloadPaths(it[8] as String),
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