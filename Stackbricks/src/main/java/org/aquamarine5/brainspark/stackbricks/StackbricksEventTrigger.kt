/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

abstract class StackbricksEventTrigger {
    abstract fun onCheckUpdate(isTestChannel: Boolean = false)
    abstract fun onDownloadPackage()
    abstract fun onInstallPackage(
        isTestChannel: Boolean = false,
        versionData: StackbricksVersionData
    )

    abstract fun onChannelChanged(isTestChannel: Boolean)
    abstract fun onCheckUpdateOnLaunchChanged(isChecked: Boolean)
}