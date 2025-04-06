package org.aquamarine5.brainspark.stackbricks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import java.io.File
import java.io.Serializable
import java.time.Instant

data class StackbricksState(
    var status: MutableState<StackbricksStatus> = mutableStateOf(StackbricksStatus.STATUS_START),
    var downloadingProgress: MutableState<Float?> = mutableStateOf(null)
){
    companion object{
        val Saver: Saver<StackbricksState, *> = listSaver(
            save = {
                listOf(
                    it.status.value,
                    it.downloadingProgress.value
                )
            },
            restore = {
                StackbricksState(
                    mutableStateOf(it[0] as StackbricksStatus),
                    mutableStateOf(it[1] as Float?)
                )
            }
        )
    }
}