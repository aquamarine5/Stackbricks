package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import java.io.File
import java.io.Serializable
import java.time.Instant

class StackbricksState(
    var status: MutableState<StackbricksStatus> = mutableStateOf(StackbricksStatus.STATUS_START),
    private var localVersionData: StackbricksVersionData? = null,
    var packageFile: StackbricksPackageFile? = null,
    var checkUpdateTime: Instant? = null,
    var downloadingProgress: MutableState<Float?> = mutableStateOf(null)
) {

    private class BuiltinStackbricksVersionData(
        override val versionCode: Int,
        override val versionName: String,
        override val downloadFilename: String,
        override val releaseDate: Instant,
        override val packageName: String
    ) : StackbricksVersionData, Serializable

    companion object {
        val Saver: Saver<MutableState<StackbricksState>, *> = listSaver(
            save = { saver ->
                saver.value.let {
                    listOf(
                        it.status.value,
                        it.localVersionData == null,
                        it.packageFile?.file?.absolutePath,
                        it.checkUpdateTime,
                        it.downloadingProgress.value,
                        it.localVersionData?.versionCode,
                        it.localVersionData?.versionName,
                        it.localVersionData?.downloadFilename,
                        it.localVersionData?.releaseDate,
                        it.localVersionData?.packageName,
                    )
                }
            },
            restore = {
                mutableStateOf(
                    StackbricksState(
                        status = mutableStateOf(it[0] as StackbricksStatus),
                        localVersionData = if (it[1] as Boolean) null else BuiltinStackbricksVersionData(
                            it[5] as Int,
                            it[6] as String,
                            it[7] as String,
                            it[8] as Instant,
                            it[9] as String
                        ),
                        packageFile = (it[2] as String?)?.let { StackbricksPackageFile(File(it)) },
                        checkUpdateTime = it[3] as Instant?,
                        downloadingProgress = mutableStateOf(it[4] as Float?)
                    )
                )
            }
        )
    }

    var versionData: StackbricksVersionData?
        get() = localVersionData
        set(value) {
            localVersionData = value
            checkUpdateTime = Instant.now()
        }
}