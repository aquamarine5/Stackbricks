package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import org.aquamarine5.brainspark.stackbricks.StackbricksManifest

data class QiniuManifest(
    override val latestStable: QiniuVersionData,
    override val latestTest: QiniuVersionData,
    override val manifestVersion: Int
):StackbricksManifest
