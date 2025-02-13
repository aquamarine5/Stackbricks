package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.aquamarine5.brainspark.stackbricks.pkgpvder.GhproxyPkgPvder
import org.aquamarine5.brainspark.stackbricks.pkgpvder.Huang1111PkgPvder
import java.io.File


class PkgPvderManager {
    companion object {
        private val MsgPvderMatchDict = mapOf<String, IPkgPvder>(
            GhproxyPkgPvder.PkgPvderID to GhproxyPkgPvder(),
            Huang1111PkgPvder.PkgPvderID to Huang1111PkgPvder()
        )

        fun parseFromId(msgPvderId: String): IPkgPvder? {
            return MsgPvderMatchDict[msgPvderId]
        }
    }
}

data class UpdatePackage(val apkFile: File) {
    fun installApk(context: Context) {
        Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".file_provider",
                    apkFile
                ), "application/vnd.android.package-archive"
            )
            context.startActivity(this)
        }
    }
}

interface IPkgPvder {
    val id: String
    suspend fun downloadPackage(
        context: Context,
        updateMessage: UpdateMessage,
        pkgPvderData: String
    ): ExceptionalResult<UpdatePackage>
}