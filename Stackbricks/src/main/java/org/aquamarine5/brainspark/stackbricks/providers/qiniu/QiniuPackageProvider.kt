package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.buffer
import okio.sink
import org.aquamarine5.brainspark.stackbricks.NoAvailableManifestException
import org.aquamarine5.brainspark.stackbricks.ProgressedResponseBody
import org.aquamarine5.brainspark.stackbricks.StackbricksPackageFile
import org.aquamarine5.brainspark.stackbricks.StackbricksPackageProvider
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData
import java.io.File

class QiniuPackageProvider(
    private val configuration: QiniuConfiguration
) : StackbricksPackageProvider {
    companion object {
        const val TAG = "QiniuPackageProvider"
    }

    override suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadProgress: MutableState<Float?>?
    ): StackbricksPackageFile {
        configuration.possibleConfigurations.forEach {
            val req = Request.Builder()
                .url("http://${it.first}/${versionData.downloadFilename}")
                .get()
            configuration.referer?.let { referer ->
                req.addHeader("Referer", referer)
            }
            return withContext(Dispatchers.IO) {
                configuration.okHttpClient.newCall(req.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("Unexpected response code: ${response.code}")
                    }
                    val body = response.body
                    val progressedBody =
                        ProgressedResponseBody(body) { bytesRead, contentLength, isDone ->
                            downloadProgress?.value =
                                if (isDone) 1f else (bytesRead / contentLength.toDouble()).toFloat()
                        }
                    val file = File.createTempFile(
                        "stackbricks_qiniu_${versionData.versionCode}",
                        ".apk",
                        context.cacheDir
                    )
                    file.sink().buffer().use { sink ->
                        sink.writeAll(progressedBody.source())
                    }
                    return@withContext StackbricksPackageFile(file, versionData.isStable)
                }
            }
        }
        throw NoAvailableManifestException()
    }
}