package org.aquamarine5.brainspark.stackbricks

interface StackbricksMessageProvider {
    suspend fun getLatestVersionData(): StackbricksVersionData
}