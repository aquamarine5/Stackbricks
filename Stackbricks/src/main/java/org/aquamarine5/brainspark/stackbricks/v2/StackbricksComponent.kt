package org.aquamarine5.brainspark.stackbricks.v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.aquamarine5.brainspark.stackbricks.R

@Composable
fun StackbricksComponent(
    stackbricksService: StackbricksService,
    status:MutableState<StackbricksStatus> = rememberStackbricksStatus()) {
    R.font.gilroy

}

@Composable
fun rememberStackbricksStatus():MutableState<StackbricksStatus>{
    return remember { mutableStateOf(StackbricksStatus.STATUS_START) }
}