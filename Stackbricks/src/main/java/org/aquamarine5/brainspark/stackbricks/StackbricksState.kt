package org.aquamarine5.brainspark.stackbricks

import java.time.Instant

class StackbricksState(
    var status: StackbricksStatus = StackbricksStatus.STATUS_START,
    private var localVersionData: StackbricksVersionData? = null,
    var packageFile: StackbricksPackageFile? = null,
    var checkUpdateTime: Instant? = null,
    var downloadingProgress: Float? = null
) {
    var versionData: StackbricksVersionData?
        get() = localVersionData
        set(value) {
            localVersionData = value
            checkUpdateTime = Instant.now()
        }
}