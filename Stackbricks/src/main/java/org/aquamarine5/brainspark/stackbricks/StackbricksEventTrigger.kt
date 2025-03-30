package org.aquamarine5.brainspark.stackbricks

abstract class StackbricksEventTrigger {
    abstract fun onCheckUpdate()
    abstract fun onDownloadTestVersion()
    abstract fun onDownloadStableVersion()
    abstract fun onInstallPackage()
}