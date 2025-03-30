package org.aquamarine5.brainspark.stackbricks

interface StackbricksMessageProvider {
    suspend fun getLatestVersionData(): StackbricksVersionData
    suspend fun getLatestTestVersionData(): StackbricksVersionData
    suspend fun getManifest(): StackbricksManifest
}