/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface StackbricksPackageProvider {
    suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadProgress: MutableState<Float?>? = null
    ): StackbricksPackageFile
}