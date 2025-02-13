package org.aquamarine5.brainspark.stackbricks.pkgpvder

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.buffer
import okio.sink
import org.aquamarine5.brainspark.stackbricks.ExceptionalResult
import org.aquamarine5.brainspark.stackbricks.IPkgPvder
import org.aquamarine5.brainspark.stackbricks.StackbricksService
import org.aquamarine5.brainspark.stackbricks.UpdateMessage
import org.aquamarine5.brainspark.stackbricks.UpdatePackage
import java.io.File

class GhproxyPkgPvder : IPkgPvder {
    override val id: String = PkgPvderID

    companion object {
        const val PkgPvderID = "stbkt.pkgpvder.ghproxy"
    }

    override suspend fun downloadPackage(
        context: Context,
        updateMessage: UpdateMessage,
        pkgPvderData: String
    ): ExceptionalResult<UpdatePackage> {
        try {
            val apkFile: File = withContext(Dispatchers.IO) {
                File.createTempFile(
                    "stackbricks_apk_${updateMessage.version}",
                    ".apk",
                    context.cacheDir
                )
            }
            val data = pkgPvderData.split("]]")
            val url =
                "https://mirror.ghproxy.com/github.com/${data[0]}/${data[1]}/releases/download/${data[2]}/${data[3]}"
            val req = Request.Builder()
                .url(url)
                .build()
            return withContext(Dispatchers.IO) {
                StackbricksService.okHttpClient.newCall(req).execute().apply {
                    if (body != null) {
                        apkFile.sink().buffer().apply {
                            writeAll(body!!.source())
                            close()
                        }
                    }
                }
                ExceptionalResult.success(UpdatePackage(apkFile))
            }
        } catch (ex: Exception) {
            return ExceptionalResult.fail(ex, "")
        }

    }
}
