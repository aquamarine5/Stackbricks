package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.icu.util.VersionInfo
import okhttp3.OkHttpClient

class StackbricksService(val context: Context, val msgPvderId: String, val msgPvderData: String) {
    companion object {
        val okHttpClient = OkHttpClient()
    }

    var mUpdatePackage: ExceptionalResult<UpdatePackage> = ExceptionalResult.fail(
        NullPointerException("Should not use this value"),
        "Should not use this value"
    )
    var mUpdateMessage: ExceptionalResult<UpdateMessage> = ExceptionalResult.fail(
        NullPointerException("Should not use this value"),
        "Should not use this value"
    )

    fun getMsgPvder(): IMsgPvder? {
        return MsgPvderManager.parseFromId(msgPvderId)
    }

    fun getPkgPvder(pkgPvderId: String): IPkgPvder? {
        return PkgPvderManager.parseFromId(pkgPvderId)
    }

    suspend fun checkUpdate(): ExceptionalResult<Boolean> {
        val currentVersion = VersionInfo.getInstance(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName
        )
        val updateMessage = getUpdateMessage()
        return if (updateMessage.isSuccess) {
            ExceptionalResult.success(updateMessage.result!!.version > currentVersion)
        } else {
            ExceptionalResult.fail(updateMessage)
        }
    }

    suspend fun updateWhenAvailable(): ExceptionalResult<StackbricksStatus> {
        val checkResult = checkUpdate()
        return if (checkResult.isSuccess) {
            if (checkResult.result!!) {
                val updatePackage = getUpdatePackage()
                if (updatePackage.isSuccess) {
                    updatePackage.result!!.installApk(context)
                    ExceptionalResult.success(StackbricksStatus.STATUS_NEWEST)
                } else {
                    ExceptionalResult.fail(updatePackage)
                }
            } else ExceptionalResult.success(StackbricksStatus.STATUS_NEWEST)
        } else {
            ExceptionalResult.fail(checkResult)
        }

    }

    suspend fun getUpdatePackage(): ExceptionalResult<UpdatePackage> {
        return if (mUpdatePackage.isSuccess)
            mUpdatePackage
        else {
            val updateMessage = getUpdateMessage()
            if (updateMessage.isSuccess) {

                mUpdatePackage = getPkgPvder(updateMessage.result!!.pkgPvderId)!!
                    .downloadPackage(
                        context,
                        updateMessage.result,
                        updateMessage.result.pkgPvderData
                    )
                mUpdatePackage
            } else {
                ExceptionalResult.fail(updateMessage)
            }
        }
    }

    suspend fun getUpdateMessage(): ExceptionalResult<UpdateMessage> {
        return if (mUpdateMessage.isSuccess)
            mUpdateMessage
        else {
            mUpdateMessage = getMsgPvder()!!.getUpdateMessage(msgPvderData)
            mUpdateMessage
        }
    }
}