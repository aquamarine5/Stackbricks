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
import org.json.JSONObject
import java.io.File

class Huang1111PkgPvder : IPkgPvder {
    override val id: String = PkgPvderID

    companion object {
        const val PkgPvderID = "stbkt.pkgpvder.huang1111"
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
            withContext(Dispatchers.IO) {
                StackbricksService.okHttpClient.newCall(Request.Builder().apply {
                    url("https://pan.huang1111.cn/api/v3/share/download/$pkgPvderData")
                    method("PUT",null)
                }.build()).execute().apply {
                    if (body != null) {
                        val data=JSONObject(body!!.string()).getString("data")
                        val response= StackbricksService.okHttpClient.newCall(Request.Builder().apply {
                            url(data)
                        }.build()).execute()
                        if(response.body!=null) {
                            apkFile.sink().buffer().apply {
                                writeAll(response.body!!.source())
                                close()
                            }
                        }
                    }
                }
            }
            return ExceptionalResult.success(UpdatePackage(apkFile))
        } catch (ex: Exception) {
            return ExceptionalResult.fail(ex, "")
        }

    }
}