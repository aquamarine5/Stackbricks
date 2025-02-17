package org.aquamarine5.brainspark.stackbricks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuConfiguration
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuMessageProvider
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuPackageProvider
import java.io.IOException

@Composable
fun StackbricksComponent(
    service: StackbricksStateService,
    modifier: Modifier = Modifier
) {
    val buttonColorMatchMap = mapOf(
        StackbricksStatus.STATUS_START to Color(81, 196, 211),
        StackbricksStatus.STATUS_CHECKING to Color(81, 196, 211),
        StackbricksStatus.STATUS_CLICK_INSTALL to Color(236, 138, 164),
        StackbricksStatus.STATUS_INTERNAL_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_NETWORK_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_DOWNLOADING to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWER_VERSION to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWEST to Color(69, 210, 154)
    )
    val tipsTextMatchMap = mapOf(
        StackbricksStatus.STATUS_NEWEST to stringResource(R.string.stackbricks_tips_newest),
        StackbricksStatus.STATUS_NEWER_VERSION to stringResource(R.string.stackbricks_tips_newversion),
        StackbricksStatus.STATUS_START to stringResource(R.string.stackbricks_tips_checkupdate),
        StackbricksStatus.STATUS_INTERNAL_ERROR to stringResource(R.string.stackbricks_tips_programerror),
        StackbricksStatus.STATUS_NETWORK_ERROR to stringResource(R.string.stackbricks_tips_networkerror),
        StackbricksStatus.STATUS_CLICK_INSTALL to stringResource(R.string.stackbricks_tips_clickinstall),
        StackbricksStatus.STATUS_DOWNLOADING to stringResource(R.string.stackbricks_tips_downloading),
        StackbricksStatus.STATUS_CHECKING to stringResource(R.string.stackbricks_tips_checking)
    )
    var status by service.state.status
    var downloadProgress by service.state.downloadingProgress
    var errorTips by remember { mutableStateOf("") }
    val buttonColorValue by remember { derivedStateOf { buttonColorMatchMap[status]!! } }
    val buttonColor by animateColorAsState(buttonColorValue)
    val buttonTips by remember {
        derivedStateOf {
            if (status == StackbricksStatus.STATUS_NETWORK_ERROR || status == StackbricksStatus.STATUS_INTERNAL_ERROR)
                errorTips
            else
                tipsTextMatchMap[status]!!
        }
    }
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            try {
                when (status) {
                    StackbricksStatus.STATUS_NEWEST,
                    StackbricksStatus.STATUS_START -> {
                        coroutineScope.launch {
                            status = StackbricksStatus.STATUS_CHECKING
                            status =
                                if (service.isNewerVersion())
                                    StackbricksStatus.STATUS_NEWER_VERSION
                                else StackbricksStatus.STATUS_NEWEST
                        }
                    }

                    StackbricksStatus.STATUS_CHECKING,
                    StackbricksStatus.STATUS_DOWNLOADING -> {

                    }

                    StackbricksStatus.STATUS_NEWER_VERSION -> {
                        coroutineScope.launch {
                            downloadProgress = 0f
                            service.downloadPackageWithProgress(service.getVersionData())
                        }
                    }

                    StackbricksStatus.STATUS_CLICK_INSTALL -> {
                        val isSuccess = service.installPackage()
                        if (isSuccess) {
                            status = StackbricksStatus.STATUS_NEWEST
                        } else {
                            status = StackbricksStatus.STATUS_INTERNAL_ERROR
                            errorTips = "安装失败"
                        }
                    }

                    StackbricksStatus.STATUS_INTERNAL_ERROR,
                    StackbricksStatus.STATUS_NETWORK_ERROR -> {
                        coroutineScope.launch {
                            status = StackbricksStatus.STATUS_CHECKING
                            status =
                                if (service.isNewerVersion())
                                    StackbricksStatus.STATUS_NEWER_VERSION
                                else StackbricksStatus.STATUS_NEWEST
                        }
                    }
                }
            } catch (ioE: IOException) {
                status = StackbricksStatus.STATUS_NETWORK_ERROR
                errorTips = "网络错误：${ioE.localizedMessage}"
            } catch (e: Exception) {
                status = StackbricksStatus.STATUS_INTERNAL_ERROR
                errorTips = "内部错误：${e.localizedMessage}"
            }
        },
        colors = ButtonDefaults.buttonColors(buttonColor),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(7.dp, 7.dp, 7.dp, 4.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.stackbricks_logo),
                    contentDescription = "",
                    modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp),
                    tint = Color.Unspecified
                )
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = buttonTips,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(16F, TextUnitType.Sp)
                    )
                }
            }
            downloadProgress?.let {
                LinearProgressIndicator(
                    progress = { it },
                    modifier = Modifier.fillMaxWidth(),
                    gapSize = (-1).dp,
                    drawStopIndicator = {}
                )
                if (it == 1F) {
                    status = StackbricksStatus.STATUS_CLICK_INSTALL
                }
            }
            Text(
                text = buildAnnotatedString {
                    append("更新服务由 ")
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily(
                                Font(R.font.gilroy)
                            ),
                            fontSize = TextUnit(13F, TextUnitType.Sp),
                        )
                    ) {
                        append("Stackbricks")
                    }
                    append(" 提供。")
                },
                fontSize = TextUnit(12F, TextUnitType.Sp),

                )
        }
    }
}

@Composable
fun rememberStackbricksStatus(
    status: StackbricksStatus = StackbricksStatus.STATUS_START,
    downloadProgress: Float? = null
): StackbricksState {
    return remember(status, downloadProgress) {
        StackbricksState(
            status = mutableStateOf(status),
            downloadingProgress = mutableStateOf(downloadProgress)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val qiniuConfiguration = QiniuConfiguration("http://localhost:8080", "/config.json")
    StackbricksComponent(
        StackbricksStateService(
            LocalContext.current, QiniuMessageProvider(qiniuConfiguration),
            packageProvider = QiniuPackageProvider(qiniuConfiguration),
            state = rememberStackbricksStatus()
        )
    )
}