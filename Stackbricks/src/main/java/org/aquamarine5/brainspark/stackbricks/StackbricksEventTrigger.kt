package org.aquamarine5.brainspark.stackbricks

abstract class StackbricksEventTrigger {
    abstract fun onCheckUpdate(isTestChannel: Boolean = false)
    abstract fun onDownloadPackage()
    abstract fun onInstallPackage(isTestChannel: Boolean = false,versionData: StackbricksVersionData)
    abstract fun onChannelChanged(isTestChannel: Boolean)
    abstract fun onCheckUpdateOnLaunchChanged(isChecked: Boolean)
}