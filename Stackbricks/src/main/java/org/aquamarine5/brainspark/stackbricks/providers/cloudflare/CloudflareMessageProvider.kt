/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.cloudflare

import android.util.Log
import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.aquamarine5.brainspark.stackbricks.NoAvailableManifestException
import org.aquamarine5.brainspark.stackbricks.StackbricksManifestJsonHelper
import org.aquamarine5.brainspark.stackbricks.StackbricksMessageProvider
import org.aquamarine5.brainspark.stackbricks.StackbricksUnsupportedConfigException
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData

class CloudflareMessageProvider(
    private val configuration: CloudflareConfiguration
) : StackbricksMessageProvider {
    companion object {
        const val LOGTAG = "CloudflareMessageProvider"
        const val SUPPORTED_PARSE_CONFIG_VERSION = 4
        const val CURRENT_LATEST_PARSE_CONFIG_VERSION = 4
    }

    override suspend fun getManifest(): CloudflareManifest {
        val exceptions = mutableListOf<Throwable>()
        configuration.possibleConfigurations.forEach {
            val configUrl =
                "${if (configuration.isHttps) "https" else "http"}://${it.first}/${it.second}"
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
                            throw IllegalStateException(
                                "Unexpected response code: ${response.request.url},${response.code}"
                            )
                        }
                        val baseJson = JSONObject.parseObject(response.body.string())
                        val version = baseJson.getIntValue("manifestVersion")
                        if (version < SUPPORTED_PARSE_CONFIG_VERSION) {
                            throw StackbricksUnsupportedConfigException(
                                version,
                                CURRENT_LATEST_PARSE_CONFIG_VERSION
                            )
                        }
                        if (version > CURRENT_LATEST_PARSE_CONFIG_VERSION) {
                            Log.w(
                                LOGTAG,
                                "Unsupported config version: $version, maximum supported: $CURRENT_LATEST_PARSE_CONFIG_VERSION"
                            )
                        }
                        return@use CloudflareManifest(
                            parseVersionData(baseJson.getJSONObject("latestStable"), isStable = true),
                            parseVersionData(baseJson.getJSONObject("latestTest"), isStable = false),
                            version
                        )
                    }
                }
            }.onSuccess { manifest ->
                return manifest
            }.onFailure {
                it.printStackTrace()
                exceptions.add(it)
            }
        }
        throw NoAvailableManifestException(exceptions)
    }

    @Suppress("DEPRECATION")
    private fun parseVersionData(json: JSONObject, isStable: Boolean): CloudflareVersionData {
        val fields = StackbricksManifestJsonHelper.parseVersionFields(json)
        if (fields.possibleDownloadPath.isEmpty())
            throw IllegalStateException(
                "${LOGTAG}: version ${fields.versionName} declares no possibleDownloadPath"
            )
        return CloudflareVersionData(
            fields.versionCode,
            fields.versionName,
            fields.possibleDownloadPath,
            fields.releaseDate,
            fields.packageName,
            fields.changelog,
            fields.forceInstall,
            isStable,
            fields.forceInstallLessVersion
        )
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