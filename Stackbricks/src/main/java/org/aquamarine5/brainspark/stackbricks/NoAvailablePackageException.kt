/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

class NoAvailablePackageException(
    val exceptions: List<Throwable>
) : Exception("没有可用的安装包下载源（${exceptions.toFriendlyMessages()}）") {

    fun getFriendlyExceptionMessages(): String = exceptions.toFriendlyMessages()
}
