package org.aquamarine5.brainspark.stackbricks.v2

interface StackbricksMessageProvider {
    suspend fun getLatestVersionData(): StackbricksVersionData
}