/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.compose.runtime.MutableState

interface StackbricksPackageProvider {
    val authority: String

    suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadPath: StackbricksDownloadPath,
        downloadProgress: MutableState<Float?>? = null
    ): StackbricksPackageFile

    @Deprecated(
        "Use the overload taking an explicit StackbricksDownloadPath instead",
        ReplaceWith("downloadPackage(context, versionData, downloadPath, downloadProgress)")
    )
    suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadProgress: MutableState<Float?>? = null
    ): StackbricksPackageFile = downloadPackage(
        context,
        versionData,
        versionData.possibleDownloadPath.first { it.authority == authority },
        downloadProgress
    )
}