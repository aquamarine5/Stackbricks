/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.cloudflare

import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.StackbricksDownloadPath

data class CloudflareDownloadPath(
    override val filename: String,
    override val additionalInformation: JSONObject = JSONObject()
) : StackbricksDownloadPath {
    override val authority: String get() = StackbricksDownloadPath.CLOUDFLARE_AUTHORITY

    val baseUrl: String? get() = additionalInformation.getString("baseUrl")
    val accountId: String? get() = additionalInformation.getString("accountId")
    val bucketName: String? get() = additionalInformation.getString("bucketName")
    val objectKey: String get() = additionalInformation.getString("objectKey") ?: filename
}