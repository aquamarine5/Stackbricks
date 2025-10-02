package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import android.util.Log
import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.aquamarine5.brainspark.stackbricks.NoAvailableManifestException
import org.aquamarine5.brainspark.stackbricks.StackbricksManifest
import org.aquamarine5.brainspark.stackbricks.StackbricksMessageProvider
import org.aquamarine5.brainspark.stackbricks.StackbricksUnsupportedConfigException
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData
import java.net.URL
import java.time.Instant

class QiniuMessageProvider(
    private val configuration: QiniuConfiguration
) : StackbricksMessageProvider {
    companion object {
        const val LOGTAG = "QiniuMessageProvider"
        const val SUPPORTED_PARSE_CONFIG_VERSION = 3
    }

    override suspend fun getManifest(): QiniuManifest {
        configuration.possibleConfigurations.forEach {
            val configUrl =
                URL(if (configuration.isHttps) "https" else "http" + "://${it.first}/${it.second}")
            val req = Request.Builder()
                .get()
                .url(configUrl)
            configuration.referer?.let { referer ->
                req.addHeader("Referer", referer)
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    configuration.okHttpClient.newCall(req.build()).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException("Unexpected response code: ${response.request.url},${response.code}")
                        }
                        val body = response.body
                        val baseJson = JSONObject.parseObject(body.string())
                        val version = baseJson.getIntValue("manifestVersion")
                        val isVersionSupported =
                            SUPPORTED_PARSE_CONFIG_VERSION >= version
                        if (!isVersionSupported) {
                            Log.w(
                                LOGTAG,
                                "Unsupported config version: $version, maximum supported: $SUPPORTED_PARSE_CONFIG_VERSION"
                            )
                        } else {
                            if (version == 1) {
                                throw StackbricksUnsupportedConfigException(
                                    version,
                                    1
                                )
                            }
                        }
                        return@use QiniuManifest(
                            baseJson.getJSONObject("latestStable").run {
                                QiniuVersionData(
                                    getInteger("versionCode"),
                                    getString("versionName"),
                                    getString("downloadUrl"),
                                    Instant.ofEpochMilli(getLong("releaseDate")),
                                    getString("packageName"),
                                    getString("changelog"),
                                    getBoolean("forceInstall"),
                                    true,
                                    getInteger("forceInstallLessVersion") ?: -1
                                )
                            },
                            baseJson.getJSONObject("latestTest").run {
                                QiniuVersionData(
                                    getInteger("versionCode"),
                                    getString("versionName"),
                                    getString("downloadUrl"),
                                    Instant.ofEpochMilli(getLong("releaseDate")),
                                    getString("packageName"),
                                    getString("changelog"),
                                    getBoolean("forceInstall"),
                                    false,
                                    getInteger("forceInstallLessVersion") ?: -1
                                )
                            },
                            version
                        )
                    }
                }
            }.onSuccess { manifest ->
                return manifest
            }
        }
        throw NoAvailableManifestException()
    }

    @Deprecated(
        "Use getManifest().latestTest instead",
        ReplaceWith("StackbricksService.getManifest().latestTest")
    )
    override suspend fun getLatestTestVersionData(): StackbricksVersionData {
        return getManifest().latestTest
    }

    @Deprecated(
        "Use getManifest().latestStable instead",
        ReplaceWith("StackbricksService.getManifest().latestStable")
    )
    override suspend fun getLatestVersionData(): StackbricksVersionData {
        return getManifest().latestStable
    }
}