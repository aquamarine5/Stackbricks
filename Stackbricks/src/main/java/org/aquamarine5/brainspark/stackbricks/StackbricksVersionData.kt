package org.aquamarine5.brainspark.stackbricks

import java.time.Instant
import java.util.Date

interface StackbricksVersionData {
    val packageName: String
    val versionCode: Int
    val versionName: String
    val downloadFilename: String
    val releaseDate: Instant
}
