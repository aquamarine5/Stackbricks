package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import okhttp3.OkHttpClient

data class QiniuConfiguration(
    val host: String,
    val configFilePath: String = "stackbricks_config_v1.json",
    val okHttpClient: OkHttpClient = OkHttpClient(),
    val referer: String? = null
)
