package org.aquamarine5.brainspark.stackbricks

import java.time.Instant
import java.util.Date

interface StackbricksVersionData {
    val packageName: String
    val versionCode: Int
    val versionName: String
    val downloadFilename: String
    val releaseDate: Instant
    val changelog:String
    @Deprecated("Use forceInstallLessVersion instead, deprecated in manifestVersion>=3", ReplaceWith("forceInstallLessVersion>versionCode"))
    val forceInstall:Boolean
    val isStable:Boolean
    val forceInstallLessVersion:Int
}
