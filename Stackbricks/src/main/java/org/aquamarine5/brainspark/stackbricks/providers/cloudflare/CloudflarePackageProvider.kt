/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.cloudflare

import android.content.Context
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okio.buffer
import okio.sink
import org.aquamarine5.brainspark.stackbricks.ProgressedResponseBody
import org.aquamarine5.brainspark.stackbricks.StackbricksPackageFile
import org.aquamarine5.brainspark.stackbricks.StackbricksPackageProvider
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData
import org.aquamarine5.brainspark.stackbricks.StackbricksDownloadPath
import java.io.File

class CloudflarePackageProvider(
    private val configuration: CloudflareConfiguration
) : StackbricksPackageProvider {
    companion object {
        const val TAG = "CloudflarePackageProvider"
    }

    override val authority: String = StackbricksDownloadPath.CLOUDFLARE_AUTHORITY

    override suspend fun downloadPackage(
        context: Context,
        versionData: StackbricksVersionData,
        downloadPath: StackbricksDownloadPath,
        downloadProgress: MutableState<Float?>?
    ): StackbricksPackageFile {
        val path = downloadPath as? CloudflareDownloadPath
            ?: throw IllegalArgumentException(
                "$TAG can only handle ${StackbricksDownloadPath.CLOUDFLARE_AUTHORITY} paths, got ${downloadPath.authority}"
            )
        val baseUrl = path.baseUrl ?: configuration.defaultBaseUrl
            ?: throw IllegalArgumentException(
                "${TAG}: ${path.filename} declares neither baseUrl nor accountId, and " +
                        "CloudflareConfiguration.defaultBaseUrl is not set"
            )
        val url = baseUrl.toHttpUrlOrNull()?.newBuilder()
            ?.addPathSegments(path.objectKey)
            ?.build()
            ?: throw IllegalArgumentException("${TAG}: \"$baseUrl\" is not a valid url")

        val req = Request.Builder().url(url).get()
        configuration.referer?.let { referer ->
            req.addHeader("Referer", referer)
        }
        return withContext(Dispatchers.IO) {
            configuration.okHttpClient.newCall(req.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Unexpected response code: ${response.code}, url: $url")
                }
                val body = response.body
                var lastPublishedProgress = 0f
                val progressedBody =
                    ProgressedResponseBody(body) { bytesRead, contentLength, isDone ->
                        val progress = when {
                            isDone -> 1f
                            contentLength > 0 ->
                                (bytesRead.toFloat() / contentLength).coerceIn(0f, 1f)

                            else -> null
                        }
                        if (progress != null &&
                            (progress == 1f || progress - lastPublishedProgress >= 0.01f)
                        ) {
                            lastPublishedProgress = progress
                            downloadProgress?.value = progress
                        }
                    }
                val file = File.createTempFile(
                    "stackbricks_temp_apks_cloudflare_${versionData.versionCode}",
                    ".apk",
                    context.cacheDir
                )
                file.sink().buffer().use { sink ->
                    sink.writeAll(progressedBody.source())
                }
                StackbricksPackageFile(file, versionData.isStable)
            }
        }
    }
}