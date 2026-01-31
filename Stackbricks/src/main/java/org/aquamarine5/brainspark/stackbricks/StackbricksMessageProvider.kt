/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuManifest
import kotlin.coroutines.Continuation

interface StackbricksMessageProvider {
    @Deprecated("Use getManifest().latestStable instead")
    suspend fun getLatestVersionData(): StackbricksVersionData

    @Deprecated("Use getManifest().latestTest instead")
    suspend fun getLatestTestVersionData(): StackbricksVersionData
    suspend fun getManifest(continuation: Continuation<QiniuManifest>?=null): StackbricksManifest
}