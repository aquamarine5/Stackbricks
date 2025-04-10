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