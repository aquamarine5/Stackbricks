package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

open class StackbricksPackageFile(
    private val file: File
) {
    open fun installPackage(context: Context) {
        Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(
                FileProvider.getUriForFile(context, "${context.packageName}.file_provider", file),
                "application/vnd.android.package-archive"
            )
            context.startActivity(this)
        }
    }

    open fun clean(){
        file.delete()
    }
}
