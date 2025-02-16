package org.aquamarine5.brainspark.stackbricks

import android.content.Context

interface StackbricksPackageProvider {
    suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData
    ): StackbricksPackageFile
}