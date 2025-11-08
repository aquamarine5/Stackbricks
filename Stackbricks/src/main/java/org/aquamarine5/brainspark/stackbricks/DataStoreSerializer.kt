/*
 * Copyright (c) 2025, @aquamarine5 (@海蓝色的咕咕鸽). All Rights Reserved.
 * Author: aquamarine5@163.com (Github: https://github.com/aquamarine5) and Brainspark (previously RenegadeCreation)
 * Repository: https://github.com/aquamarine5/Stackbricks
 */

package org.aquamarine5.brainspark.stackbricks

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import org.aquamarine5.brainspark.stackbricks.datastore.StackbricksDataStore
import java.io.InputStream
import java.io.OutputStream

object DataStoreSerializer : Serializer<StackbricksDataStore> {
    override val defaultValue: StackbricksDataStore
        get() = StackbricksDataStore.newBuilder()
            .setIsBetaChannel(false)
            .setIsCheckUpdateOnLaunch(true)
            .build()

    override suspend fun readFrom(input: InputStream): StackbricksDataStore = try {
        StackbricksDataStore.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(t: StackbricksDataStore, output: OutputStream) =
        t.writeTo(output)
}

val Context.stackbricksDataStore: DataStore<StackbricksDataStore> by dataStore(
    fileName = "stackbricks_datastore.pb",
    serializer = DataStoreSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { DataStoreSerializer.defaultValue }
)