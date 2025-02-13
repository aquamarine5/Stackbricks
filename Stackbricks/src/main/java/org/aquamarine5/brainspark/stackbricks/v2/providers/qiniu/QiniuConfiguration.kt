package org.aquamarine5.brainspark.stackbricks.v2.providers.qiniu

import okhttp3.OkHttpClient

data class QiniuConfiguration(
    val host: String,
    val configFilePath: String = "stackbricks_config_v1.json",
    val okHttpClient: OkHttpClient = OkHttpClient()
)
