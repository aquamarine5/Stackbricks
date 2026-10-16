/*
 * Copyright (c) 2025-2026, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.Immutable
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.providers.cloudflare.CloudflareDownloadPath
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuDownloadPath
import java.io.Serializable

@Immutable
interface StackbricksDownloadPath : Serializable {
    val filename: String
    val authority: String
    val additionalInformation: JSONObject

    companion object {
        const val QINIU_AUTHORITY = "org.aquamarine5.brainspark.stackbricks.downloadpath.qiniu"
        const val CLOUDFLARE_AUTHORITY = "org.aquamarine5.brainspark.stackbricks.downloadpath.cloudflare"

        fun fromJson(json: JSONObject): StackbricksDownloadPath {
            val filename = json.getString("filename")
                ?: throw IllegalArgumentException("downloadPath is missing \"filename\"")
            val additionalInformation = json.getJSONObject("additionalInformation") ?: JSONObject()
            return when (val authority = json.getString("authority")) {
                QINIU_AUTHORITY -> QiniuDownloadPath(filename, additionalInformation)
                CLOUDFLARE_AUTHORITY -> CloudflareDownloadPath(filename, additionalInformation)
                else -> throw IllegalArgumentException("Unknown downloadPath authority: $authority")
            }
        }

        fun listFromJson(json: JSONArray?): List<StackbricksDownloadPath> {
            if (json.isNullOrEmpty()) return emptyList()
            return json.indices.map { fromJson(json.getJSONObject(it)) }
        }
    }
}