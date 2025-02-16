package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.time.Instant

class StackbricksState(
    var status: MutableState<StackbricksStatus> = mutableStateOf(StackbricksStatus.STATUS_START),
    private var localVersionData: StackbricksVersionData? = null,
    var packageFile: StackbricksPackageFile? = null,
    var checkUpdateTime: Instant? = null,
    var downloadingProgress: MutableState<Float?> = mutableStateOf(null)
) {
    var versionData: StackbricksVersionData?
        get() = localVersionData
        set(value) {
            localVersionData = value
            checkUpdateTime = Instant.now()
        }
}