package org.aquamarine5.brainspark.stackbricks.msgpvder

import android.accounts.NetworkErrorException
import android.icu.util.VersionInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.aquamarine5.brainspark.stackbricks.ExceptionalResult
import org.aquamarine5.brainspark.stackbricks.IMsgPvder
import org.aquamarine5.brainspark.stackbricks.StackbricksService
import org.aquamarine5.brainspark.stackbricks.UpdateMessage
import org.json.JSONObject

class GithubApiMsgPvder: IMsgPvder {
    override suspend fun getUpdateMessage(msgPvderData: String): ExceptionalResult<UpdateMessage> {
        try {
            var data: List<String> = listOf()
            val request = Request.Builder()
                .url("https://api.github.com/repos/$msgPvderData/releases/latest")
                .build()
            return withContext(Dispatchers.IO) {
                StackbricksService.okHttpClient.newCall(request).execute().apply {
                    if (isSuccessful) {
                        val json = JSONObject(body!!.string())
                        Log.d(WeiboCommentsMsgPvder::class.simpleName, json.toString())
                        data =
                            json.getString("body").split("**The above content is only used for Stackbricks to get the data of the updated version.**")[0].split(";;")
                    } else throw NetworkErrorException("a")
                }
                ExceptionalResult.success(
                    UpdateMessage(
                        VersionInfo.getInstance(data[1]),
                        data[2],
                        data[3]
                    )
                )
            }
        } catch (ex: Exception) {
            return ExceptionalResult.fail(ex, "")
        }
    }

    override val id: String
        get() = MsgPvderID

    companion object {
        const val MsgPvderID = "stbkt.msgpvder.githubapi"
    }
}