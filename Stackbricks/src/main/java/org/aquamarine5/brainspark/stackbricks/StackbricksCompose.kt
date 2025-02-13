package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.aquamarine5.brainspark.stackbricks.msgpvder.WeiboCommentsMsgPvder

class StackbricksCompose(
    private val coroutineScope: CoroutineScope,
    val context: Context,
    msgPvderId: String,
    msgPvderData: String
) {
    val stackbricksService = StackbricksService(context, msgPvderId, msgPvderData)
    private val buttonColorMatchMap = mapOf(
        StackbricksStatus.STATUS_START to Color(81, 196, 211),
        StackbricksStatus.STATUS_CHECKING to Color(81, 196, 211),
        StackbricksStatus.STATUS_CLICKINSTALL to Color(236, 138, 164),
        StackbricksStatus.STATUS_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_DOWNLOADING to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWVERSION to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWEST to Color(69,210,154)
    )
    private val tipsTextMatchMap = mapOf(
        StackbricksStatus.STATUS_NEWEST to context.getString(R.string.stackbricks_tips_newest),
        StackbricksStatus.STATUS_NEWVERSION to context.getString(R.string.stackbricks_tips_newversion),
        StackbricksStatus.STATUS_START to context.getString(R.string.stackbricks_tips_checkupdate),
        StackbricksStatus.STATUS_ERROR to context.getString(R.string.stackbricks_tips_programerror),
        StackbricksStatus.STATUS_NETWORKERROR to context.getString(R.string.stackbricks_tips_networkerror),
        StackbricksStatus.STATUS_CLICKINSTALL to context.getString(R.string.stackbricks_tips_clickinstall),
        StackbricksStatus.STATUS_DOWNLOADING to context.getString(R.string.stackbricks_tips_downloading),
        StackbricksStatus.STATUS_CHECKING to context.getString(R.string.stackbricks_tips_checking)
    )

    private var mStatus = StackbricksStatus.STATUS_START
    var status: StackbricksStatus
        get() {
            return mStatus
        }
        set(value) {
            mStatus = value
            buttonColor = buttonColorMatchMap[mStatus]!!
            tipsText = tipsTextMatchMap[mStatus]!!
        }

    var buttonColor by mutableStateOf(buttonColorMatchMap[mStatus]!!)
    var tipsText by mutableStateOf(tipsTextMatchMap[mStatus]!!)
    private fun onException(exception: Exception?, helpText: String?) {
        buttonColor = buttonColorMatchMap[StackbricksStatus.STATUS_ERROR]!!
        tipsText = helpText + exception?.toString()
    }

    private fun onException(exceptionalResponse: ExceptionalResponse) {
        buttonColor = buttonColorMatchMap[StackbricksStatus.STATUS_ERROR]!!
        tipsText = exceptionalResponse.helpText + exceptionalResponse.exception?.message
    }

    private suspend fun onClickEvent() {
        when (mStatus) {
            StackbricksStatus.STATUS_START, StackbricksStatus.STATUS_NEWEST -> {
                status = StackbricksStatus.STATUS_CHECKING
                val result = stackbricksService.checkUpdate()
                if (!result.isSuccess) {
                    onException(result.exception, result.helpText)
                    return
                }
                status = if (result.result!!) {
                    StackbricksStatus.STATUS_NEWVERSION
                } else {
                    StackbricksStatus.STATUS_NEWEST
                }
            }

            StackbricksStatus.STATUS_NEWVERSION -> {
                status = StackbricksStatus.STATUS_DOWNLOADING
                stackbricksService.getUpdatePackage()
                status = StackbricksStatus.STATUS_CLICKINSTALL
            }

            StackbricksStatus.STATUS_CLICKINSTALL -> {
                val updatePackage = stackbricksService.getUpdatePackage()
                if (updatePackage.isSuccess) {
                    updatePackage.result!!.installApk(context)
                } else {
                    onException(updatePackage.exception, updatePackage.helpText)
                }
                status = StackbricksStatus.STATUS_NEWEST
            }

            else -> {}
        }
    }

    @Composable
    fun DrawCompose() {
        Button(
            onClick = {
                coroutineScope.launch {
                    onClickEvent()
                }
            },
            colors = ButtonDefaults.buttonColors(buttonColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(7.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
                ) {
                    val iconModifier = Modifier
                        .padding(0.dp, 0.dp, 20.dp, 0.dp)
                    Icon(
                        painter = painterResource(R.drawable.stackbricks_logo),
                        contentDescription = "",
                        modifier = iconModifier,
                        tint = Color.Unspecified
                    )
                    Text(
                        tipsText, fontWeight = FontWeight.Bold, fontSize = TextUnit(
                            16F,
                            TextUnitType.Sp
                        )
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceAround) {
                    Text(
                        "OnceShot 的更新服务由 Stackbricks-kt 提供 (@aquamarine5, @海蓝色的咕咕鸽)",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp).width(190.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.developedby_rngdcreation),
                        "developedby_rngdcreation"
                    )
                }
            }

        }
    }
}

@Composable
@Preview()
fun preview() {
    StackbricksCompose(
        rememberCoroutineScope(),
        LocalContext.current, WeiboCommentsMsgPvder.MsgPvderID, "4936409558027888"
    ).DrawCompose()

}