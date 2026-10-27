/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal fun Throwable.toFriendlyMessage(): String =
    networkErrorMessage()
        ?: localizedMessage?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: "未知错误（${javaClass.simpleName}）"

internal fun Iterable<Throwable>.toFriendlyMessages(): String =
    map { it.toFriendlyMessage() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
        .ifBlank { "未知错误" }

private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean =
    generateSequence(this) { it.cause }.any { it is T }

private fun Throwable.networkErrorMessage(): String? = when {
    hasCause<SocketTimeoutException>() -> "网络连接超时，请检查网络后重试"
    hasCause<UnknownHostException>() -> "无法解析服务器地址，请检查网络连接或 DNS 设置"
    hasCause<ConnectException>() -> "无法连接到服务器，请检查网络或服务器状态后重试"
    hasCause<NoRouteToHostException>() -> "无法访问服务器，请检查网络连接"
    hasCause<PortUnreachableException>() -> "服务器端口不可达，请检查网络环境"
    hasCause<SSLHandshakeException>() -> "安全连接（SSL/TLS）握手失败，请检查网络环境或服务器证书"
    hasCause<SSLPeerUnverifiedException>() -> "服务器证书验证失败，请检查网络环境"
    hasCause<SSLException>() -> "建立安全连接失败，请检查网络环境"
    hasCause<EOFException>() -> "服务器连接意外中断，请重试"
    hasCause<ProtocolException>() -> "网络协议错误，请稍后重试"
    hasCause<UnknownServiceException>() -> "服务器返回了无法识别的响应"
    hasCause<SocketException>() -> "网络连接异常，请检查网络后重试"
    hasCause<InterruptedIOException>() -> "网络请求被中断或超时，请重试"
    hasCause<IOException>() -> "网络请求失败，请检查网络后重试"
    else -> null
}
