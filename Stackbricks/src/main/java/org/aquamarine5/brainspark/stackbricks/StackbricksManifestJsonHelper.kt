/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuDownloadPath
import java.time.Instant

internal object StackbricksManifestJsonHelper {
    fun parseDownloadPaths(json: JSONObject): List<StackbricksDownloadPath> =
        StackbricksDownloadPath.listFromJson(json.getJSONArray("possibleDownloadPath"))

    fun parseVersionFields(
        json: JSONObject,
        legacyDownloadUrl: String? = null
    ): ParsedVersionFields = ParsedVersionFields(
        json.getInteger("versionCode"),
        json.getString("versionName"),
        parseDownloadPaths(json).ifEmpty {
            legacyDownloadUrl?.let { listOf(QiniuDownloadPath(it)) } ?: emptyList()
        },
        Instant.ofEpochMilli(json.getLong("releaseDate")),
        json.getString("packageName"),
        json.getString("changelog"),
        json.getBoolean("forceInstall"),
        json.getLong("forceInstallLessVersion") ?: -1
    )
}

@Suppress("DEPRECATION")
internal data class ParsedVersionFields(
    val versionCode: Int,
    val versionName: String,
    val possibleDownloadPath: List<StackbricksDownloadPath>,
    val releaseDate: Instant,
    val packageName: String,
    val changelog: String,
    val forceInstall: Boolean,
    val forceInstallLessVersion: Long
)