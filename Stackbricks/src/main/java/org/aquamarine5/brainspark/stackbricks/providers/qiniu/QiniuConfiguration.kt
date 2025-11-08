/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import okhttp3.OkHttpClient

data class QiniuConfiguration(
    @Deprecated("Use possibleConfigurations instead")
    val host: String="",
    @Deprecated("Use possibleConfigurations instead")
    val configFilePath: String = "stackbricks_config_v1.json",
    val isHttps: Boolean = false,
    val possibleConfigurations: List<Pair<String, String>>,
    val okHttpClient: OkHttpClient = OkHttpClient(),
    val referer: String? = null
)
