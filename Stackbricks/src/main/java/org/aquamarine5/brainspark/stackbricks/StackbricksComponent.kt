package org.aquamarine5.brainspark.stackbricks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuConfiguration
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuMessageProvider
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuPackageProvider
import java.io.IOException
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackbricksComponent(
    service: StackbricksService,
    modifier: Modifier = Modifier,
    checkUpdateOnLaunch: Boolean = true,
    trigger: StackbricksEventTrigger? = null
) {
    val buttonColorMatchMap = mapOf(
        StackbricksStatus.STATUS_START to Color(81, 196, 211),
        StackbricksStatus.STATUS_CHECKING to Color(81, 196, 211),
        StackbricksStatus.STATUS_CLICK_INSTALL to Color(236, 138, 164),
        StackbricksStatus.STATUS_INTERNAL_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_NETWORK_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_DOWNLOADING to Color(248, 223, 112),
        StackbricksStatus.STATUS_BETA_AVAILABLE to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWER_VERSION to Color(248, 223, 112),
        StackbricksStatus.STATUS_NEWEST to Color(69, 210, 154)
    )
    val tipsTextMatchMap = mapOf(
        StackbricksStatus.STATUS_NEWEST to stringResource(R.string.stackbricks_tips_newest),
        StackbricksStatus.STATUS_NEWER_VERSION to stringResource(R.string.stackbricks_tips_newversion),
        StackbricksStatus.STATUS_BETA_AVAILABLE to "有测试版可用",
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
            when (status) {
                StackbricksStatus.STATUS_NETWORK_ERROR, StackbricksStatus.STATUS_INTERNAL_ERROR -> errorTips
                else -> tipsTextMatchMap[status]!!
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var buttonSize by remember { mutableFloatStateOf(10.dp.value) }
    Box {
        AnimatedVisibility(
            service.internalVersionData?.isStable == false,
            enter = expandVertically()+ fadeIn(),
            exit = shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8D86A)
                ), modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=with(LocalDensity.current) { ceil(buttonSize).toInt().toDp() } - 16.dp)
                    .zIndex(0f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        painterResource(R.drawable.ic_triangle_alert),
                        contentDescription = "Alert"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "你正在尝试使用测试版，可能会导致程序崩溃或数据丢失，请谨慎使用。",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.W500
                    )
                }
            }
        }
        Button(
            onClick = {
                try {
                    when (status) {
                        StackbricksStatus.STATUS_NEWEST,
                        StackbricksStatus.STATUS_START -> {
                            coroutineScope.launch {
                                runCatching {
                                    status = StackbricksStatus.STATUS_CHECKING
                                    status =
                                        if (service.isNewerVersion())
                                            StackbricksStatus.STATUS_NEWER_VERSION
                                        else StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                    errorTips = "内部错误：${it.localizedMessage}"
                                }
                            }
                        }

                        StackbricksStatus.STATUS_CHECKING,
                        StackbricksStatus.STATUS_DOWNLOADING -> {
                            // abort
                        }

                        StackbricksStatus.STATUS_NEWER_VERSION -> {
                            coroutineScope.launch {
                                runCatching {
                                    downloadProgress = 0f
                                    service.downloadPackage()
                                }.onFailure {
                                    status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                    errorTips = "下载失败：${it.localizedMessage}"
                                }
                            }
                        }

                        StackbricksStatus.STATUS_BETA_AVAILABLE -> {

                        }

                        StackbricksStatus.STATUS_CLICK_INSTALL -> {
                            coroutineScope.launch {
                                runCatching {
                                    service.installPackage()
                                }.onSuccess {
                                    status = StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                    errorTips = "安装失败"
                                }
                            }
                        }

                        StackbricksStatus.STATUS_INTERNAL_ERROR,
                        StackbricksStatus.STATUS_NETWORK_ERROR -> {
                            coroutineScope.launch {
                                runCatching {
                                    status = StackbricksStatus.STATUS_CHECKING
                                    status =
                                        if (service.isNewerVersion())
                                            StackbricksStatus.STATUS_NEWER_VERSION
                                        else StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                    errorTips = "内部错误：${it.localizedMessage}"
                                }
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
                .combinedClickable(onLongClick = {
                    coroutineScope.launch {
                        service.isBetaVersionAvailable()
                    }
                }) {}
                .onGloballyPositioned {
                    buttonSize = it.boundsInParent().height
                }
                .zIndex(1f)
                .then(modifier),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(7.dp, 7.dp, 7.dp, 4.dp)
                    .fillMaxWidth()
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        service.isBetaVersionAvailable()
                    }
                }){Text("1")}
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stackbricks_logo),
                        contentDescription = "",
                        modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = buttonTips,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(16F, TextUnitType.Sp)
                    )

                }
                AnimatedVisibility(
                    downloadProgress != null,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
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
                }
                AnimatedVisibility(
                    service.internalVersionData != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            buildAnnotatedString {
                                append("最新")
                                if (status == StackbricksStatus.STATUS_BETA_AVAILABLE) {
                                    withStyle(
                                        SpanStyle(
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("测试版")
                                    }
                                } else {
                                    append("稳定版")
                                }
                                append("：")
                                withStyle(
                                    SpanStyle(
                                        fontFamily = FontFamily(
                                            Font(R.font.gilroy)
                                        ),
                                        fontSize = 13.sp,
                                    )
                                ) {
                                    val message = service.internalVersionData!!
                                    append("${message.versionName}(${message.versionCode})")
                                }
                            },
                            fontSize = 12.sp
                        )
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
                                fontSize = 13.sp,
                            )
                        ) {
                            append("Stackbricks")
                        }
                        append(" 提供。\n")
                        append("当前程序版本：")
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily(
                                    Font(R.font.gilroy)
                                ),
                                fontSize = 13.sp,
                            )
                        ) {
                            append("${service.getCurrentVersionName()}(${service.getCurrentVersion()})")
                        }
                    },
                    fontSize = 12.sp,
                    modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
                )
            }
        }

    }


    LaunchedEffect(Unit) {
        if (checkUpdateOnLaunch && service.state.status.value == StackbricksStatus.STATUS_START) {
            status = StackbricksStatus.STATUS_CHECKING
            status =
                if (service.isNewerVersion())
                    StackbricksStatus.STATUS_NEWER_VERSION
                else StackbricksStatus.STATUS_NEWEST
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
        StackbricksService(
            LocalContext.current, QiniuMessageProvider(qiniuConfiguration),
            packageProvider = QiniuPackageProvider(qiniuConfiguration),
            state = rememberStackbricksStatus()
        )
    )
}