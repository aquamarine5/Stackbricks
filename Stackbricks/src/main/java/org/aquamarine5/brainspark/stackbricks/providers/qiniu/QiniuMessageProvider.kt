package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import android.util.Log
import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.aquamarine5.brainspark.stackbricks.StackbricksMessageProvider
import org.aquamarine5.brainspark.stackbricks.StackbricksUnsupportedConfigException
import java.net.URL
import java.time.Instant

class QiniuMessageProvider(
    private val configuration: QiniuConfiguration
) : StackbricksMessageProvider {
    companion object {
        const val LOGTAG = "QiniuMessageProvider"
        const val SUPPORTED_PARSE_CONFIG_VERSION = 1
    }

    override suspend fun getLatestVersionData(): QiniuVersionData {
        val configUrl = URL("http://${configuration.host}/${configuration.configFilePath}")
        val req = Request.Builder()
            .get()
            .url(configUrl)
        configuration.referer?.let {
            req.addHeader("Referer", it)
        }
        return withContext(Dispatchers.IO) {
            configuration.okHttpClient.newCall(req.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Unexpected response code: ${response.request.url},${response.code}")
                }
                val body = response.body ?: throw IllegalStateException("Empty response body")
                val baseJson = JSONObject.parseObject(body.string())
                val isVersionSupported =
                    SUPPORTED_PARSE_CONFIG_VERSION >= baseJson.getIntValue("@version")
                if (isVersionSupported) {
                    Log.w(
                        LOGTAG,
                        "Unsupported config version: ${baseJson.getIntValue("@version")}, maximum supported: $SUPPORTED_PARSE_CONFIG_VERSION"
                    )
                }
                try {
                    val rawJson = baseJson.getJSONObject("latest")
                    val versionCode = rawJson.getIntValue("versionCode")
                    val versionName = rawJson.getString("versionName")
                    val downloadUrl = rawJson.getString("downloadUrl")
                    val releaseDate = Instant.ofEpochMilli(rawJson.getLongValue("releaseDate"))
                    return@withContext QiniuVersionData(
                        versionCode,
                        versionName,
                        downloadUrl,
                        releaseDate,
                        rawJson.getString("packageName"),
                        rawJson
                    )
                } catch (e: Exception) {
                    if (isVersionSupported.not()) {
                        throw StackbricksUnsupportedConfigException(
                            baseJson.getIntValue("@version"),
                            SUPPORTED_PARSE_CONFIG_VERSION,
                            e
                        )
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}