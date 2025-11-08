/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import kotlin.Exception

class StackbricksUnsupportedConfigException(
    configVersion: Int,
    supportedVersion: Int,
    e: Throwable? = null
) :
    UnsupportedOperationException(
        "Parse Stackbricks config failed, config version: $configVersion, supported maximum version: $supportedVersion; ${e?.message}",
        e
    )