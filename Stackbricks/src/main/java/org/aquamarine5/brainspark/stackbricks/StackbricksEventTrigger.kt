package org.aquamarine5.brainspark.stackbricks

abstract class StackbricksEventTrigger {
    abstract fun onCheckUpdate(isTestChannel: Boolean = false)
    abstract fun onDownloadPackage(isTestChannel: Boolean = false)
    abstract fun onInstallPackage(isTestChannel: Boolean = false)
}