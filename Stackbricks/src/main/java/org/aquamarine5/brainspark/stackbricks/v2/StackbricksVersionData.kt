package org.aquamarine5.brainspark.stackbricks.v2

import java.net.URI
import java.net.URL
import java.util.Date

interface StackbricksVersionData{
    val versionCode: Int
    val versionName: String
    val downloadUrl: URL
    val releaseDate: Date
}
