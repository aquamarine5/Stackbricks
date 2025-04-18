/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

data class StackbricksPolicy(
    val versionName: String? = null,
    val versionCode: Long? = null,
    val isAllowedToDisableCheckUpdateOnLaunch: Boolean = true,
    val isForceInstallValueCallback: Boolean = true,
)