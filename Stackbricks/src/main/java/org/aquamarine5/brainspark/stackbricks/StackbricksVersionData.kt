/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.time.Instant

@Immutable
interface StackbricksVersionData : Serializable {
    val packageName: String
    val versionCode: Int
    val versionName: String
    val possibleDownloadPath: List<StackbricksDownloadPath>
    val releaseDate: Instant
    val changelog: String

    @Deprecated(
        "Use possibleDownloadPath instead, deprecated in manifestVersion>=4",
        ReplaceWith("possibleDownloadPath.first().filename")
    )
    val downloadFilename: String
        get() = possibleDownloadPath.firstOrNull()?.filename
            ?: throw IllegalStateException(
                "possibleDownloadPath of $versionName is empty, cannot resolve a download filename"
            )

    @Deprecated(
        "Use forceInstallLessVersion instead, deprecated in manifestVersion>=3",
        ReplaceWith("forceInstallLessVersion>versionCode")
    )
    val forceInstall: Boolean
    val isStable: Boolean
    val forceInstallLessVersion: Long
}
