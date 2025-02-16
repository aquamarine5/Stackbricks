package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.compose.runtime.MutableState

interface StackbricksPackageProvider {
    suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadProgress:MutableState<Float>? = null
    ): StackbricksPackageFile
}