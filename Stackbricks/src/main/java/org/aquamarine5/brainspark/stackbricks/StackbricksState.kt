package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuVersionData
import java.io.File
import java.time.Instant

data class StackbricksState(
    var status: MutableState<StackbricksStatus> = mutableStateOf(StackbricksStatus.STATUS_START),
    var downloadingProgress: MutableState<Float?> = mutableStateOf(null),
    var tmpPackage: StackbricksPackageFile? = null,
    var tmpVersion: MutableState<StackbricksVersionData?> = mutableStateOf(null)
) {
    companion object {
        val Saver: Saver<StackbricksState, *> = listSaver(
            save = {
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
                    it.tmpVersion.value?.packageName
                )
            },
            restore = {
                StackbricksState(
                    mutableStateOf(it[0] as StackbricksStatus),
                    mutableStateOf(it[1] as Float?),
                    if (it[2] as Boolean) StackbricksPackageFile(
                        File(it[3] as String),
                        it[4] as Boolean
                    ) else null,
                    if(it[5] as Boolean) mutableStateOf(QiniuVersionData(
                        it[6] as Int,
                        it[7] as String,
                        it[8] as String,
                        Instant.ofEpochSecond(it[12] as Long),
                        it[13] as String,
                        it[10] as String,
                        it[11] as Boolean,
                        it[9] as Boolean,
                    ))else mutableStateOf(null)
                )
            }
        )
    }
}