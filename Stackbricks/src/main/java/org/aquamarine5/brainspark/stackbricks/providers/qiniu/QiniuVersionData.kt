/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks.providers.qiniu

import com.alibaba.fastjson2.JSONObject
import org.aquamarine5.brainspark.stackbricks.StackbricksVersionData
import java.time.Instant
import java.util.Date

data class QiniuVersionData(
    override val versionCode: Int,
    override val versionName: String,
    override val downloadFilename: String,
    override val releaseDate: Instant,
    override val packageName: String,
    override val changelog: String,
    @Deprecated(
        "Use forceInstallLessVersion instead, deprecated in manifestVersion>=3",
        ReplaceWith("forceInstallLessVersion>versionCode")
    )
    override val forceInstall: Boolean,
    override val isStable: Boolean, override val forceInstallLessVersion: Int
) : StackbricksVersionData