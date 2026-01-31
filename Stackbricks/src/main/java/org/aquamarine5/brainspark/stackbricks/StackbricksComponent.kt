/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuConfiguration
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuMessageProvider
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuPackageProvider
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.ceil

@Composable
fun StackbricksComponent(
    service: StackbricksService,
    modifier: Modifier = Modifier,
    trigger: StackbricksEventTrigger? = null
) {
    val context = LocalContext.current
    val buttonColorMatchMap = mapOf(
        StackbricksStatus.STATUS_START to Color(81, 196, 211),
        StackbricksStatus.STATUS_CHECKING to Color(81, 196, 211),
        StackbricksStatus.STATUS_CLICK_INSTALL to Color(236, 138, 164),
        StackbricksStatus.STATUS_INTERNAL_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_NETWORK_ERROR to Color(238, 72, 102),
        StackbricksStatus.STATUS_DOWNLOADING to Color(0xFFFCC307),
        StackbricksStatus.STATUS_BETA_AVAILABLE to Color(0xFFFCC307),
        StackbricksStatus.STATUS_NEWER_VERSION to Color(0xFFFCC307),
        StackbricksStatus.STATUS_NEWEST to Color(69, 210, 154)
    )
    val tipsTextMatchMap = mapOf(
        StackbricksStatus.STATUS_NEWEST to stringResource(R.string.stackbricks_tips_newest),
        StackbricksStatus.STATUS_NEWER_VERSION to stringResource(R.string.stackbricks_tips_newversion),
        StackbricksStatus.STATUS_BETA_AVAILABLE to "有新测试版可用",
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
    val fontGilroy = SpanStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.gilroy)))
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    var isDownloading = remember { false }
    var buttonSize by remember { mutableFloatStateOf(30.dp.value) }
    var isBetaChannel by remember { mutableStateOf(false) }
    var isCheckUpdateOnLaunch by remember { mutableStateOf(true) }
    var isShowDialog by remember { mutableStateOf(false) }
    val isCurrentTestVersion = remember { service.checkCurrentIsTestChannel() }
    var isShowChangelogDialog by remember { mutableStateOf(false) }
    var isForceInstallDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        context.stackbricksDataStore.data.first().let { datastore ->
            isBetaChannel = datastore.isBetaChannel
            isCheckUpdateOnLaunch = datastore.isCheckUpdateOnLaunch
            runCatching {
                if (datastore.isCheckUpdateOnLaunch && service.state.status.value == StackbricksStatus.STATUS_START) {
                    if (isBetaChannel) {
                        status = StackbricksStatus.STATUS_CHECKING
                        status =
                            if (service.isBetaVersionAvailable() != null)
                                StackbricksStatus.STATUS_BETA_AVAILABLE
                            else StackbricksStatus.STATUS_NEWEST
                    } else {
                        status = StackbricksStatus.STATUS_CHECKING
                        status =
                            if (service.isNewerVersion())
                                StackbricksStatus.STATUS_NEWER_VERSION
                            else StackbricksStatus.STATUS_NEWEST
                    }
                }
            }.onFailure {
                if (it.isWebException()) {
                    status = StackbricksStatus.STATUS_NETWORK_ERROR
                    errorTips = "网络错误：${it.localizedMessage}"
                } else {
                    status = StackbricksStatus.STATUS_INTERNAL_ERROR
                    errorTips = "内部错误：${it.localizedMessage}"
                }
            }
        }
    }
    Box {
        Column {
            Spacer(modifier = Modifier.height(30.dp))
            AnimatedVisibility(
                isBetaChannel && isCurrentTestVersion.not() && buttonSize > 40.dp.value,
                enter = expandVertically() + fadeIn(initialAlpha = 0f),
                exit = shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFE288)
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = with(LocalDensity.current) {
                            ceil(buttonSize).toInt().toDp()
                        } - 60.dp)
                        .zIndex(0f)
                ) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painterResource(R.drawable.ic_triangle_alert),
                            contentDescription = "Alert",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "你正在尝试使用测试版，可能会导致程序崩溃或数据丢失，请谨慎使用。",
                            color = Color.Black,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }
        }
        Column {
            if (isCurrentTestVersion && buttonSize > 40.dp.value) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFE288)
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = with(LocalDensity.current) {
                            ceil(buttonSize).toInt().toDp()
                        } - 31.dp)
                        .zIndex(0f)
                ) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painterResource(R.drawable.ic_badge_info),
                            contentDescription = "Alert",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "你正在使用测试版，可能会导致程序崩溃或数据丢失。",
                            color = Color.Black,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }
        }
        LaunchedEffect(service.internalVersionData) {
            if (service.internalVersionData?.let { it.forceInstallLessVersion > service.getCurrentVersion() } == true) {
                isForceInstallDialog = true
            }
        }
        if (isForceInstallDialog && service.stackbricksPolicy?.isForceInstallValueCallback != false) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    TextButton(onClick = {
                        isForceInstallDialog = false
                        coroutineScope.launch {
                            runCatching {
                                service.downloadPackage().let {
                                    trigger?.onDownloadPackage()
                                }
                                service.installPackage().let {
                                    trigger?.onInstallPackage(it, service.internalVersionData!!)
                                }
                            }.onFailure {
                                status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                errorTips = "安装失败"
                                isForceInstallDialog = false
                            }
                        }
                    }) {
                        Text("下载并安装")
                    }
                },
                text = {
                    Column {
                        Text(
                            "版本过低需要强制更新",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "当前版本为 ${service.getCurrentVersionName()}(${service.getCurrentVersion()})，版本过低需要强制安装新版本。\n" +
                                    "新版本：${service.internalVersionData?.versionName}(${service.internalVersionData?.versionCode})\n"
                        )
                    }
                })
        }
        Button(
            onClick = {
                runCatching {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    when (status) {
                        StackbricksStatus.STATUS_NEWEST -> {
                            coroutineScope.launch {
                                runCatching {
                                    status = StackbricksStatus.STATUS_CHECKING
                                    trigger?.onCheckUpdate(isTestChannel = false)
                                    status = if (isBetaChannel)
                                        if (service.isBetaVersionAvailable(true) != null)
                                            StackbricksStatus.STATUS_BETA_AVAILABLE
                                        else
                                            StackbricksStatus.STATUS_NEWEST
                                    else
                                        if (service.isNewerVersion(true))
                                            StackbricksStatus.STATUS_NEWER_VERSION
                                        else StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                    if (it.isWebException()) {
                                        status = StackbricksStatus.STATUS_NETWORK_ERROR
                                        errorTips = "网络错误：${it.localizedMessage}"
                                    } else {
                                        status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                        errorTips = "内部错误：${it.localizedMessage}"
                                    }
                                }
                            }
                        }

                        StackbricksStatus.STATUS_START -> {
                            coroutineScope.launch {
                                runCatching {
                                    status = StackbricksStatus.STATUS_CHECKING
                                    trigger?.onCheckUpdate(isTestChannel = false)
                                    status = if (isBetaChannel)
                                        if (service.isBetaVersionAvailable() != null)
                                            StackbricksStatus.STATUS_BETA_AVAILABLE
                                        else
                                            StackbricksStatus.STATUS_NEWEST
                                    else
                                        if (service.isNewerVersion())
                                            StackbricksStatus.STATUS_NEWER_VERSION
                                        else StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                    if (it.isWebException()) {
                                        status = StackbricksStatus.STATUS_NETWORK_ERROR
                                        errorTips = "网络错误：${it.localizedMessage}"
                                    } else {
                                        status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                        errorTips = "内部错误：${it.localizedMessage}"
                                    }
                                }
                            }
                        }

                        StackbricksStatus.STATUS_CHECKING,
                        StackbricksStatus.STATUS_DOWNLOADING -> {
                            // abort
                        }

                        StackbricksStatus.STATUS_BETA_AVAILABLE,
                        StackbricksStatus.STATUS_NEWER_VERSION -> {
                            coroutineScope.launch {
                                runCatching {
                                    downloadProgress = 0f
                                    if (isDownloading.not()) {
                                        isDownloading = true
                                        trigger?.onDownloadPackage()
                                        suspendCoroutine{ continuation ->
                                            coroutineScope.launch {
                                                service.downloadPackage(continuation=continuation)
                                            }
                                        }
                                        isDownloading = false
                                    }
                                }.onFailure {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                    if (it.isWebException()) {
                                        status = StackbricksStatus.STATUS_NETWORK_ERROR
                                        errorTips = "网络错误：${it.localizedMessage}"
                                    } else {
                                        status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                        errorTips = "内部错误：${it.localizedMessage}"
                                    }
                                }
                            }
                        }

                        StackbricksStatus.STATUS_CLICK_INSTALL -> {
                            coroutineScope.launch {
                                runCatching {
                                    service.installPackage().let {
                                        trigger?.onInstallPackage(
                                            it,
                                            service.internalVersionData!!
                                        )
                                    }
                                }.onSuccess {
                                    status = StackbricksStatus.STATUS_NEWEST
                                }.onFailure {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
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
                                    trigger?.onCheckUpdate(isTestChannel = false)
                                }.onFailure {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                    if (it.isWebException()) {
                                        status = StackbricksStatus.STATUS_NETWORK_ERROR
                                        errorTips = "网络错误：${it.localizedMessage}"
                                    } else {
                                        status = StackbricksStatus.STATUS_INTERNAL_ERROR
                                        errorTips = "内部错误：${it.localizedMessage}"
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                    if (it.isWebException()) {
                        status = StackbricksStatus.STATUS_NETWORK_ERROR
                        errorTips = "网络错误：${it.localizedMessage}"
                    } else {
                        status = StackbricksStatus.STATUS_INTERNAL_ERROR
                        errorTips = "内部错误：${it.localizedMessage}"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(buttonColor),
            modifier = Modifier
                .fillMaxWidth()
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
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(2.dp))
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
                }
                AnimatedVisibility(
                    service.internalVersionData != null && (status == StackbricksStatus.STATUS_BETA_AVAILABLE || status == StackbricksStatus.STATUS_NEWER_VERSION),
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
                                withStyle(fontGilroy) {
                                    val message = service.internalVersionData!!
                                    append("${message.versionName} (${message.versionCode})")
                                }
                            },
                            fontSize = 12.sp
                        )
                    }
                }
                if (isShowChangelogDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowChangelogDialog = false },
                        confirmButton = {
                            TextButton(onClick = { isShowChangelogDialog = false }) {
                                Text("确定")
                            }
                        },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                var changelog by remember { mutableStateOf("") }
                                LaunchedEffect(Unit) {
                                    runCatching {
                                        changelog = service.getCurrentChangelog()
                                    }.onFailure {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                        changelog = "获取更新日志失败：${it.localizedMessage}"
                                    }
                                }
                                Text(
                                    "更新日志",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(changelog)
                            }
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("更新服务由 ")
                            withStyle(fontGilroy) {
                                append("Stackbricks")
                            }
                            append(" 提供。\n")
                            append("当前程序版本：")
                            withStyle(fontGilroy) {
                                append("${service.getCurrentVersionName()} (${service.getCurrentVersion()})")
                            }
                        },
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(0.dp, 4.dp, 0.dp, 0.dp)
                            .weight(1f)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        IconButton(
                            onClick = {
                                isShowChangelogDialog = true
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_file_text),
                                null,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Icon(
                            painterResource(R.drawable.ic_settings),
                            null,
                            modifier = Modifier
                                .clickable {
                                    isShowDialog = true
                                }
                                .size(24.dp)
                        )
                    }

                }
            }
        }
        if (isShowDialog) {
            if (isBetaChannel.not() && service.checkCurrentIsTestChannel()) {
                isBetaChannel = true
                LaunchedEffect(Unit) {
                    context.stackbricksDataStore.updateData { datastore ->
                        datastore.toBuilder()
                            .setIsBetaChannel(isBetaChannel)
                            .build()
                    }
                }
            }
            AlertDialog(
                onDismissRequest = { isShowDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        isShowDialog = false
                        coroutineScope.launch {
                            context.stackbricksDataStore.updateData { datastore ->
                                datastore.toBuilder()
                                    .setIsBetaChannel(isBetaChannel)
                                    .setIsCheckUpdateOnLaunch(isCheckUpdateOnLaunch)
                                    .build()
                            }
                            if (isBetaChannel) {
                                status = StackbricksStatus.STATUS_CHECKING
                                status =
                                    if (service.isBetaVersionAvailable() != null)
                                        StackbricksStatus.STATUS_BETA_AVAILABLE
                                    else StackbricksStatus.STATUS_NEWEST
                            }
                        }
                    }) {
                        Text("确定")
                    }
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_settings),
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column {
                        Text(buildAnnotatedString {
                            append("修改 ")
                            withStyle(
                                SpanStyle(
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily(Font(R.font.gilroy))
                                )
                            ) {
                                append("Stackbricks")
                            }
                            append(" 设置：")
                        }, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("使用测试版")
                            Switch(
                                isBetaChannel,
                                onCheckedChange = {
                                    isBetaChannel = it
                                    trigger?.onChannelChanged(it)
                                },
                                enabled = isCurrentTestVersion.not()
                            )
                        }
                        if (isCurrentTestVersion && isBetaChannel) {
                            Text(
                                "当前已经使用测试版本，不能回退到稳定版。",
                                color = Color(0xFFF34718)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("启动时检查更新")
                            Switch(
                                isCheckUpdateOnLaunch,
                                onCheckedChange = {
                                    isCheckUpdateOnLaunch = it
                                    trigger?.onCheckUpdateOnLaunchChanged(it)
                                },
                                enabled = service.stackbricksPolicy?.isAllowedToDisableCheckUpdateOnLaunch
                                    ?: true
                            )
                        }
                        if (service.stackbricksPolicy?.isAllowedToDisableCheckUpdateOnLaunch == false) {
                            Text(
                                "开发者设置了应用更新策略，不允许此值。",
                                color = Color(0xFFF34718)
                            )
                        }
                        Text(
                            "Stackbricks ${BuildConfig.stackbricksVersion}, by @aquamarine5.",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color.Gray
                        )
                    }
                })
        }
    }
}


@Composable
fun rememberStackbricksStatus(
    status: StackbricksStatus = StackbricksStatus.STATUS_START,
    downloadProgress: Float? = null
): StackbricksState {
    return rememberSaveable(saver = StackbricksState.Saver) {
        StackbricksState(
            status = mutableStateOf(status),
            downloadingProgress = mutableStateOf(downloadProgress)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val qiniuConfiguration = QiniuConfiguration(
        possibleConfigurations = listOf(
            "localhost" to "stackbricks_manifest_v3.json"
        )
    )
    StackbricksComponent(
        StackbricksService(
            LocalContext.current, QiniuMessageProvider(qiniuConfiguration),
            packageProvider = QiniuPackageProvider(qiniuConfiguration),
            state = rememberStackbricksStatus()
        )
    )
}

fun Throwable.isWebException(): Boolean =
    this is SocketException || this is SocketTimeoutException || this is UnknownHostException || this is SSLHandshakeException || this is IOException