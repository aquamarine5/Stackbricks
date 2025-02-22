package org.aquamarine5.brainspark.stackbricks

import kotlin.Exception

class StackbricksUnsupportedConfigException(
    configVersion: Int,
    supportedVersion: Int,
    e: Exception
) :
    UnsupportedOperationException(
        "Parse Stackbricks config failed, config version: $configVersion, supported maximum version: $supportedVersion; ${e.message}",
        e
    )