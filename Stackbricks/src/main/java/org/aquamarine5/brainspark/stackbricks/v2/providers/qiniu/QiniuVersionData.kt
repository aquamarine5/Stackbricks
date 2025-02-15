package org.aquamarine5.brainspark.stackbricks.v2.providers.qiniu

import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.v2.StackbricksVersionData
import java.net.URL
import java.util.Date

class QiniuVersionData(
    override val versionCode: Int,
    override val versionName: String,
    override val downloadFilename: String,
    override val releaseDate: Date,
    override val packageName: String,
    val rawJson: JSONObject
) : StackbricksVersionData