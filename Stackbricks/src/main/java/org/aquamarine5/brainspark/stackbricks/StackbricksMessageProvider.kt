package org.aquamarine5.brainspark.stackbricks

interface StackbricksMessageProvider {
    @Deprecated("Use getManifest().latestStable instead")
    suspend fun getLatestVersionData(): StackbricksVersionData

    @Deprecated("Use getManifest().latestTest instead")
    suspend fun getLatestTestVersionData(): StackbricksVersionData
    suspend fun getManifest(): StackbricksManifest
}