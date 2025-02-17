package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import java.time.Instant
import java.time.temporal.ChronoUnit

class StackbricksStateService(
    private val context: Context,
    messageProvider: StackbricksMessageProvider,
    private val packageProvider: StackbricksPackageProvider,
    val state: StackbricksState
) : StackbricksService(context, messageProvider, packageProvider) {
    companion object {
        const val VERSION_DATA_AVAILABLE_DURATION = 5L
        const val TAG = "StackbricksStateService"
    }

    private val mState = state
    suspend fun isNewerVersion(): Boolean {
        val versionData = isNeedUpdate()

        return if (versionData == null) {
            false
        } else {
            mState.versionData = versionData
            true
        }
    }

    suspend fun getVersionData(): StackbricksVersionData {
        if (mState.versionData == null) {
            mState.versionData = getLatestPackageInfo()
        } else if (mState.checkUpdateTime?.plus(VERSION_DATA_AVAILABLE_DURATION, ChronoUnit.MINUTES)
                ?.isBefore(Instant.now()) == true
        ) {
            mState.versionData = getLatestPackageInfo()
            Log.i(TAG, "Version data is no longer available")
        }
        return mState.versionData!!
    }

    override suspend fun downloadPackage(versionData: StackbricksVersionData): StackbricksPackageFile {
        val packageFile = super.downloadPackage(versionData)
        mState.packageFile = packageFile
        return packageFile
    }

    suspend fun downloadPackage(): StackbricksPackageFile {
        return downloadPackage(getVersionData())
    }

    suspend fun downloadPackageWithProgress(versionData: StackbricksVersionData):StackbricksPackageFile{
        val packageFile = packageProvider.downloadPackage(context,versionData,state.downloadingProgress)
        mState.packageFile = packageFile
        return packageFile
    }

    fun installPackage(): Boolean {
        return mState.packageFile?.let {
            it.installPackage(context)
            return true
        } ?: false
    }

    override suspend fun getLatestPackageInfo(): StackbricksVersionData {
        val versionData = super.getLatestPackageInfo()
        mState.versionData = versionData
        return versionData
    }
}