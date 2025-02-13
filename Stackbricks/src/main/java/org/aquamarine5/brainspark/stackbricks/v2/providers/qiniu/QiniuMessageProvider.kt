package org.aquamarine5.brainspark.stackbricks.v2.providers.qiniu

import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.aquamarine5.brainspark.stackbricks.v2.StackbricksMessageProvider
import org.aquamarine5.brainspark.stackbricks.v2.StackbricksVersionData
import java.net.URI
import java.net.URL
import java.util.Date

class QiniuMessageProvider(
    private val configuration: QiniuConfiguration
) : StackbricksMessageProvider {
    companion object {
        const val TAG = "QiniuMessageProvider"
    }

    override suspend fun getLatestVersionData(): QiniuVersionData {
        val configUrl = URL("http://${configuration.host}/${configuration.configFilePath}")
        val req = Request.Builder()
            .url(configUrl)
            .build()
        return withContext(Dispatchers.IO) {
            configuration.okHttpClient.newCall(req).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Unexpected response code: ${response.code}")
                }
                val body = response.body ?: throw IllegalStateException("Empty response body")
                val rawJson = JSONObject.parseObject(body.string())
                val versionCode = rawJson.getIntValue("versionCode")
                val versionName = rawJson.getString("versionName")
                val downloadUrl = URL(rawJson.getString("downloadUrl"))
                val releaseDate = Date(rawJson.getLongValue("releaseDate"))
                return@withContext QiniuVersionData(
                    versionCode,
                    versionName,
                    downloadUrl,
                    releaseDate,
                    rawJson
                )
            }
        }
    }
}