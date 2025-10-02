package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

open class StackbricksPackageFile(
    val file: File,
    val isStable: Boolean,
) {
    open fun installPackage(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.stackbricks.file_provider", file
            )
            Log.i("StackbricksPackageFile", "uri: $uri")
            setDataAndTypeAndNormalize(
                uri,
                "application/vnd.android.package-archive"
            )
        })
    }

    open fun clean() {
        file.delete()
    }
}
