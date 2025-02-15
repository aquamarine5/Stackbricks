package org.aquamarine5.brainspark.stackbricks.v2

import java.util.Date

interface StackbricksVersionData {
    val packageName: String
    val versionCode: Int
    val versionName: String
    val downloadFilename: String
    val releaseDate: Date
}
