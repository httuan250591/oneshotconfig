package com.onshortconfig.smartconfig.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.io.Serializable

interface HMJsonObject : Serializable {

    fun toJson(): String {
        return HMJsonParser.getDefaultGSONParser().toJson(this@HMJsonObject)
    }

    companion object {

        inline fun <reified T : HMJsonObject> create(clazz: Class<T>, jsonString: String?): T {
            return try {
                Gson().fromJson<T>(jsonString ?: "{}", clazz)
            } catch (exception: JsonSyntaxException) {
                exception.printStackTrace()
                clazz.newInstance()
            }
        }
    }
}

object HMJsonParser {

    fun getDefaultGSONParser(): Gson {
        val builder = GsonBuilder()
            .setLenient()
            .serializeNulls()

        return builder.create()
    }
}