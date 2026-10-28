/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import androidx.compose.runtime.Immutable
import org.aquamarine5.brainspark.stackbricks.StackbricksManifest

@Immutable
data class QiniuManifest(
    override val latestStable: QiniuVersionData,
    override val latestTest: QiniuVersionData,
    override val manifestVersion: Int
) : StackbricksManifest
