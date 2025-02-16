package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.buffer
import okio.sink
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
        versionData: StackbricksVersionData
    ): StackbricksPackageFile {
        val req = Request.Builder()
            .url("http://${configuration.host}/${versionData.downloadFilename}")
            .get()
        configuration.referer?.let {
            req.addHeader("Referer",it)
        }
        return withContext(Dispatchers.IO) {
            configuration.okHttpClient.newCall(req.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Unexpected response code: ${response.code}")
                }
                val body = response.body ?: throw IllegalStateException("Empty response body")
                val file = File.createTempFile(
                    "stackbricks_qiniu_${versionData.versionCode}",
                    ".apk",
                    context.cacheDir
                )
                file.sink().buffer().use { sink ->
                    sink.writeAll(body.source())
                }
                return@withContext StackbricksPackageFile(file)
            }
        }
    }
}