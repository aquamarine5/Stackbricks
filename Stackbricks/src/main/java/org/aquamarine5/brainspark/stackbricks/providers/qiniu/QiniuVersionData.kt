package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData
import java.time.Instant
import java.util.Date

class QiniuVersionData(
    override val versionCode: Int,
    override val versionName: String,
    override val downloadFilename: String,
    override val releaseDate: Instant,
    override val packageName: String,
    val rawJson: JSONObject
) : StackbricksVersionData