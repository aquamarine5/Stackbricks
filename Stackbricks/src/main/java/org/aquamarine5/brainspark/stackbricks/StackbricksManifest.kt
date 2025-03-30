package org.aquamarine5.brainspark.stackbricks

interface StackbricksManifest {
    val latestStable: StackbricksVersionData
    val latestTest: StackbricksVersionData
    val manifestVersion: Int
}
